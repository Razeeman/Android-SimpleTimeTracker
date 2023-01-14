package com.example.util.simpletimetracker.feature_widget.universal

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.widget.RemoteViews
import com.example.util.simpletimetracker.core.utils.PendingIntents
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.domain.interactor.RunningRecordInteractor
import com.example.util.simpletimetracker.feature_views.extension.getBitmapFromView
import com.example.util.simpletimetracker.feature_views.extension.measureExactly
import com.example.util.simpletimetracker.feature_widget.R
import com.example.util.simpletimetracker.feature_widget.universal.activity.view.WidgetUniversalActivity
import com.example.util.simpletimetracker.feature_widget.universal.customView.WidgetUniversalView
import com.example.util.simpletimetracker.feature_widget.universal.mapper.WidgetUniversalViewDataMapper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@AndroidEntryPoint
class WidgetUniversalProvider : AppWidgetProvider() {

    @Inject
    lateinit var runningRecordInteractor: RunningRecordInteractor

    @Inject
    lateinit var recordTypeInteractor: RecordTypeInteractor

    @Inject
    lateinit var widgetUniversalViewDataMapper: WidgetUniversalViewDataMapper

    @Inject
    lateinit var prefsInteractor: PrefsInteractor

    override fun onUpdate(
        context: Context?,
        appWidgetManager: AppWidgetManager?,
        appWidgetIds: IntArray?
    ) {
        appWidgetIds?.forEach { widgetId ->
            updateAppWidget(context, appWidgetManager, widgetId)
        }
    }

    private fun updateAppWidget(
        context: Context?,
        appWidgetManager: AppWidgetManager?,
        appWidgetId: Int
    ) {
        if (context == null || appWidgetManager == null) return

        val view = prepareView(context)
        measureView(context, view)
        val bitmap = view.getBitmapFromView()

        val views = RemoteViews(context.packageName, R.layout.widget_layout)
        views.setImageViewBitmap(R.id.ivWidgetBackground, bitmap)
        views.setOnClickPendingIntent(R.id.btnWidget, getPendingIntent(context))

        appWidgetManager.updateAppWidget(appWidgetId, views)
    }

    private fun prepareView(
        context: Context
    ): View = runBlocking {
        val runningRecords = runningRecordInteractor.getAll()
        val recordTypes = recordTypeInteractor.getAll().associateBy { it.id }
        val isDarkTheme = prefsInteractor.getDarkMode()

        val data = runningRecords
            .let { widgetUniversalViewDataMapper.mapToWidgetViewData(it, recordTypes, isDarkTheme) }
            .takeUnless { it.data.isEmpty() }
            ?: widgetUniversalViewDataMapper.mapToEmptyWidgetViewData()

        WidgetUniversalView(ContextThemeWrapper(context, R.style.AppTheme)).apply {
            setData(data)
        }
    }

    private fun measureView(context: Context, view: View) {
        var width = context.resources.getDimensionPixelSize(R.dimen.widget_width)
        var height = context.resources.getDimensionPixelSize(R.dimen.widget_height)
        val inflater = LayoutInflater.from(context)

        val entireView: View = inflater.inflate(R.layout.widget_layout, null)
        entireView.measureExactly(width = width, height = height)

        val imageView = entireView.findViewById<View>(R.id.ivWidgetBackground)
        width = imageView.measuredWidth
        height = imageView.measuredHeight
        view.measureExactly(width = width, height = height)
    }

    private fun getPendingIntent(context: Context): PendingIntent {
        val intent = Intent(context, WidgetUniversalActivity::class.java)
        return PendingIntent.getActivity(context, 0, intent, PendingIntents.getFlags())
    }
}