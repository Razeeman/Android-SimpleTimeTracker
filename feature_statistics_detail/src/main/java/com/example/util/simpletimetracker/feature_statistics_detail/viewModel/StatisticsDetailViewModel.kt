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
import com.example.util.simpletimetracker.feature_statistics_detail.interactor.ChartLength
import com.example.util.simpletimetracker.feature_statistics_detail.interactor.StatisticsDetailInteractor
import com.example.util.simpletimetracker.feature_statistics_detail.mapper.StatisticsDetailViewDataMapper
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailChartLengthViewData
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailChartViewData
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailGroupingViewData
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
    val chartGroupingViewData: LiveData<List<ViewHolderType>> by lazy {
        return@lazy MutableLiveData<List<ViewHolderType>>().let { initial ->
            initial.value = loadChartGroupingViewData()
            initial
        }
    }
    val chartLengthViewData: LiveData<List<ViewHolderType>> by lazy {
        return@lazy MutableLiveData<List<ViewHolderType>>().let { initial ->
            initial.value = loadChartLengthViewData()
            initial
        }
    }

    private var chartGrouping: ChartGrouping = ChartGrouping.DAILY
    private var chartLength: ChartLength = ChartLength.TEN

    fun onChartGroupingClick(grouping: StatisticsDetailGroupingViewData) {
        this.chartGrouping = grouping.chartGrouping
        updateChartGroupingViewData()
        updateChartViewData()
    }

    fun onChartLengthClick(chartLength: StatisticsDetailChartLengthViewData) {
        this.chartLength = chartLength.chartLength
        updateChartLengthViewData()
        updateChartViewData()
    }

    private fun updateChartViewData() = viewModelScope.launch {
        (chartViewData as MutableLiveData).value = loadChartViewData()
    }

    private fun updateChartGroupingViewData() {
        (chartGroupingViewData as MutableLiveData).value = loadChartGroupingViewData()
    }

    private fun updateChartLengthViewData() {
        (chartLengthViewData as MutableLiveData).value = loadChartLengthViewData()
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
                chartLength = chartLength
            )
        }

        return statisticsDetailViewDataMapper.mapToChartViewData(data)
    }

    private fun loadChartGroupingViewData() : List<ViewHolderType> {
        return statisticsDetailViewDataMapper.mapToChartGroupingViewData(chartGrouping)
    }

    private fun loadChartLengthViewData() : List<ViewHolderType> {
        return statisticsDetailViewDataMapper.mapToChartLengthViewData(chartLength)
    }
}
