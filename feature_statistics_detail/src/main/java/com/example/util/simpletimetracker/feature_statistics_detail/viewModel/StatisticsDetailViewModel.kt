package com.example.util.simpletimetracker.feature_statistics_detail.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.util.simpletimetracker.core.adapter.ViewHolderType
import com.example.util.simpletimetracker.domain.interactor.RecordInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.feature_statistics_detail.extra.StatisticsDetailExtra
import com.example.util.simpletimetracker.feature_statistics_detail.interactor.ChartGrouping
import com.example.util.simpletimetracker.feature_statistics_detail.interactor.RangeLength
import com.example.util.simpletimetracker.feature_statistics_detail.interactor.StatisticsDetailInteractor
import com.example.util.simpletimetracker.feature_statistics_detail.mapper.StatisticsDetailViewDataMapper
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailChartViewData
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailRangeViewData
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
    val rangesViewData: LiveData<List<ViewHolderType>> by lazy {
        return@lazy MutableLiveData<List<ViewHolderType>>().let { initial ->
            initial.value = loadChartRangesViewData()
            initial
        }
    }

    private var chartGrouping: ChartGrouping = ChartGrouping.DAILY
    private var chartRange: RangeLength = RangeLength.TEN

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

    fun onRangeClick(range: StatisticsDetailRangeViewData) {
        chartRange = range.rangeLength
        updateChartRangesViewData()
        updateChartViewData()
    }

    private fun updateChartViewData() = viewModelScope.launch {
        (chartViewData as MutableLiveData).value = loadChartViewData()
    }

    private fun updateChartRangesViewData() {
        (rangesViewData as MutableLiveData).value = loadChartRangesViewData()
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
                rangeLength = chartRange
            )
        }

        return statisticsDetailViewDataMapper.mapToChartViewData(data, chartGrouping)
    }

    private fun loadChartRangesViewData() : List<ViewHolderType> {
        return statisticsDetailViewDataMapper.mapToRangesViewData(chartRange)
    }
}
