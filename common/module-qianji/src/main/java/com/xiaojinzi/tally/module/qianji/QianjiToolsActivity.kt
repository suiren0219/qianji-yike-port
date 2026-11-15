package com.xiaojinzi.tally.module.qianji

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import com.xiaojinzi.component.anno.RouterAnno
import com.xiaojinzi.component.impl.Router
import com.xiaojinzi.tally.module.base.support.AppRouterConfig
import com.xiaojinzi.tally.module.base.theme.AppTheme
import com.xiaojinzi.tally.module.base.theme.PortActionRow
import com.xiaojinzi.tally.module.base.theme.PortCard
import com.xiaojinzi.tally.module.base.theme.PortPage
import com.xiaojinzi.tally.module.base.theme.PortTheme

/** 旧 tools 路由的兼容入口；主流程从首页/设置直达各独立页面。 */
@RouterAnno(hostAndPath = AppRouterConfig.QIANJI_TOOLS)
class QianjiToolsActivity : ComponentActivity() {

    private var status by mutableStateOf<String?>(null)
    private var statusIsError by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AppTheme {
                PortTheme {
                    PortPage(title = "钱迹工具", subtitle = "记账与数据管理的快捷入口", onBack = { finish() }) {
                        PortCard(contentPadding = PaddingValues(0.dp)) {
                            PortActionRow("自动记账", subtitle = "管理识别与记账设置", onClick = { navigate(AppRouterConfig.QIANJI_AUTO_RECORD) })
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                            PortActionRow("导入账单", subtitle = "选择账本，导入 CSV / XLS / 钱迹 DB", onClick = { navigate(AppRouterConfig.QIANJI_IMPORT) })
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                            PortActionRow("预算", subtitle = "查看月度支出与分类额度", onClick = { navigate(AppRouterConfig.QIANJI_BUDGET) })
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                            PortActionRow("预算小部件", subtitle = "向桌面添加预算概览", onClick = { requestBudgetWidget() })
                        }
                        status?.let { TransferStatus(title = if (statusIsError) "暂时无法完成" else "提示", message = it, error = statusIsError) }
                    }
                }
            }
        }
    }

    private fun navigate(path: String) {
        status = null
        try {
            Router.with(context = this).hostAndPath(hostAndPath = path).forward()
        } catch (_: Exception) {
            statusIsError = true
            status = "页面暂时无法打开，请返回后重试。"
        }
    }

    private fun requestBudgetWidget() {
        try {
            val manager = AppWidgetManager.getInstance(this)
            if (!manager.isRequestPinAppWidgetSupported) {
                statusIsError = false
                status = "当前桌面不支持一键添加，可长按桌面，在小部件列表中选择预算小部件。"
                return
            }
            val accepted = manager.requestPinAppWidget(
                ComponentName(packageName, "com.xiaojinzi.tally.module.widget.QianjiBudgetWidgetReceiver"),
                null,
                null,
            )
            statusIsError = !accepted
            status = if (accepted) "已向桌面发送添加请求，请在系统提示中确认。" else "桌面未接受添加请求，可从桌面小部件列表手动添加。"
        } catch (_: Exception) {
            statusIsError = true
            status = "无法添加小部件，请从桌面小部件列表重试。"
        }
    }
}
