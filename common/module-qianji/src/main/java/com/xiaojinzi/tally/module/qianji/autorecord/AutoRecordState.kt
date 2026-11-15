package com.xiaojinzi.tally.module.qianji.autorecord

import android.content.Context
import android.os.SystemClock
import com.xiaojinzi.tally.module.qianji.recognition.RecognizedPayment
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

object AutoRecordState {
    data class Status(
        val connected: Boolean = false,
        val sampled: Int = 0,
        val matched: Int = 0,
        val duplicate: Int = 0,
        val lastResult: String = "等待无障碍服务连接",
    )

    private val mutableStatus = MutableStateFlow(Status())
    val status = mutableStatus.asStateFlow()

    fun connected(value: Boolean) {
        mutableStatus.value = mutableStatus.value.copy(
            connected = value,
            lastResult = if (value) "服务已连接，等待支付结果页" else "服务已断开",
        )
    }

    fun result(message: String, accepted: Boolean = false, duplicate: Boolean = false) {
        val old = mutableStatus.value
        mutableStatus.value = old.copy(
            sampled = old.sampled + 1,
            matched = old.matched + if (accepted) 1 else 0,
            duplicate = old.duplicate + if (duplicate) 1 else 0,
            lastResult = message,
        )
    }
}

object AutoRecordPreferences {
    private const val FILE = "auto_record_preferences_v2"
    fun enabled(context: Context) = context.getSharedPreferences(FILE, 0).getBoolean("enabled", false)
    fun setEnabled(context: Context, value: Boolean) {
        context.getSharedPreferences(FILE, 0).edit().putBoolean("enabled", value).apply()
    }
    fun overlay(context: Context) = context.getSharedPreferences(FILE, 0).getBoolean("overlay", true)
    fun setOverlay(context: Context, value: Boolean) {
        context.getSharedPreferences(FILE, 0).edit().putBoolean("overlay", value).apply()
    }
}

/** 只有正在显示的自检页可以授权本进程内的合成页面；不接收外部 Intent 的包名覆盖。 */
object RecognitionLabSession {
    data class Active(val packageName: String, val expiresAt: Long)
    @Volatile private var active: Active? = null

    fun begin(packageName: String) {
        active = Active(packageName, SystemClock.elapsedRealtime() + 120_000L)
    }
    fun end() { active = null }
    fun current(): Active? = active?.takeIf { SystemClock.elapsedRealtime() < it.expiresAt }
}

data class PendingPayment(
    val id: String,
    val payment: RecognizedPayment,
    val synthetic: Boolean,
)

/** 只保留确认所需字段，不存支付页面节点、订单截图或未脱敏日志。 */
object PendingPayments {
    private const val FILE = "pending_payments_v2"
    private const val KEY = "records"
    private const val MAX_AGE = 24 * 60 * 60 * 1000L
    private val mutableItems = MutableStateFlow<List<PendingPayment>>(emptyList())
    val items = mutableItems.asStateFlow()

    @Synchronized
    fun load(context: Context): List<PendingPayment> {
        val now = System.currentTimeMillis()
        val json = context.getSharedPreferences(FILE, 0).getString(KEY, "[]").orEmpty()
        val records = runCatching {
            val array = JSONArray(json)
            (0 until array.length()).mapNotNull { index ->
                runCatching {
                    val item = array.getJSONObject(index)
                    val occurred = item.getLong("occurredAt")
                    if (now - occurred > MAX_AGE || occurred > now + 60_000L) return@runCatching null
                    PendingPayment(
                        id = item.getString("id"),
                        synthetic = item.optBoolean("synthetic"),
                        payment = RecognizedPayment(
                            amountFen = item.getLong("amountFen"),
                            income = item.getBoolean("income"),
                            merchant = item.optString("merchant"),
                            sourcePackage = item.getString("sourcePackage"),
                            occurredAt = occurred,
                            ruleId = item.getString("ruleId"),
                            orderReference = null,
                        ),
                    )
                }.getOrNull()
            }.takeLast(32)
        }.getOrDefault(emptyList())
        mutableItems.value = records
        return records
    }

    @Synchronized
    fun add(context: Context, payment: RecognizedPayment, synthetic: Boolean): PendingPayment {
        val pending = PendingPayment(UUID.randomUUID().toString(), payment, synthetic)
        persist(context, (load(context) + pending).takeLast(32))
        return pending
    }

    @Synchronized
    fun find(context: Context, id: String): PendingPayment? = load(context).find { it.id == id }

    @Synchronized
    fun remove(context: Context, id: String) = persist(context, load(context).filterNot { it.id == id })

    private fun persist(context: Context, records: List<PendingPayment>) {
        val array = JSONArray()
        records.forEach { pending ->
            val payment = pending.payment
            array.put(JSONObject().apply {
                put("id", pending.id)
                put("synthetic", pending.synthetic)
                put("amountFen", payment.amountFen)
                put("income", payment.income)
                put("merchant", payment.merchant.take(120))
                put("sourcePackage", payment.sourcePackage)
                put("occurredAt", payment.occurredAt)
                put("ruleId", payment.ruleId)
            })
        }
        context.getSharedPreferences(FILE, 0).edit().putString(KEY, array.toString()).apply()
        mutableItems.value = records
    }
}
