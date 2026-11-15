package com.xiaojinzi.tally.module.widget.spi

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import com.xiaojinzi.component.anno.ServiceAnno
import com.xiaojinzi.support.ktx.app
import com.xiaojinzi.tally.module.base.spi.AppWidgetSpi
import com.xiaojinzi.tally.module.base.spi.AppWidgetSpi.PinRequestResult
import com.xiaojinzi.tally.module.base.spi.AppWidgetSpi.WidgetType
import com.xiaojinzi.tally.module.widget.AppDayStatisticsWidgetReceiver
import com.xiaojinzi.tally.module.widget.AppMonthStatisticsWidgetReceiver
import com.xiaojinzi.tally.module.widget.QianjiBudgetWidgetReceiver
import com.xiaojinzi.tally.module.widget.WidgetRefreshScheduler

@ServiceAnno(AppWidgetSpi::class)
class AppWidgetSpiImpl : AppWidgetSpi {
    override fun requestPinWidget(context: Context, type: WidgetType): PinRequestResult {
        return try {
            val manager = AppWidgetManager.getInstance(context)
            if (!manager.isRequestPinAppWidgetSupported) {
                return PinRequestResult.Unsupported
            }
            val receiver = when (type) {
                WidgetType.DayStatistics -> AppDayStatisticsWidgetReceiver::class.java
                WidgetType.MonthStatistics -> AppMonthStatisticsWidgetReceiver::class.java
                WidgetType.MonthBudget -> QianjiBudgetWidgetReceiver::class.java
            }
            if (manager.requestPinAppWidget(ComponentName(context, receiver), null, null)) {
                PinRequestResult.Accepted
            } else {
                PinRequestResult.Rejected
            }
        } catch (_: Exception) {
            PinRequestResult.Failed
        }
    }

    // Preserve the SPI contract; the historic name no longer means starting a service.
    override suspend fun startWidgetServiceIfUsed() {
        WidgetRefreshScheduler.requestRefresh(app)
    }

    override fun tryUpdateWidget() {
        WidgetRefreshScheduler.requestRefresh(app)
    }
}
