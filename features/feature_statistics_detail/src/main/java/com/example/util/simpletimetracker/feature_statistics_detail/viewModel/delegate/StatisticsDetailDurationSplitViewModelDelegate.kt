package com.example.util.simpletimetracker.feature_statistics_detail.viewModel.delegate

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.util.simpletimetracker.core.base.ViewModelDelegate
import com.example.util.simpletimetracker.core.extension.set
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.feature_statistics_detail.R
import com.example.util.simpletimetracker.feature_statistics_detail.adapter.StatisticsDetailBarChartViewData
import com.example.util.simpletimetracker.feature_statistics_detail.adapter.StatisticsDetailBlock
import com.example.util.simpletimetracker.feature_statistics_detail.adapter.StatisticsDetailHintViewData
import com.example.util.simpletimetracker.feature_statistics_detail.interactor.StatisticsDetailSplitChartInteractor
import com.example.util.simpletimetracker.feature_statistics_detail.mapper.mapItem
import com.example.util.simpletimetracker.feature_statistics_detail.mapper.mapToViewData
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailViewData
import kotlinx.coroutines.launch
import javax.inject.Inject

class StatisticsDetailDurationSplitViewModelDelegate @Inject constructor(
    private val resourceRepo: ResourceRepo,
    private val splitChartInteractor: StatisticsDetailSplitChartInteractor,
) : StatisticsDetailViewModelDelegate, ViewModelDelegate() {

    val viewData: LiveData<List<StatisticsDetailViewData.Item<*>>> by lazy {
        return@lazy MutableLiveData()
    }
    val comparisonViewData: LiveData<List<StatisticsDetailViewData.Item<*>>> by lazy {
        return@lazy MutableLiveData()
    }

    private var parent: StatisticsDetailViewModelDelegate.Parent? = null

    override fun attach(parent: StatisticsDetailViewModelDelegate.Parent) {
        this.parent = parent
    }

    override fun getViewData(): StatisticsDetailViewData? {
        return listOfNotNull(
            viewData.value,
            comparisonViewData.value,
        ).flatten().let(::mapToViewData)
    }

    override fun updateViewData(animate: Boolean) {
        delegateScope.launch {
            viewData.set(loadViewData(isForComparison = false))
            comparisonViewData.set(loadViewData(isForComparison = true))
            parent?.updateContent()
        }
    }

    private suspend fun loadViewData(isForComparison: Boolean): List<StatisticsDetailViewData.Item<*>> {
        val parent = parent ?: return emptyList()

        val viewData = splitChartInteractor.getDurationSplitViewData(
            records = if (isForComparison) parent.compareRecords else parent.records,
            filter = if (isForComparison) parent.comparisonFilter else parent.filter,
            isForComparison = isForComparison,
            rangeLength = parent.rangeLength,
            rangePosition = parent.rangePosition,
        ) ?: return emptyList()

        return listOfNotNull(
            if (!isForComparison) {
                StatisticsDetailHintViewData(
                    block = StatisticsDetailBlock.DurationSplitHint,
                    text = resourceRepo.getString(R.string.statistics_detail_duration_split_hint),
                ).mapItem()
            } else {
                null
            },
            StatisticsDetailBarChartViewData(
                block = if (isForComparison) {
                    StatisticsDetailBlock.DurationSplitChartComparison
                } else {
                    StatisticsDetailBlock.DurationSplitChart
                },
                singleColor = null, // Replaced later.
                marginTopDp = 0,
                data = viewData,
            ).mapItem(forComparison = isForComparison),
        )
    }

    companion object : StatisticsDetailViewData.Key
}