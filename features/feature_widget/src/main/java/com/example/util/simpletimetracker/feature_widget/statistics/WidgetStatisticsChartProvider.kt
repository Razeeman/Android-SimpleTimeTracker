package com.example.util.simpletimetracker.feature_widget.statistics

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.os.Bundle
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.widget.RemoteViews
import com.example.util.simpletimetracker.core.interactor.StatisticsChartViewDataInteractor
import com.example.util.simpletimetracker.core.interactor.StatisticsMediator
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.core.utils.PendingIntents
import com.example.util.simpletimetracker.core.utils.SHORTCUT_NAVIGATION_KEY
import com.example.util.simpletimetracker.core.utils.SHORTCUT_NAVIGATION_STATISTICS
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.domain.model.ChartFilterType
import com.example.util.simpletimetracker.feature_views.extension.dpToPx
import com.example.util.simpletimetracker.feature_views.extension.getBitmapFromView
import com.example.util.simpletimetracker.feature_views.extension.measureExactly
import com.example.util.simpletimetracker.feature_views.extension.pxToDp
import com.example.util.simpletimetracker.feature_widget.R
import com.example.util.simpletimetracker.feature_widget.statistics.customView.WidgetStatisticsChartView
import com.example.util.simpletimetracker.navigation.Router
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@AndroidEntryPoint
class WidgetStatisticsChartProvider : AppWidgetProvider() {

    @Inject
    lateinit var statisticsChartViewDataInteractor: StatisticsChartViewDataInteractor

    @Inject
    lateinit var statisticsMediator: StatisticsMediator

    @Inject
    lateinit var router: Router

    @Inject
    lateinit var prefsInteractor: PrefsInteractor

    @Inject
    lateinit var recordTypeInteractor: RecordTypeInteractor

    @Inject
    lateinit var resourceRepo: ResourceRepo

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
        CoroutineScope(Dispatchers.Main).launch {
            appWidgetIds?.forEach { prefsInteractor.removeStatisticsWidget(it) }
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

        val view = prepareView(context, appWidgetId)
        val options = appWidgetManager.getAppWidgetOptions(appWidgetId)
        measureView(context, options, view)
        val bitmap = view.getBitmapFromView()

        val views = RemoteViews(context.packageName, R.layout.widget_layout)
        views.setImageViewBitmap(R.id.ivWidgetBackground, bitmap)
        views.setOnClickPendingIntent(R.id.btnWidget, getPendingSelfIntent(context))

        appWidgetManager.updateAppWidget(appWidgetId, views)
    }

    private fun prepareView(
        context: Context,
        appWidgetId: Int,
    ): View = runBlocking {
        // TODO remove blocking
        val isDarkTheme = prefsInteractor.getDarkMode()
        val useProportionalMinutes = prefsInteractor.getUseProportionalMinutes()
        val showSeconds = prefsInteractor.getShowSeconds()
        val widgetData = prefsInteractor.getStatisticsWidget(appWidgetId)
        val types = recordTypeInteractor.getAll()

        val filterType = widgetData.chartFilterType
        val filteredIds = when (filterType) {
            ChartFilterType.ACTIVITY -> widgetData.filteredTypes
            ChartFilterType.CATEGORY -> widgetData.filteredCategories
        }.toList()
        val rangeLength = widgetData.rangeLength

        val dataHolders = statisticsMediator.getDataHolders(
            filterType = filterType,
            types = types
        )
        val statistics = statisticsMediator.getStatistics(
            filterType = filterType,
            filteredIds = filteredIds,
            rangeLength = rangeLength,
            shift = 0
        )
        val chart = statisticsChartViewDataInteractor.getChart(
            filterType = filterType,
            filteredIds = filteredIds,
            statistics = statistics,
            dataHolders = dataHolders,
            types = types,
            isDarkTheme = isDarkTheme
        )
        val total: String = statisticsMediator.getStatisticsTotalTracked(
            statistics = statistics,
            filteredIds = filteredIds,
            useProportionalMinutes = useProportionalMinutes,
            showSeconds = showSeconds,
        )
        val totalTracked = resourceRepo.getString(R.string.statistics_total_tracked_short) +
            "\n" + total

        WidgetStatisticsChartView(ContextThemeWrapper(context, R.style.AppTheme)).apply {
            setSegments(chart, totalTracked)
        }
    }

    private fun measureView(
        context: Context,
        options: Bundle,
        view: View,
    ) {
        val defaultWidth = context.resources.getDimensionPixelSize(R.dimen.record_type_card_width).pxToDp()
        val defaultHeight = context.resources.getDimensionPixelSize(R.dimen.record_type_card_height).pxToDp()

        var width = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, defaultWidth)
            .dpToPx().takeUnless { it == 0 } ?: defaultWidth
        var height = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT, defaultHeight)
            .dpToPx().takeUnless { it == 0 } ?: defaultHeight
        val inflater = LayoutInflater.from(context)

        val entireView: View = inflater.inflate(R.layout.widget_layout, null)
        entireView.measureExactly(width = width, height = height)

        val imageView = entireView.findViewById<View>(R.id.ivWidgetBackground)
        width = imageView.measuredWidth
        height = imageView.measuredHeight
        view.measureExactly(width = width, height = height)
    }

    private fun getPendingSelfIntent(
        context: Context,
    ): PendingIntent {
        val intent = router.getMainStartIntent().apply {
            putExtra(SHORTCUT_NAVIGATION_KEY, SHORTCUT_NAVIGATION_STATISTICS)
        }
        return PendingIntent.getActivity(context, 0, intent, PendingIntents.getFlags())
    }
}