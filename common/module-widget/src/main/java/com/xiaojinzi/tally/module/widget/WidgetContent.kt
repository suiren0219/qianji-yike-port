package com.xiaojinzi.tally.module.widget

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.LocalSize
import androidx.glance.action.Action
import androidx.glance.action.clickable
import androidx.glance.appwidget.LinearProgressIndicator
import androidx.glance.appwidget.SizeMode
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider

/** Glance equivalents of PortDesign: 24dp cards, 6dp progress and the same day/night palette. */
internal object WidgetDesign {
    val sizes = SizeMode.Responsive(
        setOf(DpSize(110.dp, 56.dp), DpSize(140.dp, 160.dp), DpSize(300.dp, 160.dp)),
    )
    val text = ColorProvider(R.color.widget_text)
    val secondary = ColorProvider(R.color.widget_secondary)
    val accent = ColorProvider(R.color.widget_accent)
    val onAccent = ColorProvider(R.color.widget_on_accent)
    val track = ColorProvider(R.color.widget_track)
    val error = ColorProvider(R.color.widget_error)
}

internal enum class WidgetKind { Budget, Day, Month }

@Composable
internal fun WidgetContent(snapshot: WidgetSnapshot, kind: WidgetKind) {
    val context = LocalContext.current
    val size = LocalSize.current
    val expanded = size.width >= 300.dp
    val short = size.height < 160.dp
    val budgetAction = WidgetActionActivity.action(context, WidgetActionActivity.BUDGET, snapshot)
    val addAction = WidgetActionActivity.action(context, WidgetActionActivity.ADD_BILL, snapshot)
    val appAction = WidgetActionActivity.action(context, WidgetActionActivity.OPEN_APP, snapshot)
    val title = when (kind) {
        WidgetKind.Budget -> "${snapshot.monthLabel}预算"
        WidgetKind.Day -> "今日收支"
        WidgetKind.Month -> "${snapshot.monthLabel}收支"
    }
    Column(
        modifier = GlanceModifier.fillMaxSize()
            .background(ImageProvider(R.drawable.widget_bg1))
            .padding(if (short) 8.dp else 12.dp),
    ) {
        if (snapshot.status != WidgetStatus.Ready) {
            Column(modifier = GlanceModifier.fillMaxSize().clickable(appAction)) {
                WidgetText(title, 12, color = WidgetDesign.accent)
                Spacer(GlanceModifier.defaultWeight())
                WidgetText(snapshot.statusText, if (short) 11 else 13, maxLines = 2)
                if (!short) {
                    Spacer(GlanceModifier.height(8.dp))
                    WidgetButton("打开应用", appAction, GlanceModifier.fillMaxWidth())
                }
            }
        } else if (short) {
            // Keep previously bound very small IDs useful; new pins default to 2x2.
            Row(modifier = GlanceModifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = GlanceModifier.defaultWeight().clickable(budgetAction)) {
                    WidgetText(title, 10, color = WidgetDesign.secondary)
                    WidgetText(snapshot.amount(if (kind == WidgetKind.Budget) snapshot.remaining else spending(snapshot, kind)), 15, bold = true)
                }
                Box(
                    modifier = GlanceModifier.width(40.dp).height(40.dp).clickable(addAction),
                    contentAlignment = Alignment.Center,
                ) { WidgetText("记一笔", 11, color = WidgetDesign.accent) }
            }
        } else {
            Row(modifier = GlanceModifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                WidgetText(title, 12, bold = true, color = WidgetDesign.accent)
                Spacer(GlanceModifier.width(6.dp))
                WidgetText(snapshot.bookName, 10, modifier = GlanceModifier.defaultWeight(), color = WidgetDesign.secondary)
            }
            Spacer(GlanceModifier.height(4.dp))
            if (kind == WidgetKind.Budget) {
                BudgetBody(snapshot, expanded, budgetAction, GlanceModifier.defaultWeight())
            } else {
                StatisticsBody(snapshot, kind, expanded, GlanceModifier.defaultWeight())
            }
            Row(modifier = GlanceModifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                WidgetButton("预算", budgetAction, GlanceModifier.defaultWeight(), primary = false)
                Spacer(GlanceModifier.width(8.dp))
                WidgetButton("记一笔", addAction, GlanceModifier.defaultWeight())
            }
        }
    }
}

@Composable
private fun BudgetBody(snapshot: WidgetSnapshot, expanded: Boolean, action: Action, modifier: GlanceModifier) {
    Column(modifier = modifier.fillMaxWidth().clickable(action)) {
        val hidden = !snapshot.amountsVisible
        val noBudget = !hidden && (snapshot.budget ?: 0L) <= 0L
        val label = when {
            hidden -> if (expanded) "预算剩余" else "剩余"
            noBudget -> "未设预算"
            snapshot.overBudget -> "已超支"
            else -> if (expanded) "预算剩余" else "剩余"
        }
        Row(modifier = GlanceModifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            WidgetText(label, 11, color = WidgetDesign.secondary)
            Spacer(GlanceModifier.width(6.dp))
            WidgetText(
                if (noBudget) "去设置" else snapshot.amount(snapshot.remaining?.let { if (it < 0) -it else it }),
                if (expanded) 22 else 18,
                modifier = GlanceModifier.defaultWeight(),
                bold = true,
                color = if (snapshot.overBudget) WidgetDesign.error else WidgetDesign.text,
            )
        }
        if (hidden) {
            WidgetText("金额与进度已隐藏", 10, color = WidgetDesign.secondary)
        } else if (!noBudget) {
            WidgetText("总预算 ${snapshot.amount(snapshot.budget)}", 10, color = WidgetDesign.secondary)
            LinearProgressIndicator(
                progress = snapshot.progress ?: 0f,
                modifier = GlanceModifier.fillMaxWidth().height(6.dp),
                color = if (snapshot.overBudget) WidgetDesign.error else WidgetDesign.accent,
                backgroundColor = WidgetDesign.track,
            )
        } else {
            WidgetText("点此安排本月额度", 10, color = WidgetDesign.secondary)
        }
        Spacer(GlanceModifier.defaultWeight())
        Row(modifier = GlanceModifier.fillMaxWidth()) {
            Metric("支出", snapshot.amount(snapshot.monthSpending), GlanceModifier.defaultWeight(), expanded)
            Spacer(GlanceModifier.width(8.dp))
            Metric("收入", snapshot.amount(snapshot.monthIncome), GlanceModifier.defaultWeight(), expanded)
        }
    }
}

@Composable
private fun StatisticsBody(snapshot: WidgetSnapshot, kind: WidgetKind, expanded: Boolean, modifier: GlanceModifier) {
    val spent = spending(snapshot, kind)
    val income = if (kind == WidgetKind.Day) snapshot.dayIncome else snapshot.monthIncome
    Column(modifier = modifier.fillMaxWidth()) {
        WidgetText("支出 ${snapshot.amount(spent)}", if (expanded) 24 else 20, bold = true)
        Spacer(GlanceModifier.defaultWeight())
        Row(modifier = GlanceModifier.fillMaxWidth()) {
            Metric("收入", snapshot.amount(income), GlanceModifier.defaultWeight(), expanded)
            Spacer(GlanceModifier.width(8.dp))
            Metric("结余", snapshot.amount(if (spent != null && income != null) income - spent else null), GlanceModifier.defaultWeight(), expanded)
        }
    }
}

private fun spending(snapshot: WidgetSnapshot, kind: WidgetKind) =
    if (kind == WidgetKind.Day) snapshot.daySpending else snapshot.monthSpending

@Composable
private fun Metric(label: String, value: String, modifier: GlanceModifier, expanded: Boolean) {
    if (expanded) {
        Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
            WidgetText(label, 11, color = WidgetDesign.secondary)
            Spacer(GlanceModifier.width(6.dp))
            WidgetText(value, 14, bold = true, modifier = GlanceModifier.defaultWeight())
        }
    } else {
        Column(modifier = modifier) {
            WidgetText(label, 10, color = WidgetDesign.secondary)
            WidgetText(value, 11, bold = true)
        }
    }
}

@Composable
private fun WidgetButton(label: String, action: Action, modifier: GlanceModifier, primary: Boolean = true) {
    Box(
        modifier = modifier.height(40.dp)
            .background(ImageProvider(if (primary) R.drawable.widget_action_primary else R.drawable.widget_action_secondary))
            .clickable(action),
        contentAlignment = Alignment.Center,
    ) {
        WidgetText(label, 12, bold = true, color = if (primary) WidgetDesign.onAccent else WidgetDesign.accent)
    }
}

@Composable
private fun WidgetText(
    text: String,
    size: Int,
    modifier: GlanceModifier = GlanceModifier,
    color: ColorProvider = WidgetDesign.text,
    bold: Boolean = false,
    maxLines: Int = 1,
) {
    Text(
        text = text,
        modifier = modifier,
        maxLines = maxLines,
        style = TextStyle(color = color, fontSize = size.sp, fontWeight = if (bold) FontWeight.Medium else FontWeight.Normal),
    )
}
