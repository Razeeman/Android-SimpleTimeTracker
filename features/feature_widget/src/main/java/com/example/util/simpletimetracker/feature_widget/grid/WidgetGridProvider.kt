package com.example.util.simpletimetracker.feature_widget.grid

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.example.util.simpletimetracker.core.extension.allowDiskRead
import com.example.util.simpletimetracker.domain.prefs.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.widget.interactor.WidgetInteractor
import com.example.util.simpletimetracker.feature_widget.common.WidgetTypeClickManager
import com.example.util.simpletimetracker.feature_widget.utils.getAppWidgetIdOrNull
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class WidgetGridProvider : AppWidgetProvider() {

    @Inject
    lateinit var widgetTypeClickManager: WidgetTypeClickManager

    @Inject
    lateinit var widgetInteractor: WidgetInteractor

    @Inject
    lateinit var widgetGridRemoveViewsFactory: WidgetGridRemoveViewsFactory

    @Inject
    lateinit var prefsInteractor: PrefsInteractor

    override fun onReceive(context: Context?, intent: Intent?) {
        super.onReceive(context, intent)
        allowDiskRead { MainScope() }.launch {
            when (intent?.action) {
                ITEM_CLICK_ACTION -> onClick(context, intent)
                CONTROLS_NEW_PAGE -> onNewPage(intent)
            }
        }
    }

    override fun onUpdate(
        context: Context?,
        appWidgetManager: AppWidgetManager?,
        appWidgetIds: IntArray?,
    ) {
        appWidgetIds?.forEach { widgetId ->
            updateAppWidget(context, appWidgetManager, widgetId)
        }
    }

    override fun onDeleted(context: Context?, appWidgetIds: IntArray?) {
        allowDiskRead { MainScope() }.launch {
            appWidgetIds?.forEach { prefsInteractor.removeGridWidget(it) }
        }
    }

    override fun onAppWidgetOptionsChanged(
        context: Context?,
        appWidgetManager: AppWidgetManager?,
        appWidgetId: Int,
        newOptions: Bundle?,
    ) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions)
        updateAppWidget(context, appWidgetManager, appWidgetId)
    }

    private fun updateAppWidget(
        context: Context?,
        appWidgetManager: AppWidgetManager?,
        appWidgetId: Int,
    ) {
        if (context == null || appWidgetManager == null) return

        allowDiskRead { MainScope() }.launch {
            val views = widgetGridRemoveViewsFactory.getView(
                context = context,
                appWidgetManager = appWidgetManager,
                appWidgetId = appWidgetId,
            )
            runCatching {
                appWidgetManager.updateAppWidget(appWidgetId, views)
            }
        }
    }

    private suspend fun onClick(
        context: Context?,
        intent: Intent,
    ) {
        val appWidgetId = intent.getAppWidgetIdOrNull() ?: return
        widgetTypeClickManager.onClick(
            context = context,
            recordTypeId = intent.getLongExtra(TYPE_ID_EXTRA, 0),
            onWidgetUpdate = { widgetInteractor.updateGridWidget(appWidgetId) },
        )
    }

    private suspend fun onNewPage(
        intent: Intent,
    ) {
        val appWidgetId = intent.getAppWidgetIdOrNull() ?: return
        val page = intent.getIntExtra(NEW_PAGE_EXTRA, 0)
        prefsInteractor.setGridWidget(appWidgetId, page)
        widgetInteractor.updateGridWidget(appWidgetId)
    }

    companion object {
        const val TYPE_ID_EXTRA =
            "com.example.util.simpletimetracker.feature_widget.grid.typeIdExtra"
        const val NEW_PAGE_EXTRA =
            "com.example.util.simpletimetracker.feature_widget.grid.pageExtra"
        const val ITEM_CLICK_ACTION =
            "com.example.util.simpletimetracker.feature_widget.grid.ITEM_CLICK_ACTION"
        const val CONTROLS_NEW_PAGE =
            "com.example.util.simpletimetracker.feature_widget.grid.CONTROLS_NEW_PAGE"
    }
}