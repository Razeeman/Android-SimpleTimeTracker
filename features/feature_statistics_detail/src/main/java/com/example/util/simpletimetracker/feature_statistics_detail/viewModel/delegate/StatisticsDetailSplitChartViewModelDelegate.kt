package com.example.util.simpletimetracker.feature_statistics_detail.viewModel.delegate

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.util.simpletimetracker.core.base.ViewModelDelegate
import com.example.util.simpletimetracker.core.extension.lazySuspend
import com.example.util.simpletimetracker.core.extension.set
import com.example.util.simpletimetracker.core.view.buttonsRowView.ButtonsRowViewData
import com.example.util.simpletimetracker.domain.model.RangeLength
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_statistics_detail.interactor.StatisticsDetailChartInteractor
import com.example.util.simpletimetracker.feature_statistics_detail.interactor.StatisticsDetailSplitChartInteractor
import com.example.util.simpletimetracker.feature_statistics_detail.mapper.StatisticsDetailViewDataMapper
import com.example.util.simpletimetracker.feature_statistics_detail.model.ChartGrouping
import com.example.util.simpletimetracker.feature_statistics_detail.model.ChartLength
import com.example.util.simpletimetracker.feature_statistics_detail.model.SplitChartGrouping
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailChartCompositeViewData
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailChartLengthViewData
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailChartViewData
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailGroupingViewData
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailSplitGroupingViewData
import kotlinx.coroutines.launch
import javax.inject.Inject

class StatisticsDetailSplitChartViewModelDelegate @Inject constructor(
    private val splitChartInteractor: StatisticsDetailSplitChartInteractor,
    private val mapper: StatisticsDetailViewDataMapper,
) : StatisticsDetailViewModelDelegate, ViewModelDelegate() {

    val splitChartViewData: LiveData<StatisticsDetailChartViewData?> by lazy {
        return@lazy MutableLiveData()
    }
    val comparisonSplitChartViewData: LiveData<StatisticsDetailChartViewData?> by lazy {
        return@lazy MutableLiveData()
    }
    val splitChartGroupingViewData: LiveData<List<ViewHolderType>> by lazySuspend {
        loadSplitChartGroupingViewData().also { parent?.updateContent() }
    }

    private var parent: StatisticsDetailViewModelDelegate.Parent? = null
    private var splitChartGrouping: SplitChartGrouping = SplitChartGrouping.DAILY

    override fun attach(parent: StatisticsDetailViewModelDelegate.Parent) {
        this.parent = parent
    }

    fun onSplitChartGroupingClick(viewData: ButtonsRowViewData) {
        if (viewData !is StatisticsDetailSplitGroupingViewData) return
        this.splitChartGrouping = viewData.splitChartGrouping
        updateSplitChartGroupingViewData()
        updateSplitChartViewData()
    }

    fun updateSplitChartGroupingViewData() {
        splitChartGroupingViewData.set(loadSplitChartGroupingViewData())
        parent?.updateContent()
    }

    fun updateSplitChartViewData() = delegateScope.launch {
        splitChartViewData.set(loadSplitChartViewData(isForComparison = false))
        comparisonSplitChartViewData.set(loadSplitChartViewData(isForComparison = true))
        parent?.updateContent()
    }

    private fun loadSplitChartGroupingViewData(): List<ViewHolderType> {
        val parent = parent ?: return emptyList()
        return mapper.mapToSplitChartGroupingViewData(
            rangeLength = parent.rangeLength,
            splitChartGrouping = splitChartGrouping,
        )
    }

    private suspend fun loadSplitChartViewData(isForComparison: Boolean): StatisticsDetailChartViewData? {
        val parent = parent ?: return null

        val grouping = splitChartGrouping
            .takeUnless { parent.rangeLength is RangeLength.Day }
            ?: SplitChartGrouping.HOURLY

        return splitChartInteractor.getSplitChartViewData(
            records = if (isForComparison) parent.compareRecords else parent.records,
            filter = if (isForComparison) parent.comparisonFilter else parent.filter,
            isForComparison = isForComparison,
            rangeLength = parent.rangeLength,
            rangePosition = parent.rangePosition,
            splitChartGrouping = grouping,
        )
    }
}