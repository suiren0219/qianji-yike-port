package com.xiaojinzi.tally.module.main.module.setting.view

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.FileDownload
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Widgets
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.RadioButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.xiaojinzi.component.impl.Router
import com.xiaojinzi.component.impl.routeApi
import com.xiaojinzi.reactive.template.view.BusinessContentView
import com.xiaojinzi.support.bean.StringItemDto
import com.xiaojinzi.support.compose.util.contentWithComposable
import com.xiaojinzi.support.ktx.shake
import com.xiaojinzi.support.ktx.toStringItemDto
import com.xiaojinzi.support.ktx.tryFinishActivity
import com.xiaojinzi.tally.lib.res.model.support.LocalImageItemDto
import com.xiaojinzi.tally.lib.res.model.support.rememberPainter
import com.xiaojinzi.tally.lib.res.model.support.toLocalImageItemDto
import com.xiaojinzi.tally.module.base.spi.AppWidgetSpi.PinRequestResult
import com.xiaojinzi.tally.module.base.spi.AppWidgetSpi.WidgetType
import com.xiaojinzi.tally.module.base.support.AppRouterConfig
import com.xiaojinzi.tally.module.base.support.AppRouterMainApi
import com.xiaojinzi.tally.module.base.support.AppRouterUserApi
import com.xiaojinzi.tally.module.base.support.AppServices
import com.xiaojinzi.tally.module.base.theme.PortActionRow
import com.xiaojinzi.tally.module.base.theme.PortCard
import com.xiaojinzi.tally.module.base.theme.PortDesign
import com.xiaojinzi.tally.module.base.theme.PortPage
import com.xiaojinzi.tally.module.base.theme.PortSectionTitle
import com.xiaojinzi.tally.module.base.theme.PortTheme
import com.xiaojinzi.tally.module.main.module.setting.domain.AppIconSwitcher
import kotlinx.coroutines.InternalCoroutinesApi
import java.util.Calendar
import java.util.Locale

@Composable
fun SettingSwitchView1(
    modifier: Modifier = Modifier,
    image: LocalImageItemDto,
    title: StringItemDto,
    value: Boolean,
    subtitle: String? = null,
    onCheckedChange: ((Boolean) -> Unit)?,
) {
    PortActionRow(
        modifier = modifier.toggleable(
            value = value,
            enabled = onCheckedChange != null,
            role = Role.Switch,
            onValueChange = { onCheckedChange?.invoke(it) },
        ),
        title = title.contentWithComposable(),
        subtitle = subtitle,
        icon = image.rememberPainter(),
        enabled = onCheckedChange != null,
        trailing = {
            // 整行负责操作与读屏语义，不缩小 Switch，也不创建第二个点击目标。
            Switch(checked = value, onCheckedChange = null, enabled = onCheckedChange != null)
        },
    )
}

@Composable
fun SettingActionView1(
    modifier: Modifier = Modifier,
    image: LocalImageItemDto,
    title: StringItemDto,
    onclick: () -> Unit = {},
) {
    PortActionRow(
        modifier = modifier,
        title = title.contentWithComposable(),
        icon = image.rememberPainter(),
        onClick = onclick,
    )
}

@Composable
internal fun WidgetSettingsRow() {
    val context = LocalContext.current
    var showWidgetPicker by rememberSaveable { mutableStateOf(false) }
    var widgetMessage by rememberSaveable { mutableStateOf<String?>(null) }
    PortActionRow(
        title = "桌面小部件",
        subtitle = widgetMessage ?: "在桌面查看收支与预算，快捷记账",
        icon = rememberVectorPainter(Icons.Rounded.Widgets),
        onClick = {
            widgetMessage = null
            showWidgetPicker = true
        },
    )
    if (showWidgetPicker) {
        AlertDialog(
            onDismissRequest = { showWidgetPicker = false },
            title = { Text("桌面小部件") },
            text = {
                Column {
                    Text("选择要添加到桌面的小部件")
                    listOf(
                        WidgetType.DayStatistics to "今日收支",
                        WidgetType.MonthStatistics to "月度收支",
                        WidgetType.MonthBudget to "月度预算",
                    ).forEach { (type, label) ->
                        PortActionRow(
                            title = label,
                            onClick = {
                                showWidgetPicker = false
                                val result = AppServices.appWidgetSpi?.requestPinWidget(context, type)
                                    ?: PinRequestResult.Failed
                                widgetMessage = when (result) {
                                    PinRequestResult.Unsupported -> "当前桌面不支持一键添加，请长按桌面，从小部件列表选择「$label」。"
                                    PinRequestResult.Accepted -> "已向桌面发送「$label」添加请求，请在系统提示中确认。"
                                    PinRequestResult.Rejected -> "桌面未接受添加请求，请重试，或长按桌面手动添加「$label」。"
                                    PinRequestResult.Failed -> "暂时无法请求添加，请重试，或长按桌面手动添加「$label」。"
                                }
                            },
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    modifier = Modifier.heightIn(min = PortDesign.TouchTarget),
                    onClick = { showWidgetPicker = false },
                ) {
                    Text("取消")
                }
            },
        )
    }
}

@Composable
private fun AppIconRow(
    current: AppIconSwitcher.IconOption,
    message: String?,
    onClick: () -> Unit,
) {
    PortActionRow(
        title = "应用图标",
        subtitle = message ?: "当前：${current.label} · 可随时切回原图标",
        icon = rememberVectorPainter(Icons.Rounded.Palette),
        onClick = onClick,
    )
}

@Composable
private fun AppIconPickerDialog(
    current: AppIconSwitcher.IconOption,
    onDismiss: () -> Unit,
    onSelected: (AppIconSwitcher.IconOption) -> Unit,
) {
    val context = LocalContext.current
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("应用图标") },
        text = {
            Column(modifier = Modifier.selectableGroup()) {
                AppIconSwitcher.options.forEach { option ->
                    val selected = option.id == current.id
                    val bitmap = remember(option.id) {
                        AppIconSwitcher.iconBitmap(
                            context = context,
                            option = option,
                            sizePx = (44 * context.resources.displayMetrics.density).toInt(),
                        )
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = selected,
                                role = Role.RadioButton,
                                onClick = { onSelected(option) },
                            )
                            .padding(horizontal = PortDesign.SmallGap, vertical = PortDesign.SmallGap),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        if (bitmap != null) {
                            Image(
                                bitmap = bitmap.asImageBitmap(),
                                contentDescription = null,
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(PortDesign.SmallShape),
                            )
                        }
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 12.dp),
                        ) {
                            Text(option.label, style = MaterialTheme.typography.bodyLarge)
                            Text(
                                option.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        RadioButton(selected = selected, onClick = null)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                modifier = Modifier.heightIn(min = PortDesign.TouchTarget),
                onClick = onDismiss,
            ) {
                Text("取消")
            }
        },
    )
}

@Composable
private fun SettingDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = PortDesign.CardPadding),
        color = MaterialTheme.colorScheme.outlineVariant,
    )
}

@InternalCoroutinesApi
@ExperimentalMaterial3Api
@ExperimentalAnimationApi
@ExperimentalFoundationApi
@Composable
private fun SettingView(
    needInit: Boolean? = false,
) {
    val context = LocalContext.current
    val showTime by AppServices.appConfigSpi.isShowHourAndMinuteWhenBillCrudStateOb.collectAsState(
        initial = false,
    )
    val allowZero by AppServices.appConfigSpi.isAllowZeroAmountWhenBillCrudStateOb.collectAsState(
        initial = false,
    )
    val showAssets by AppServices.appConfigSpi.isShowAssetsTabStateOb.collectAsState(initial = false)
    val showAmounts by AppServices.appConfigSpi.isHomeBillStatVisibleStateOb.collectAsState(initial = false)
    val bookSelected by AppServices.tallyDataSourceSpi.selectedBookStateOb.collectAsState(initial = null)
    val themeIndex by AppServices.appInfoSpi.themeIndexStateOb.collectAsState(initial = 0)
    var showAppearancePicker by rememberSaveable { mutableStateOf(false) }
    var showIconPicker by rememberSaveable { mutableStateOf(false) }
    var iconOption by remember { mutableStateOf(AppIconSwitcher.current(context)) }
    var iconMessage by rememberSaveable { mutableStateOf<String?>(null) }
    val vibrate by AppServices.appConfigSpi.isVibrateStateOb.collectAsState(initial = true)
    BusinessContentView<SettingViewModel>(needInit = needInit) {
        PortPage(
            title = "设置",
            subtitle = "让记账适合你的习惯",
            onBack = { context.tryFinishActivity() },
        ) {
            PortSectionTitle(title = "记账")
            PortCard(contentPadding = PaddingValues(0.dp)) {
                PortActionRow(
                    title = "自动记账",
                    subtitle = "识别方式、权限与待确认账单",
                    icon = painterResource(com.xiaojinzi.tally.lib.res.R.drawable.res_robot1),
                    onClick = {
                        Router.with(context).hostAndPath(AppRouterConfig.QIANJI_AUTO_RECORD).forward()
                    },
                )
                SettingDivider()
                PortActionRow(
                    title = "导入账单",
                    subtitle = "把已有记录带入当前记账流程",
                    icon = rememberVectorPainter(Icons.Rounded.FileDownload),
                    onClick = {
                        Router.with(context).hostAndPath(AppRouterConfig.QIANJI_IMPORT).forward()
                    },
                )
                SettingDivider()
                PortActionRow(
                    title = "月度预算",
                    subtitle = "当前账本的本月总预算与分类预算",
                    icon = rememberVectorPainter(Icons.Rounded.AccountBalanceWallet),
                    enabled = bookSelected != null,
                    onClick = {
                        if (bookSelected != null) {
                            val calendar = Calendar.getInstance()
                            Router.with(context = context)
                                .hostAndPath(hostAndPath = AppRouterConfig.QIANJI_BUDGET)
                                .putString("bookId", bookSelected?.id)
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

            PortSectionTitle(title = "偏好")
            PortCard(contentPadding = PaddingValues(0.dp)) {
                SettingSwitchView1(
                    image = com.xiaojinzi.tally.lib.res.R.drawable.res_shake1.toLocalImageItemDto(),
                    title = "数字键盘振动反馈".toStringItemDto(),
                    value = vibrate,
                ) {
                    if (it) shake()
                    AppServices.appConfigSpi.switchVibrate(isVibrate = it)
                }
                SettingDivider()
                SettingSwitchView1(
                    image = com.xiaojinzi.tally.lib.res.R.drawable.res_clock1.toLocalImageItemDto(),
                    title = "记账精确到时分".toStringItemDto(),
                    value = showTime,
                ) {
                    AppServices.appConfigSpi.switchShowHourAndMinuteWhenBillCrud(b = it)
                }
                SettingDivider()
                SettingSwitchView1(
                    image = com.xiaojinzi.tally.lib.res.R.drawable.res_zero1.toLocalImageItemDto(),
                    title = "允许零金额账单".toStringItemDto(),
                    value = allowZero,
                ) {
                    AppServices.appConfigSpi.switchAllowZeroAmountWhenBillCrudStateOb(b = it)
                }
                SettingDivider()
                SettingSwitchView1(
                    image = com.xiaojinzi.tally.lib.res.R.drawable.res_account1.toLocalImageItemDto(),
                    title = "显示资产导航".toStringItemDto(),
                    subtitle = "在底栏查看账户与资产",
                    value = showAssets,
                ) {
                    AppServices.appConfigSpi.switchShowAssetsStateOb(b = it)
                }
                SettingDivider()
                SettingSwitchView1(
                    image = com.xiaojinzi.tally.lib.res.R.drawable.res_eye1.toLocalImageItemDto(),
                    title = "显示首页金额".toStringItemDto(),
                    subtitle = "同时控制收支概览与预算信息",
                    value = showAmounts,
                ) {
                    AppServices.appConfigSpi.switchHomeBillStatVisible(b = it)
                }
                SettingDivider()
                PortActionRow(
                    title = "外观",
                    subtitle = when (themeIndex) {
                        1 -> "浅色"
                        2 -> "深色"
                        else -> "跟随系统"
                    },
                    icon = painterResource(com.xiaojinzi.tally.lib.res.R.drawable.res_theme1),
                    onClick = { showAppearancePicker = true },
                )
                SettingDivider()
                SettingActionView1(
                    image = com.xiaojinzi.tally.lib.res.R.drawable.res_theme1.toLocalImageItemDto(),
                    title = "主题配色".toStringItemDto(),
                ) {
                    AppRouterMainApi::class.routeApi().toThemeSelectView(context = context)
                }
            }

            PortSectionTitle(title = "应用")
            PortCard(contentPadding = PaddingValues(0.dp)) {
                WidgetSettingsRow()
                SettingDivider()
                AppIconRow(
                    current = iconOption,
                    message = iconMessage,
                    onClick = {
                        iconMessage = null
                        showIconPicker = true
                    },
                )
                SettingDivider()
                SettingActionView1(
                    image = com.xiaojinzi.tally.lib.res.R.drawable.res_warn1.toLocalImageItemDto(),
                    title = "关于与开源许可".toStringItemDto(),
                ) {
                    AppRouterUserApi::class.routeApi().toAboutUsView(context = context)
                }
            }

        }
        if (showAppearancePicker) {
            AlertDialog(
                onDismissRequest = { showAppearancePicker = false },
                title = { Text("外观") },
                text = {
                    Column(modifier = Modifier.selectableGroup()) {
                        listOf(0 to "跟随系统", 1 to "浅色", 2 to "深色").forEach { (index, label) ->
                            val selected = if (index == 0) themeIndex !in 1..2 else themeIndex == index
                            PortActionRow(
                                modifier = Modifier.selectable(
                                    selected = selected,
                                    role = Role.RadioButton,
                                    onClick = {
                                        AppServices.appInfoSpi.switchTheme(themeIndex = index)
                                        showAppearancePicker = false
                                    },
                                ),
                                title = label,
                                trailing = { RadioButton(selected = selected, onClick = null) },
                            )
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        modifier = Modifier.heightIn(min = PortDesign.TouchTarget),
                        onClick = { showAppearancePicker = false },
                    ) {
                        Text("完成")
                    }
                },
            )
        }
        if (showIconPicker) {
            AppIconPickerDialog(
                current = iconOption,
                onDismiss = { showIconPicker = false },
                onSelected = { option ->
                    val switched = AppIconSwitcher.switchTo(context, option)
                    iconOption = AppIconSwitcher.current(context)
                    iconMessage = if (switched) {
                        "已切换到「${option.label}」图标，桌面可能需要几秒刷新"
                    } else {
                        "图标切换失败，请稍后重试"
                    }
                    showIconPicker = false
                },
            )
        }
    }
}

@InternalCoroutinesApi
@ExperimentalMaterial3Api
@ExperimentalAnimationApi
@ExperimentalFoundationApi
@Composable
fun SettingViewWrap() {
    PortTheme { SettingView() }
}

@InternalCoroutinesApi
@ExperimentalMaterial3Api
@ExperimentalAnimationApi
@ExperimentalFoundationApi
@Preview
@Composable
private fun SettingViewPreview() {
    PortTheme { SettingView(needInit = false) }
}
