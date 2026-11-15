package com.xiaojinzi.tally.module.qianji.autorecord

import android.Manifest
import android.accessibilityservice.AccessibilityService
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.xiaojinzi.tally.module.qianji.QianjiConfirmActivity
import java.math.BigDecimal

object PaymentNotifications {
    private const val CHANNEL = "payment_confirmation_v2"
    const val EXTRA_PENDING_ID = "pending_payment_id"

    fun intent(context: Context, id: String): Intent = Intent(context, QianjiConfirmActivity::class.java)
        .putExtra(EXTRA_PENDING_ID, id)
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)

    fun publish(context: Context, pending: PendingPayment): Boolean {
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(NotificationChannel(CHANNEL, "记账确认", NotificationManager.IMPORTANCE_HIGH).apply {
            description = "支付结果识别后，由你确认是否记账"
            lockscreenVisibility = NotificationCompat.VISIBILITY_PRIVATE
        })
        if (Build.VERSION.SDK_INT >= 33 &&
            context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) return false
        if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) return false
        val open = PendingIntent.getActivity(
            context, pending.id.hashCode(), intent(context, pending.id),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val payment = pending.payment
        val title = if (pending.synthetic) "识别自检 · 待确认" else "识别到一笔支付"
        val notification = NotificationCompat.Builder(context, CHANNEL)
            .setSmallIcon(android.R.drawable.ic_menu_edit)
            .setContentTitle(title)
            .setContentText("${if (payment.income) "收入" else "支出"} ¥${money(payment.amountFen)} · 点击核对")
            .setContentIntent(open)
            .addAction(android.R.drawable.ic_menu_edit, "核对账单", open)
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            .setOnlyAlertOnce(true)
            .build()
        return runCatching {
            manager.notify(pending.id.hashCode(), notification)
            true
        }.getOrDefault(false)
    }

    fun cancel(context: Context, id: String) {
        context.getSystemService(NotificationManager::class.java).cancel(id.hashCode())
    }

    fun money(amountFen: Long): String = BigDecimal.valueOf(amountFen, 2).toPlainString()
}

/** 授权无障碍服务内的确认卡；只响应明确点击，绝不自行点击支付界面或保存账单。 */
class PaymentOverlay(private val service: AccessibilityService) {
    private val windowManager = service.getSystemService(WindowManager::class.java)
    private var view: View? = null
    private var shownId: String? = null

    fun show(pending: PendingPayment): Boolean {
        if (view != null) return false
        val dark = service.resources.configuration.uiMode and 0x30 == 0x20
        val background = if (dark) 0xFF25262A.toInt() else Color.WHITE
        val foreground = if (dark) 0xFFF4F4F7.toInt() else 0xFF191A1E.toInt()
        val muted = if (dark) 0xFFB3B5BF.toInt() else 0xFF6B6E78.toInt()
        val card = LinearLayout(service).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(18), dp(20), dp(12))
            elevation = dp(10).toFloat()
            this.background = GradientDrawable().apply {
                setColor(background)
                cornerRadius = dp(24).toFloat()
            }
        }
        fun label(value: String, size: Float, color: Int, bold: Boolean = false): TextView = TextView(service).apply {
            text = value
            textSize = size
            setTextColor(color)
            if (bold) setTypeface(typeface, Typeface.BOLD)
        }
        card.addView(label(if (pending.synthetic) "识别自检 · 合成页面" else "自动记账 · 待确认", 13f, muted))
        card.addView(label("${if (pending.payment.income) "+" else "−"} ¥${PaymentNotifications.money(pending.payment.amountFen)}", 28f, foreground, true).apply {
            setPadding(0, dp(6), 0, dp(4))
        })
        card.addView(label(pending.payment.merchant.take(60), 15f, foreground))
        card.addView(label("核对账本、分类和账户后保存", 12f, muted).apply { setPadding(0, dp(6), 0, dp(8)) })
        val actions = LinearLayout(service).apply { gravity = Gravity.END }
        fun button(title: String, onClick: () -> Unit) = label(title, 14f, 0xFF3478F6.toInt(), true).apply {
            gravity = Gravity.CENTER
            minHeight = dp(48)
            setPadding(dp(10), 0, dp(10), 0)
            setOnClickListener { onClick() }
        }
        actions.addView(button("忽略") {
            PendingPayments.remove(service, pending.id)
            PaymentNotifications.cancel(service, pending.id)
            dismiss()
        })
        actions.addView(button("稍后") { dismiss() })
        actions.addView(button("核对并记账") {
            dismiss()
            service.startActivity(PaymentNotifications.intent(service, pending.id))
        })
        card.addView(actions)
        val available = service.resources.displayMetrics.widthPixels - dp(24)
        val params = WindowManager.LayoutParams(
            minOf(available, dp(380)), WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT,
        ).apply {
            gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
            y = dp(68)
        }
        return runCatching {
            windowManager.addView(card, params)
            view = card
            shownId = pending.id
            card.postDelayed({ if (shownId == pending.id) dismiss() }, 15_000L)
            true
        }.getOrElse {
            dismiss()
            false
        }
    }

    fun dismiss() {
        view?.let { runCatching { windowManager.removeViewImmediate(it) } }
        view = null
        shownId = null
    }

    private fun dp(value: Int) = (value * service.resources.displayMetrics.density).toInt()
}
