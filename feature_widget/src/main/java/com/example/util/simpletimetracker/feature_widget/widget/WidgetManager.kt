package com.example.util.simpletimetracker.feature_widget.widget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import com.example.util.simpletimetracker.domain.di.AppContext
import com.example.util.simpletimetracker.feature_widget.universal.WidgetUniversalProvider
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WidgetManager @Inject constructor(
    @AppContext private val context: Context
) {

    fun updateWidget(widgetId: Int) {
        val intent = Intent(context, WidgetProvider::class.java)
        intent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, intArrayOf(widgetId))
        context.sendBroadcast(intent)
    }

    fun updateWidgets() {
        val providers = listOf(WidgetProvider::class.java, WidgetUniversalProvider::class.java)

        providers.forEach { provider ->
            val intent = Intent(context, provider)
            intent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            val ids = AppWidgetManager.getInstance(context).getAppWidgetIds(ComponentName(context, provider))
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
            context.sendBroadcast(intent)
        }
    }
}