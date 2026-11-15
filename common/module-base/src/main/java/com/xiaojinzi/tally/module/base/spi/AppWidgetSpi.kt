package com.xiaojinzi.tally.module.base.spi

import android.content.Context

interface AppWidgetSpi {

    enum class WidgetType { DayStatistics, MonthStatistics, MonthBudget }

    enum class PinRequestResult { Unsupported, Accepted, Rejected, Failed }

    /** Accepted 仅表示桌面接受请求，是否完成添加由用户在系统提示中确认。 */
    fun requestPinWidget(context: Context, type: WidgetType): PinRequestResult

    /**
     * 启动 App 内小部件的更新服务
     * 如果小部件有被使用
     */
    suspend fun startWidgetServiceIfUsed()

    /**
     * 尝试更新一下小部件
     */
    fun tryUpdateWidget()

}