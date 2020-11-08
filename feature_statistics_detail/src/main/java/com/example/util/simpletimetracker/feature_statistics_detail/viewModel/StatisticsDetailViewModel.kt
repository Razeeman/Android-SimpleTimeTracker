package com.example.util.simpletimetracker.feature_statistics_detail.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.util.simpletimetracker.core.adapter.ViewHolderType
import com.example.util.simpletimetracker.core.view.buttonsRowView.ButtonsRowViewData
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.feature_statistics_detail.extra.StatisticsDetailExtra
import com.example.util.simpletimetracker.feature_statistics_detail.interactor.StatisticsDetailInteractor
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
    private val recordInteractor: RecordInteractor,
    private val recordTypeInteractor: RecordTypeInteractor,
    private val statisticsDetailInteractor: StatisticsDetailInteractor,
    private val prefsInteractor: PrefsInteractor,
    private val statisticsDetailViewDataMapper: StatisticsDetailViewDataMapper
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
            RecordsAllParams(extra.typeId)
        )
    }

    private fun updateViewData() = viewModelScope.launch {
        (viewData as MutableLiveData).value = loadViewData()
    }

    private fun updatePreviewViewData() = viewModelScope.launch {
        (previewViewData as MutableLiveData).value = loadPreviewViewData()
    }

    private fun updateChartViewData() = viewModelScope.launch {
        (chartViewData as MutableLiveData).value = loadChartViewData()
    }

    private fun updateDailyChartViewData() = viewModelScope.launch {
        (dailyChartViewData as MutableLiveData).value = loadDailyChartViewData()
    }

    private fun updateChartGroupingViewData() {
        (chartGroupingViewData as MutableLiveData).value = loadChartGroupingViewData()
    }

    private fun updateChartLengthViewData() {
        (chartLengthViewData as MutableLiveData).value = loadChartLengthViewData()
    }

    private suspend fun loadViewData(): StatisticsDetailViewData {
        val isDarkTheme = prefsInteractor.getDarkMode()

        return if (extra.typeId == -1L) {
            statisticsDetailViewDataMapper.map(emptyList(), isDarkTheme)
        } else {
            val records = recordInteractor.getByType(listOf(extra.typeId))
            statisticsDetailViewDataMapper.map(records, isDarkTheme)
        }
    }

    private fun loadInitialViewData(): StatisticsDetailViewData {
        return statisticsDetailViewDataMapper.mapToEmptyViewData()
    }

    private suspend fun loadPreviewViewData(): StatisticsDetailPreviewViewData {
        val isDarkTheme = prefsInteractor.getDarkMode()

        return if (extra.typeId == -1L) {
            statisticsDetailViewDataMapper.mapToPreviewUntracked(isDarkTheme)
        } else {
            val recordType = recordTypeInteractor.get(extra.typeId)
            statisticsDetailViewDataMapper.mapToPreview(recordType, isDarkTheme)
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

    private suspend fun loadDailyChartViewData(): StatisticsDetailChartViewData {
        val data = if (extra.typeId == -1L) {
            emptyMap()
        } else {
            statisticsDetailInteractor.getDailyDurations(typeId = extra.typeId)
        }

        return statisticsDetailViewDataMapper.mapToDailyChartViewData(data)
    }

    private fun loadChartGroupingViewData() : List<ViewHolderType> {
        return statisticsDetailViewDataMapper.mapToChartGroupingViewData(chartGrouping)
    }

    private fun loadChartLengthViewData() : List<ViewHolderType> {
        return statisticsDetailViewDataMapper.mapToChartLengthViewData(chartLength)
    }
}
