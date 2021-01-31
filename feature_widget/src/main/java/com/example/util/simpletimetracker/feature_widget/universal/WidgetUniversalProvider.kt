package com.example.util.simpletimetracker.feature_widget.universal

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.View.MeasureSpec
import android.widget.RemoteViews
import com.example.util.simpletimetracker.core.extension.getBitmapFromView
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.domain.interactor.RunningRecordInteractor
import com.example.util.simpletimetracker.feature_widget.R
import com.example.util.simpletimetracker.feature_widget.di.WidgetComponentProvider
import com.example.util.simpletimetracker.feature_widget.universal.activity.view.WidgetUniversalActivity
import com.example.util.simpletimetracker.feature_widget.universal.customView.WidgetUniversalView
import com.example.util.simpletimetracker.feature_widget.universal.mapper.WidgetUniversalViewDataMapper
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

class WidgetUniversalProvider : AppWidgetProvider() {

    @Inject
    lateinit var runningRecordInteractor: RunningRecordInteractor

    @Inject
    lateinit var recordTypeInteractor: RecordTypeInteractor

    @Inject
    lateinit var widgetUniversalViewDataMapper: WidgetUniversalViewDataMapper

    @Inject
    lateinit var prefsInteractor: PrefsInteractor

    override fun onReceive(context: Context?, intent: Intent?) {
        (context?.applicationContext as? WidgetComponentProvider)
            ?.widgetComponent
            ?.inject(this)

        super.onReceive(context, intent)
    }

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
        val recordTypes = recordTypeInteractor.getAll().map { it.id to it }.toMap()
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
        var specWidth = MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY)
        var specHeight = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY)
        entireView.measure(specWidth, specHeight)
        entireView.layout(0, 0, entireView.measuredWidth, entireView.measuredHeight)

        val imageView = entireView.findViewById<View>(R.id.ivWidgetBackground)
        width = imageView.measuredWidth
        height = imageView.measuredHeight
        specWidth = MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY)
        specHeight = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY)
        view.measure(specWidth, specHeight)
        view.layout(0, 0, view.measuredWidth, view.measuredHeight)
    }

    private fun getPendingIntent(context: Context): PendingIntent {
        val intent = Intent(context, WidgetUniversalActivity::class.java)
        return PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }
}