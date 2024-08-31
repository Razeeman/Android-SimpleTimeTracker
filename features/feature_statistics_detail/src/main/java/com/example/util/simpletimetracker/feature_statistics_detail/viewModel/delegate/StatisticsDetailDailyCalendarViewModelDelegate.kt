package com.example.util.simpletimetracker.feature_statistics_detail.viewModel.delegate

import androidx.lifecycle.LiveData
import com.example.util.simpletimetracker.core.base.ViewModelDelegate
import com.example.util.simpletimetracker.core.extension.lazySuspend
import com.example.util.simpletimetracker.core.extension.set
import com.example.util.simpletimetracker.feature_statistics_detail.interactor.StatisticsDetailDailyCalendarViewDataInteractor
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailDayCalendarViewData
import kotlinx.coroutines.launch
import javax.inject.Inject

class StatisticsDetailDailyCalendarViewModelDelegate @Inject constructor(
    private val dailyCalendarViewDataInteractor: StatisticsDetailDailyCalendarViewDataInteractor,
) : StatisticsDetailViewModelDelegate, ViewModelDelegate() {

    val viewData: LiveData<StatisticsDetailDayCalendarViewData?> by lazySuspend {
        loadEmptyViewData().also { parent?.updateContent() }
    }

    private var parent: StatisticsDetailViewModelDelegate.Parent? = null

    override fun attach(parent: StatisticsDetailViewModelDelegate.Parent) {
        this.parent = parent
    }

    fun updateViewData() = delegateScope.launch {
        val data = loadViewData() ?: return@launch
        viewData.set(data)
        parent?.updateContent()
    }

    private fun loadEmptyViewData(): StatisticsDetailDayCalendarViewData? {
        val parent = parent ?: return null
        return dailyCalendarViewDataInteractor.getEmptyChartViewData(
            rangeLength = parent.rangeLength,
        )
    }

    private suspend fun loadViewData(): StatisticsDetailDayCalendarViewData? {
        val parent = parent ?: return null
        return dailyCalendarViewDataInteractor.getViewData(
            records = parent.records,
            compareRecords = parent.compareRecords,
            filter = parent.filter,
            compare = parent.comparisonFilter,
            rangeLength = parent.rangeLength,
            rangePosition = parent.rangePosition,
        )
    }
}