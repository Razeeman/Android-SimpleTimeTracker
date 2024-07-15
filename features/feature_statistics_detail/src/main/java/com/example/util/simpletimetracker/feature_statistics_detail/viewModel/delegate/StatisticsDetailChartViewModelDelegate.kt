package com.example.util.simpletimetracker.feature_statistics_detail.viewModel.delegate

import androidx.lifecycle.LiveData
import com.example.util.simpletimetracker.core.base.ViewModelDelegate
import com.example.util.simpletimetracker.core.extension.lazySuspend
import com.example.util.simpletimetracker.core.extension.set
import com.example.util.simpletimetracker.core.view.buttonsRowView.ButtonsRowViewData
import com.example.util.simpletimetracker.feature_statistics_detail.interactor.StatisticsDetailChartInteractor
import com.example.util.simpletimetracker.feature_statistics_detail.model.ChartGrouping
import com.example.util.simpletimetracker.feature_statistics_detail.model.ChartLength
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailChartCompositeViewData
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailChartLengthViewData
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailGroupingViewData
import kotlinx.coroutines.launch
import javax.inject.Inject

class StatisticsDetailChartViewModelDelegate @Inject constructor(
    private val chartInteractor: StatisticsDetailChartInteractor,
) : StatisticsDetailViewModelDelegate, ViewModelDelegate() {

    val viewData: LiveData<StatisticsDetailChartCompositeViewData> by lazySuspend {
        loadEmptyViewData().also { parent?.updateContent() }
    }

    private var parent: StatisticsDetailViewModelDelegate.Parent? = null
    private var chartGrouping: ChartGrouping = ChartGrouping.DAILY
    private var chartLength: ChartLength = ChartLength.TEN

    override fun attach(parent: StatisticsDetailViewModelDelegate.Parent) {
        this.parent = parent
    }

    fun onChartGroupingClick(viewData: ButtonsRowViewData) {
        if (viewData !is StatisticsDetailGroupingViewData) return
        this.chartGrouping = viewData.chartGrouping
        updateViewData()
    }

    fun onChartLengthClick(viewData: ButtonsRowViewData) {
        if (viewData !is StatisticsDetailChartLengthViewData) return
        this.chartLength = viewData.chartLength
        updateViewData()
    }

    fun updateViewData() = delegateScope.launch {
        val data = loadViewData() ?: return@launch
        viewData.set(data)
        chartGrouping = data.appliedChartGrouping
        chartLength = data.appliedChartLength
        parent?.updateContent()
    }

    private fun loadEmptyViewData(): StatisticsDetailChartCompositeViewData? {
        val parent = parent ?: return null
        return chartInteractor.getEmptyChartViewData(
            currentChartGrouping = chartGrouping,
            currentChartLength = chartLength,
            rangeLength = parent.rangeLength,
            rangePosition = parent.rangePosition,
        )
    }

    private suspend fun loadViewData(): StatisticsDetailChartCompositeViewData? {
        val parent = parent ?: return null
        return chartInteractor.getChartViewData(
            records = parent.records,
            compareRecords = parent.compareRecords,
            filter = parent.filter,
            compare = parent.comparisonFilter,
            currentChartGrouping = chartGrouping,
            currentChartLength = chartLength,
            rangeLength = parent.rangeLength,
            rangePosition = parent.rangePosition,
        )
    }
}