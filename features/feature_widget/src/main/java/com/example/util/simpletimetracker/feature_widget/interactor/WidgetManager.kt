package com.example.util.simpletimetracker.feature_widget.interactor

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import com.example.util.simpletimetracker.domain.model.WidgetType
import com.example.util.simpletimetracker.feature_widget.statistics.WidgetStatisticsChartProvider
import com.example.util.simpletimetracker.feature_widget.universal.WidgetUniversalProvider
import com.example.util.simpletimetracker.feature_widget.widget.WidgetProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WidgetManager @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    fun updateWidget(widgetId: Int) {
        val intent = Intent(context, WidgetProvider::class.java)
        intent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, intArrayOf(widgetId))
        context.sendBroadcast(intent)
    }

    fun updateStatisticsWidget(widgetId: Int) {
        val intent = Intent(context, WidgetStatisticsChartProvider::class.java)
        intent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, intArrayOf(widgetId))
        context.sendBroadcast(intent)
    }

    fun updateWidgets(types: List<WidgetType>) {
        val widgetsToUpdate = types
            .takeUnless { it.isEmpty() }
            ?: WidgetType.values().toList()

        val providers = widgetsToUpdate.map { type ->
            when (type) {
                WidgetType.RECORD_TYPE -> WidgetProvider::class.java
                WidgetType.UNIVERSAL -> WidgetUniversalProvider::class.java
                WidgetType.STATISTICS_CHART -> WidgetStatisticsChartProvider::class.java
            }
        }

        providers.forEach { provider ->
            val intent = Intent(context, provider)
            intent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            val ids = AppWidgetManager.getInstance(context).getAppWidgetIds(ComponentName(context, provider))
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
            context.sendBroadcast(intent)
        }
    }
}