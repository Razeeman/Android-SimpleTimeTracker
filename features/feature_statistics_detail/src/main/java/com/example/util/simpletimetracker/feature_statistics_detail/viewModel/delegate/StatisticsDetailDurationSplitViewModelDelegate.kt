package com.example.util.simpletimetracker.feature_statistics_detail.viewModel.delegate

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.util.simpletimetracker.core.base.ViewModelDelegate
import com.example.util.simpletimetracker.core.extension.set
import com.example.util.simpletimetracker.feature_statistics_detail.interactor.StatisticsDetailSplitChartInteractor
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailChartViewData
import kotlinx.coroutines.launch
import javax.inject.Inject

class StatisticsDetailDurationSplitViewModelDelegate @Inject constructor(
    private val splitChartInteractor: StatisticsDetailSplitChartInteractor,
) : StatisticsDetailViewModelDelegate, ViewModelDelegate() {

    val viewData: LiveData<StatisticsDetailChartViewData?> by lazy {
        return@lazy MutableLiveData()
    }
    val comparisonViewData: LiveData<StatisticsDetailChartViewData?> by lazy {
        return@lazy MutableLiveData()
    }

    private var parent: StatisticsDetailViewModelDelegate.Parent? = null

    override fun attach(parent: StatisticsDetailViewModelDelegate.Parent) {
        this.parent = parent
    }

    fun updateViewData() = delegateScope.launch {
        viewData.set(loadViewData(isForComparison = false))
        comparisonViewData.set(loadViewData(isForComparison = true))
        parent?.updateContent()
    }

    private suspend fun loadViewData(isForComparison: Boolean): StatisticsDetailChartViewData? {
        val parent = parent ?: return null

        return splitChartInteractor.getDurationSplitViewData(
            records = if (isForComparison) parent.compareRecords else parent.records,
            filter = if (isForComparison) parent.comparisonFilter else parent.filter,
            isForComparison = isForComparison,
            rangeLength = parent.rangeLength,
            rangePosition = parent.rangePosition,
        )
    }
}