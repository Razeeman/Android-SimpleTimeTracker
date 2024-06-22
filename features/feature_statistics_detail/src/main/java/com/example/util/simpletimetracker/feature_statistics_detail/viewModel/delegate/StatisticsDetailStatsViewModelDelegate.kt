package com.example.util.simpletimetracker.feature_statistics_detail.viewModel.delegate

import androidx.lifecycle.LiveData
import com.example.util.simpletimetracker.core.base.ViewModelDelegate
import com.example.util.simpletimetracker.core.extension.lazySuspend
import com.example.util.simpletimetracker.core.extension.set
import com.example.util.simpletimetracker.feature_statistics_detail.interactor.StatisticsDetailStatsInteractor
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailStatsViewData
import kotlinx.coroutines.launch
import javax.inject.Inject

class StatisticsDetailStatsViewModelDelegate @Inject constructor(
    private val statsInteractor: StatisticsDetailStatsInteractor,
) : StatisticsDetailViewModelDelegate, ViewModelDelegate() {

    val viewData: LiveData<StatisticsDetailStatsViewData?> by lazySuspend {
        loadEmptyViewData().also { parent?.updateContent() }
    }

    private var parent: StatisticsDetailViewModelDelegate.Parent? = null

    override fun attach(parent: StatisticsDetailViewModelDelegate.Parent) {
        this.parent = parent
    }

    fun updateViewData() = delegateScope.launch {
        viewData.set(loadViewData())
        parent?.updateContent()
    }

    private fun loadEmptyViewData(): StatisticsDetailStatsViewData {
        return statsInteractor.getEmptyStatsViewData()
    }

    private suspend fun loadViewData(): StatisticsDetailStatsViewData? {
        val parent = parent ?: return null

        return statsInteractor.getStatsViewData(
            records = parent.records,
            compareRecords = parent.compareRecords,
            showComparison = parent.comparisonFilter.isNotEmpty(),
            rangeLength = parent.rangeLength,
            rangePosition = parent.rangePosition,
        )
    }
}