package com.example.util.simpletimetracker.feature_statistics_detail.viewModel.delegate

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.util.simpletimetracker.core.base.ViewModelDelegate
import com.example.util.simpletimetracker.core.extension.lazySuspend
import com.example.util.simpletimetracker.core.extension.set
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.feature_base_adapter.buttonsRow.view.ButtonsRowViewData
import com.example.util.simpletimetracker.domain.statistics.model.RangeLength
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_statistics_detail.R
import com.example.util.simpletimetracker.feature_statistics_detail.adapter.StatisticsDetailBarChartViewData
import com.example.util.simpletimetracker.feature_statistics_detail.adapter.StatisticsDetailBlock
import com.example.util.simpletimetracker.feature_statistics_detail.adapter.StatisticsDetailHintViewData
import com.example.util.simpletimetracker.feature_statistics_detail.interactor.StatisticsDetailSplitChartInteractor
import com.example.util.simpletimetracker.feature_statistics_detail.mapper.StatisticsDetailViewDataMapper
import com.example.util.simpletimetracker.feature_statistics_detail.mapper.mapItem
import com.example.util.simpletimetracker.feature_statistics_detail.mapper.mapItems
import com.example.util.simpletimetracker.feature_statistics_detail.mapper.mapToViewData
import com.example.util.simpletimetracker.feature_statistics_detail.model.SplitChartGrouping
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailSplitGroupingViewData
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailViewData
import kotlinx.coroutines.launch
import javax.inject.Inject

class StatisticsDetailSplitChartViewModelDelegate @Inject constructor(
    private val resourceRepo: ResourceRepo,
    private val splitChartInteractor: StatisticsDetailSplitChartInteractor,
    private val mapper: StatisticsDetailViewDataMapper,
) : StatisticsDetailViewModelDelegate, ViewModelDelegate() {

    val splitChartViewData: LiveData<List<StatisticsDetailViewData.Item<*>>> by lazy {
        return@lazy MutableLiveData()
    }
    val comparisonSplitChartViewData: LiveData<List<StatisticsDetailViewData.Item<*>>> by lazy {
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

    override fun getViewData(): StatisticsDetailViewData {
        return listOfNotNull(
            splitChartViewData.value,
            comparisonSplitChartViewData.value,
            splitChartGroupingViewData.value?.mapItems(),
        ).flatten().let(::mapToViewData)
    }

    fun onSplitChartGroupingClick(viewData: ButtonsRowViewData) {
        if (viewData !is StatisticsDetailSplitGroupingViewData) return
        this.splitChartGrouping = viewData.splitChartGrouping
        updateSplitChartGroupingViewData()
        updateSplitChartViewData()
    }

    fun updateViewData() {
        updateSplitChartViewData()
        updateSplitChartGroupingViewData()
    }

    private fun updateSplitChartGroupingViewData() {
        splitChartGroupingViewData.set(loadSplitChartGroupingViewData())
        parent?.updateContent()
    }

    private fun updateSplitChartViewData() = delegateScope.launch {
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

    private suspend fun loadSplitChartViewData(isForComparison: Boolean): List<StatisticsDetailViewData.Item<*>> {
        val parent = parent ?: return emptyList()

        val grouping = splitChartGrouping
            .takeUnless { parent.rangeLength is RangeLength.Day }
            ?: SplitChartGrouping.HOURLY

        val viewData = splitChartInteractor.getSplitChartViewData(
            records = if (isForComparison) parent.compareRecords else parent.records,
            filter = if (isForComparison) parent.comparisonFilter else parent.filter,
            isForComparison = isForComparison,
            rangeLength = parent.rangeLength,
            rangePosition = parent.rangePosition,
            splitChartGrouping = grouping,
        ) ?: return emptyList()

        return listOfNotNull(
            if (!isForComparison) {
                StatisticsDetailHintViewData(
                    block = StatisticsDetailBlock.SplitHint,
                    text = resourceRepo.getString(R.string.statistics_detail_day_split_hint),
                ).mapItem()
            } else {
                null
            },
            StatisticsDetailBarChartViewData(
                block = if (isForComparison) {
                    StatisticsDetailBlock.SplitChart
                } else {
                    StatisticsDetailBlock.SplitChart
                },
                singleColor = null, // Replaced later.
                marginTopDp = 0,
                data = viewData,
            ).mapItem(forComparison = isForComparison),
        )
    }

    companion object : StatisticsDetailViewData.Key
}