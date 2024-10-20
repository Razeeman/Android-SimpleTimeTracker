package com.example.util.simpletimetracker.feature_widget.interactor

import android.content.Context
import android.view.ContextThemeWrapper
import com.example.util.simpletimetracker.core.extension.allowVmViolations
import com.example.util.simpletimetracker.feature_views.IconView
import com.example.util.simpletimetracker.feature_views.RecordTypeView
import com.example.util.simpletimetracker.feature_widget.R
import com.example.util.simpletimetracker.feature_widget.statistics.customView.WidgetStatisticsChartView
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Used inflate on app start views that would be used for widgets.
 */
@Singleton
class WidgetViewsHolder @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private var recordTypeView: RecordTypeView? = null
    private var statisticsView: WidgetStatisticsChartView? = null
    private var statisticsRefreshView: IconView? = null

    fun initialize() {
        getRecordTypeView(context)
        getStatisticsView(context)
        getStatisticsRefreshView(context)
    }

    fun getRecordTypeView(context: Context): RecordTypeView {
        recordTypeView?.let { return it }
        val view = allowVmViolations {
            RecordTypeView(ContextThemeWrapper(context, R.style.AppTheme))
        }
        recordTypeView = view
        return view
    }

    fun getStatisticsView(context: Context): WidgetStatisticsChartView {
        statisticsView?.let { return it }
        val view = allowVmViolations {
            WidgetStatisticsChartView(ContextThemeWrapper(context, R.style.AppTheme))
        }
        statisticsView = view
        return view
    }

    fun getStatisticsRefreshView(context: Context): IconView {
        statisticsRefreshView?.let { return it }
        val view = allowVmViolations {
            IconView(ContextThemeWrapper(context, R.style.AppTheme))
        }
        statisticsRefreshView = view
        return view
    }
}