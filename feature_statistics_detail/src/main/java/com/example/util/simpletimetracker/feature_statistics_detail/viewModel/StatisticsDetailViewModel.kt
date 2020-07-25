package com.example.util.simpletimetracker.feature_statistics_detail.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.util.simpletimetracker.domain.interactor.RecordInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.feature_statistics_detail.extra.StatisticsDetailExtra
import com.example.util.simpletimetracker.feature_statistics_detail.interactor.ChartGrouping
import com.example.util.simpletimetracker.feature_statistics_detail.interactor.StatisticsDetailInteractor
import com.example.util.simpletimetracker.feature_statistics_detail.mapper.StatisticsDetailViewDataMapper
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailChartViewData
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailViewData
import kotlinx.coroutines.launch
import javax.inject.Inject

class StatisticsDetailViewModel @Inject constructor(
    private val recordInteractor: RecordInteractor,
    private val recordTypeInteractor: RecordTypeInteractor,
    private val statisticsDetailInteractor: StatisticsDetailInteractor,
    private val statisticsDetailViewDataMapper: StatisticsDetailViewDataMapper
) : ViewModel() {

    lateinit var extra: StatisticsDetailExtra

    val viewData: LiveData<StatisticsDetailViewData> by lazy {
        return@lazy MutableLiveData<StatisticsDetailViewData>().let { initial ->
            viewModelScope.launch { initial.value = loadPreviewViewData() }
            initial
        }
    }
    val chartViewData: LiveData<StatisticsDetailChartViewData> by lazy {
        return@lazy MutableLiveData<StatisticsDetailChartViewData>().let { initial ->
            viewModelScope.launch { initial.value = loadChartViewData() }
            initial
        }
    }

    private var chartGrouping: ChartGrouping = ChartGrouping.DAILY

    fun onChartDailyClick() {
        chartGrouping = ChartGrouping.DAILY
        updateChartViewData()
    }

    fun onChartWeeklyClick() {
        chartGrouping = ChartGrouping.WEEKLY
        updateChartViewData()
    }

    fun onChartMonthlyClick() {
        chartGrouping = ChartGrouping.MONTHLY
        updateChartViewData()
    }

    private fun updateChartViewData() = viewModelScope.launch {
        (chartViewData as MutableLiveData).value = loadChartViewData()
    }

    private suspend fun loadPreviewViewData(): StatisticsDetailViewData {
        return if (extra.typeId == -1L) {
            statisticsDetailViewDataMapper.mapToUntracked()
        } else {
            val records = recordInteractor.getAll() // TODO get by typeId
                .filter { it.typeId == extra.typeId }
            val recordType = recordTypeInteractor.get(extra.typeId)
            statisticsDetailViewDataMapper.map(records, recordType)
        }
    }

    private suspend fun loadChartViewData(): StatisticsDetailChartViewData {
        val data = if (extra.typeId == -1L) {
            emptyList()
        } else {
            statisticsDetailInteractor.getDurations(
                typeId = extra.typeId,
                grouping = chartGrouping,
                numberOfGroups = 14
            )
        }

        return statisticsDetailViewDataMapper.mapToChartViewData(data, chartGrouping)
    }
}
