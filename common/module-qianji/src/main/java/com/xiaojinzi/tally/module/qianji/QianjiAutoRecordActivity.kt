package com.xiaojinzi.tally.module.qianji

import android.Manifest
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.accessibility.AccessibilityManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationManagerCompat
import com.xiaojinzi.component.anno.RouterAnno
import com.xiaojinzi.tally.module.base.support.AppRouterConfig
import com.xiaojinzi.tally.module.base.theme.AppTheme
import com.xiaojinzi.tally.module.base.theme.PortActionRow
import com.xiaojinzi.tally.module.base.theme.PortCard
import com.xiaojinzi.tally.module.base.theme.PortPage
import com.xiaojinzi.tally.module.base.theme.PortPrimaryButton
import com.xiaojinzi.tally.module.base.theme.PortSectionTitle
import com.xiaojinzi.tally.module.base.theme.PortTheme
import com.xiaojinzi.tally.module.qianji.authorization.AccessibilityAuthorizationController
import com.xiaojinzi.tally.module.qianji.autorecord.AutoRecordPreferences
import com.xiaojinzi.tally.module.qianji.autorecord.AutoRecordState
import com.xiaojinzi.tally.module.qianji.autorecord.PaymentNotifications
import com.xiaojinzi.tally.module.qianji.autorecord.PendingPayments

@RouterAnno(hostAndPath = AppRouterConfig.QIANJI_AUTO_RECORD)
class QianjiAutoRecordActivity : ComponentActivity() {
    private var revision by mutableIntStateOf(0)
    private lateinit var authorization: AccessibilityAuthorizationController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        authorization = AccessibilityAuthorizationController(this)
        setContent { AppTheme { PortTheme { Content() } } }
    }

    override fun onResume() {
        super.onResume()
        PendingPayments.load(this)
        authorization.refresh()
        revision++
    }

    @Composable
    private fun Content() {
        val status by AutoRecordState.status.collectAsState()
        val pending by PendingPayments.items.collectAsState()
        val grant by authorization.state.collectAsState()
        var enabled by remember { mutableStateOf(AutoRecordPreferences.enabled(this)) }
        var overlay by remember { mutableStateOf(AutoRecordPreferences.overlay(this)) }
        val accessibilityReady = remember(revision, status.connected) { isAccessibilityEnabled() }
        val notificationsReady = remember(revision) { NotificationManagerCompat.from(this).areNotificationsEnabled() }
        PortPage(
            title = "自动记账",
            subtitle = "识别支付结果，由你核对并确认",
            onBack = { finish() },
        ) {
            PortCard {
                Text(if (enabled && accessibilityReady) "已准备好识别" else "完成设置后开始识别", style = MaterialTheme.typography.titleLarge)
                Text(
                    "仅处理支持的支付页面。金额有歧义时不记账，不自动操作支付应用。",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp),
                )
                PortActionRow(
                    title = "支付识别",
                    subtitle = if (enabled) "支付宝、微信、云闪付、数字人民币" else "默认关闭，开启后仍需系统无障碍授权",
                    trailing = {
                        Switch(checked = enabled, onCheckedChange = {
                            enabled = it
                            AutoRecordPreferences.setEnabled(this@QianjiAutoRecordActivity, it)
                        })
                    },
                )
            }
            PortSectionTitle("权限与提醒")
            PortCard {
                PortActionRow(
                    title = "无障碍服务",
                    subtitle = if (accessibilityReady) "已开启" else "未开启 · 前往系统设置",
                    onClick = { startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) },
                )
                PortActionRow(
                    title = "通知提醒",
                    subtitle = if (notificationsReady) "已允许 · 点击通知核对账单" else "未允许 · 可在本页查看待确认记录",
                    onClick = {
                        if (Build.VERSION.SDK_INT >= 33 && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 101)
                        } else {
                            startActivity(Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).putExtra(Settings.EXTRA_APP_PACKAGE, packageName))
                        }
                    },
                )
                PortActionRow(
                    title = "悬浮确认卡",
                    subtitle = "由已授权的无障碍服务显示，15 秒后收起；记录保留在待确认列表",
                    trailing = {
                        Switch(checked = overlay, onCheckedChange = {
                            overlay = it
                            AutoRecordPreferences.setOverlay(this@QianjiAutoRecordActivity, it)
                        })
                    },
                )
            }
            PortSectionTitle("辅助授权")
            PortCard {
                Text("可用 Root 或 Shizuku 辅助开启本应用的无障碍服务。只在点击时执行，保留其他无障碍服务设置。", color = MaterialTheme.colorScheme.onSurfaceVariant)
                PortActionRow(
                    title = "Root 授权",
                    subtitle = "已 Root 设备可使用，请在 Root 管理器中允许",
                    enabled = !grant.busy,
                    onClick = { authorization.requestRoot() },
                )
                PortActionRow(
                    title = "ADB（Shizuku）授权",
                    subtitle = grant.shizukuStatus,
                    enabled = !grant.busy,
                    onClick = { authorization.requestShizuku() },
                )
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = {
                        val launch = packageManager.getLaunchIntentForPackage("moe.shizuku.privileged.api")
                        if (launch != null) startActivity(launch)
                        else android.widget.Toast.makeText(this@QianjiAutoRecordActivity, "请先安装并启动 Shizuku", android.widget.Toast.LENGTH_LONG).show()
                    }, enabled = !grant.busy) { Text("打开 Shizuku") }
                    TextButton(onClick = { authorization.refresh(); revision++ }, enabled = !grant.busy) { Text("刷新状态") }
                }
                if (grant.busy) Text("正在等待授权或验证服务连接…", style = MaterialTheme.typography.bodySmall)
                grant.message?.let {
                    Text(it, color = if (grant.error) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary)
                }
                Text("Shizuku 可通过无线调试或电脑 ADB 启动，也支持 Root。辅助授权后仍需开启上方的支付识别开关。", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            PortSectionTitle("待确认", subtitle = "最多保留 32 条，超过 24 小时自动过期")
            if (pending.isEmpty()) {
                PortCard { Text("还没有待确认的支付记录", color = MaterialTheme.colorScheme.onSurfaceVariant) }
            }
            pending.asReversed().forEach { item ->
                PortCard {
                    PortActionRow(
                        title = "${if (item.payment.income) "收入" else "支出"} ¥${PaymentNotifications.money(item.payment.amountFen)}",
                        subtitle = (if (item.synthetic) "自检 · " else "") + item.payment.merchant,
                        onClick = { startActivity(PaymentNotifications.intent(this@QianjiAutoRecordActivity, item.id)) },
                    )
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = {
                            PendingPayments.remove(this@QianjiAutoRecordActivity, item.id)
                            PaymentNotifications.cancel(this@QianjiAutoRecordActivity, item.id)
                        }) { Text("忽略") }
                    }
                }
            }
            PortSectionTitle("识别自检")
            PortCard {
                Text("自检会展示合成支付页面，由真实无障碍事件读取节点、识别、去重，再弹出悬浮卡和通知。它不代表真实支付 App 的适配率。", color = MaterialTheme.colorScheme.onSurfaceVariant)
                PortPrimaryButton(
                    text = "打开识别自检",
                    onClick = { startActivity(Intent(this@QianjiAutoRecordActivity, QianjiRecognitionLabActivity::class.java)) },
                    modifier = Modifier.padding(top = 12.dp),
                )
                Text("${status.sampled} 次采集 · ${status.matched} 次识别 · ${status.duplicate} 次重复", style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(top = 12.dp))
                Text(status.lastResult, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }

    override fun onDestroy() {
        if (::authorization.isInitialized) authorization.close()
        super.onDestroy()
    }

    private fun isAccessibilityEnabled(): Boolean {
        val manager = getSystemService(AccessibilityManager::class.java)
        return manager.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK).any {
            it.resolveInfo.serviceInfo.packageName == packageName &&
                    it.resolveInfo.serviceInfo.name == QianjiAutoRecordService::class.java.name
        }
    }
}
