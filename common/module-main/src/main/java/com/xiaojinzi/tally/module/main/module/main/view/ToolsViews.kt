package com.xiaojinzi.tally.module.main.module.main.view

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.xiaojinzi.component.impl.Router
import com.xiaojinzi.component.impl.routeApi
import com.xiaojinzi.tally.module.base.support.AppRouterConfig
import com.xiaojinzi.tally.module.base.support.AppRouterCoreApi
import com.xiaojinzi.tally.module.base.support.AppRouterMainApi
import com.xiaojinzi.tally.module.base.support.AppServices
import com.xiaojinzi.tally.module.base.theme.PortActionRow
import com.xiaojinzi.tally.module.base.theme.PortCard
import com.xiaojinzi.tally.module.base.theme.PortPage
import com.xiaojinzi.tally.module.base.theme.PortSectionTitle
import com.xiaojinzi.tally.module.main.module.setting.view.WidgetSettingsRow
import java.util.Calendar
import java.util.Locale

@Composable
internal fun ToolsView() {
    val context = LocalContext.current
    val selectedBook by AppServices.tallyDataSourceSpi.selectedBookStateOb.collectAsState(initial = null)
    val hasBook = selectedBook != null
    PortPage(
        title = "工具",
        subtitle = "管理账本与记账习惯，数据保存在本机",
    ) {
        PortSectionTitle(title = "账本管理")
        PortCard(contentPadding = PaddingValues(0.dp)) {
            PortActionRow(
                title = "账本",
                subtitle = selectedBook?.let { "当前账本：${it.name}" } ?: "选择或创建本地账本",
                onClick = { AppRouterCoreApi::class.routeApi().toBookInfoView(context = context) },
            )
            PortActionRow(
                title = "资产账户",
                subtitle = "管理现金、银行卡与其他资产账户",
                enabled = hasBook,
                onClick = { AppRouterCoreApi::class.routeApi().toAccountInfoView(context = context) },
            )
            PortActionRow(
                title = "分类",
                subtitle = "整理收入与支出的类别",
                enabled = hasBook,
                onClick = { AppRouterCoreApi::class.routeApi().toCategoryInfoView(context = context) },
            )
            PortActionRow(
                title = "标签",
                subtitle = "按用途归集账单",
                enabled = hasBook,
                onClick = { AppRouterCoreApi::class.routeApi().toLabelInfoView(context = context) },
            )
            PortActionRow(
                title = "账单相册",
                subtitle = "查看账单中的照片与凭证",
                enabled = hasBook,
                onClick = { AppRouterCoreApi::class.routeApi().toBillAlbumView(context = context) },
            )
        }

        PortSectionTitle(title = "记账工具")
        PortCard(contentPadding = PaddingValues(0.dp)) {
            PortActionRow(
                title = "自动识别",
                subtitle = "识别方式、权限与待确认账单",
                onClick = {
                    Router.with(context).hostAndPath(AppRouterConfig.QIANJI_AUTO_RECORD).forward()
                },
            )
            PortActionRow(
                title = "导入账单",
                subtitle = "从本地文件导入已有记录",
                onClick = {
                    Router.with(context).hostAndPath(AppRouterConfig.QIANJI_IMPORT).forward()
                },
            )
            PortActionRow(
                title = "月度预算",
                subtitle = "当前账本的本月总预算与分类预算",
                enabled = hasBook,
                onClick = {
                    selectedBook?.let { book ->
                        val calendar = Calendar.getInstance()
                        Router.with(context)
                            .hostAndPath(AppRouterConfig.QIANJI_BUDGET)
                            .putString("bookId", book.id)
                            .putString(
                                "month",
                                String.format(
                                    Locale.CHINA,
                                    "%04d-%02d",
                                    calendar[Calendar.YEAR],
                                    calendar[Calendar.MONTH] + 1,
                                ),
                            )
                            .forward()
                    }
                },
            )
        }

        PortSectionTitle(title = "偏好与桌面")
        PortCard(contentPadding = PaddingValues(0.dp)) {
            PortActionRow(
                title = "设置",
                subtitle = "显示、外观与记账偏好",
                onClick = { AppRouterMainApi::class.routeApi().toSettingView(context = context) },
            )
            WidgetSettingsRow()
        }
    }
}
