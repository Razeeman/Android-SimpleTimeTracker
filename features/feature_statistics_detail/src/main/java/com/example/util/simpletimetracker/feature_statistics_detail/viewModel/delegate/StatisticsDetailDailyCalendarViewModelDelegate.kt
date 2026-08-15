package com.example.util.simpletimetracker.feature_statistics_detail.viewModel.delegate

import androidx.lifecycle.LiveData
import com.example.util.simpletimetracker.core.base.ViewModelDelegate
import com.example.util.simpletimetracker.core.extension.lazySuspend
import com.example.util.simpletimetracker.core.extension.set
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_statistics_detail.interactor.StatisticsDetailDailyCalendarViewDataInteractor
import com.example.util.simpletimetracker.feature_statistics_detail.mapper.mapItems
import com.example.util.simpletimetracker.feature_statistics_detail.mapper.mapToViewData
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailViewData
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.collections.emptyList

class StatisticsDetailDailyCalendarViewModelDelegate @Inject constructor(
    private val dailyCalendarViewDataInteractor: StatisticsDetailDailyCalendarViewDataInteractor,
) : StatisticsDetailViewModelDelegate, ViewModelDelegate() {

    val viewData: LiveData<StatisticsDetailViewData> by lazySuspend {
        loadEmptyViewData().also { parent?.updateContent() }
    }

    private var parent: StatisticsDetailViewModelDelegate.Parent? = null

    override fun attach(parent: StatisticsDetailViewModelDelegate.Parent) {
        this.parent = parent
    }

    override fun getViewData(): StatisticsDetailViewData? {
        return viewData.value
    }

    fun updateViewData() = delegateScope.launch {
        val data = loadViewData()
        viewData.set(data)
        parent?.updateContent()
    }

    private fun loadEmptyViewData(): StatisticsDetailViewData {
        val parent = parent ?: return mapToViewData(emptyList<ViewHolderType>().mapItems())
        return dailyCalendarViewDataInteractor.getEmptyChartViewData(
            rangeLength = parent.rangeLength,
        ).mapItems().let(::mapToViewData)
    }

    private suspend fun loadViewData(): StatisticsDetailViewData {
        val parent = parent ?: return mapToViewData(emptyList<ViewHolderType>().mapItems())
        return dailyCalendarViewDataInteractor.getViewData(
            records = parent.records,
            compareRecords = parent.compareRecords,
            filter = parent.filter,
            compare = parent.comparisonFilter,
            rangeLength = parent.rangeLength,
            rangePosition = parent.rangePosition,
        ).mapItems().let(::mapToViewData)
    }

    companion object : StatisticsDetailViewData.Key
}