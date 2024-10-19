package com.example.util.simpletimetracker.feature_widget.universal

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.widget.RemoteViews
import com.example.util.simpletimetracker.core.extension.allowVmViolations
import com.example.util.simpletimetracker.core.utils.PendingIntents
import com.example.util.simpletimetracker.domain.extension.orZero
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.domain.interactor.RunningRecordInteractor
import com.example.util.simpletimetracker.domain.model.RunningRecord
import com.example.util.simpletimetracker.feature_views.extension.getBitmapFromView
import com.example.util.simpletimetracker.feature_views.extension.measureExactly
import com.example.util.simpletimetracker.feature_widget.R
import com.example.util.simpletimetracker.feature_widget.universal.activity.view.WidgetUniversalActivity
import com.example.util.simpletimetracker.feature_widget.universal.customView.WidgetUniversalView
import com.example.util.simpletimetracker.feature_widget.universal.customView.WidgetUniversalViewData
import com.example.util.simpletimetracker.feature_widget.universal.mapper.WidgetUniversalViewDataMapper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
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
        appWidgetIds: IntArray?,
    ) {
        appWidgetIds?.forEach { widgetId ->
            updateAppWidget(context, appWidgetManager, widgetId)
        }
    }

    private fun updateAppWidget(
        context: Context?,
        appWidgetManager: AppWidgetManager?,
        appWidgetId: Int,
    ) {
        if (context == null || appWidgetManager == null) return

        GlobalScope.launch(Dispatchers.Main) {
            val runningRecords: List<RunningRecord> = runningRecordInteractor.getAll()
            val recordTypes = recordTypeInteractor.getAll().associateBy { it.id }
            val isDarkTheme = prefsInteractor.getDarkMode()
            val backgroundTransparency = prefsInteractor.getWidgetBackgroundTransparencyPercent()

            val data = runningRecords
                .let {
                    widgetUniversalViewDataMapper.mapToWidgetViewData(
                        runningRecords = it,
                        recordTypes = recordTypes,
                        isDarkTheme = isDarkTheme,
                        backgroundTransparency = backgroundTransparency,
                    )
                }
                .takeUnless { it.data.isEmpty() }
                ?: widgetUniversalViewDataMapper.mapToEmptyWidgetViewData(
                    backgroundTransparency = backgroundTransparency,
                )

            val view = prepareView(context, data)
            measureView(context, view)
            val bitmap = view.getBitmapFromView()

            val views = RemoteViews(context.packageName, R.layout.widget_layout)
            if (data.data.size > 2) {
                views.setViewVisibility(R.id.timerWidget, View.GONE)
                views.setViewVisibility(R.id.timerWidget2, View.GONE)
            } else {
                setChronometer(runningRecords.getOrNull(0), R.id.timerWidget, views)
                setChronometer(runningRecords.getOrNull(1), R.id.timerWidget2, views)
            }
            views.setImageViewBitmap(R.id.ivWidgetBackground, bitmap)
            views.setOnClickPendingIntent(R.id.btnWidget, getPendingIntent(context))

            runCatching {
                appWidgetManager.updateAppWidget(appWidgetId, views)
            }
        }
    }

    private fun prepareView(
        context: Context,
        data: WidgetUniversalViewData,
    ): View {
        return allowVmViolations {
            WidgetUniversalView(ContextThemeWrapper(context, R.style.AppTheme))
        }.apply {
            setData(data)
        }
    }

    private fun setChronometer(
        runningRecord: RunningRecord?,
        chronometerId: Int,
        views: RemoteViews,
    ) {
        if (runningRecord != null) {
            val timeStarted = runningRecord.timeStarted.orZero()
            val base = SystemClock.elapsedRealtime() - (System.currentTimeMillis() - timeStarted)
            views.setChronometer(chronometerId, base, null, true)
            views.setViewVisibility(chronometerId, View.VISIBLE)
        } else {
            views.setViewVisibility(chronometerId, View.GONE)
        }
    }

    private fun measureView(context: Context, view: View) {
        var width = context.resources.getDimensionPixelSize(R.dimen.widget_width)
        var height = context.resources.getDimensionPixelSize(R.dimen.widget_height)
        val inflater = LayoutInflater.from(context)

        val entireView: View = allowVmViolations { inflater.inflate(R.layout.widget_layout, null) }
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