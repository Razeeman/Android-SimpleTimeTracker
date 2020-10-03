package com.example.util.simpletimetracker.feature_widget.widget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import com.example.util.simpletimetracker.domain.di.AppContext
import com.example.util.simpletimetracker.core.manager.WidgetManager
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WidgetManagerImpl @Inject constructor(
    @AppContext private val context: Context
) : WidgetManager {

    override fun updateWidget(widgetId: Int) {
        val intent = Intent(context, WidgetProvider::class.java)
        intent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, intArrayOf(widgetId))
        context.sendBroadcast(intent)
    }

    override fun updateWidgets() {
        val intent = Intent(context, WidgetProvider::class.java)
        intent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
        val ids = AppWidgetManager.getInstance(context)
            .getAppWidgetIds(ComponentName(context, WidgetProvider::class.java))
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
        context.sendBroadcast(intent)
    }
}