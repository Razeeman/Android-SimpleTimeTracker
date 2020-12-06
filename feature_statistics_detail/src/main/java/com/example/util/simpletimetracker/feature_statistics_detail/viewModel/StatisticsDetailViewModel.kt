package com.example.util.simpletimetracker.feature_statistics_detail.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.util.simpletimetracker.core.adapter.ViewHolderType
import com.example.util.simpletimetracker.core.view.buttonsRowView.ButtonsRowViewData
import com.example.util.simpletimetracker.feature_statistics_detail.extra.StatisticsDetailExtra
import com.example.util.simpletimetracker.feature_statistics_detail.interactor.StatisticsDetailViewDataInteractor
import com.example.util.simpletimetracker.feature_statistics_detail.mapper.StatisticsDetailViewDataMapper
import com.example.util.simpletimetracker.feature_statistics_detail.model.ChartGrouping
import com.example.util.simpletimetracker.feature_statistics_detail.model.ChartLength
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailChartLengthViewData
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailChartViewData
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailGroupingViewData
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailPreviewViewData
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailViewData
import com.example.util.simpletimetracker.navigation.Router
import com.example.util.simpletimetracker.navigation.Screen
import com.example.util.simpletimetracker.navigation.params.RecordsAllParams
import kotlinx.coroutines.launch
import javax.inject.Inject

class StatisticsDetailViewModel @Inject constructor(
    private val router: Router,
    private val interactor: StatisticsDetailViewDataInteractor,
    private val mapper: StatisticsDetailViewDataMapper
) : ViewModel() {

    lateinit var extra: StatisticsDetailExtra

    val viewData: LiveData<StatisticsDetailViewData> by lazy {
        return@lazy MutableLiveData(loadInitialViewData())
    }
    val previewViewData: LiveData<StatisticsDetailPreviewViewData> by lazy {
        return@lazy MutableLiveData<StatisticsDetailPreviewViewData>()
    }
    val chartViewData: LiveData<StatisticsDetailChartViewData> by lazy {
        return@lazy MutableLiveData<StatisticsDetailChartViewData>()
    }
    val dailyChartViewData: LiveData<StatisticsDetailChartViewData> by lazy {
        return@lazy MutableLiveData<StatisticsDetailChartViewData>()
    }
    val chartGroupingViewData: LiveData<List<ViewHolderType>> by lazy {
        return@lazy MutableLiveData(loadChartGroupingViewData())
    }
    val chartLengthViewData: LiveData<List<ViewHolderType>> by lazy {
        return@lazy MutableLiveData(loadChartLengthViewData())
    }

    private var chartGrouping: ChartGrouping = ChartGrouping.DAILY
    private var chartLength: ChartLength = ChartLength.TEN

    fun onVisible() {
        updateViewData()
        updatePreviewViewData()
        updateChartViewData()
        updateDailyChartViewData()
    }

    fun onChartGroupingClick(viewData: ButtonsRowViewData) {
        if (viewData !is StatisticsDetailGroupingViewData) return
        this.chartGrouping = viewData.chartGrouping
        updateChartGroupingViewData()
        updateChartViewData()
    }

    fun onChartLengthClick(viewData: ButtonsRowViewData) {
        if (viewData !is StatisticsDetailChartLengthViewData) return
        this.chartLength = viewData.chartLength
        updateChartLengthViewData()
        updateChartViewData()
    }

    fun onRecordsClick() {
        router.navigate(
            Screen.RECORDS_ALL,
            RecordsAllParams(extra.id)
        )
    }

    private fun updateViewData() = viewModelScope.launch {
        val data = interactor.getViewData(extra.id, extra.filterType)
        (viewData as MutableLiveData).value = data
    }

    private fun updatePreviewViewData() = viewModelScope.launch {
        val data = interactor.getPreviewData(extra.id, extra.filterType)
        (previewViewData as MutableLiveData).value = data
    }

    private fun updateChartViewData() = viewModelScope.launch {
        val data = interactor.getChartViewData(
            extra.id, chartGrouping, chartLength, extra.filterType
        )
        (chartViewData as MutableLiveData).value = data
    }

    private fun updateDailyChartViewData() = viewModelScope.launch {
        val data = interactor.getDailyChartViewData(extra.id, extra.filterType)
        (dailyChartViewData as MutableLiveData).value = data
    }

    private fun updateChartGroupingViewData() {
        (chartGroupingViewData as MutableLiveData).value = loadChartGroupingViewData()
    }

    private fun updateChartLengthViewData() {
        (chartLengthViewData as MutableLiveData).value = loadChartLengthViewData()
    }

    private fun loadInitialViewData(): StatisticsDetailViewData {
        return mapper.mapToEmptyViewData()
    }

    private fun loadChartGroupingViewData(): List<ViewHolderType> {
        return mapper.mapToChartGroupingViewData(chartGrouping)
    }

    private fun loadChartLengthViewData(): List<ViewHolderType> {
        return mapper.mapToChartLengthViewData(chartLength)
    }
}
