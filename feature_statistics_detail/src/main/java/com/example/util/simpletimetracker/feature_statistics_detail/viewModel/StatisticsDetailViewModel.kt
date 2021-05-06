package com.example.util.simpletimetracker.feature_statistics_detail.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.util.simpletimetracker.core.adapter.ViewHolderType
import com.example.util.simpletimetracker.core.mapper.RangeMapper
import com.example.util.simpletimetracker.core.mapper.TimeMapper
import com.example.util.simpletimetracker.core.view.buttonsRowView.ButtonsRowViewData
import com.example.util.simpletimetracker.core.view.spinner.CustomSpinner
import com.example.util.simpletimetracker.core.viewData.RangeViewData
import com.example.util.simpletimetracker.core.viewData.RangesViewData
import com.example.util.simpletimetracker.core.viewData.SelectDateViewData
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTypeCategoryInteractor
import com.example.util.simpletimetracker.domain.model.ChartFilterType
import com.example.util.simpletimetracker.domain.model.RangeLength
import com.example.util.simpletimetracker.feature_statistics_detail.interactor.StatisticsDetailChartInteractor
import com.example.util.simpletimetracker.feature_statistics_detail.interactor.StatisticsDetailPreviewInteractor
import com.example.util.simpletimetracker.feature_statistics_detail.interactor.StatisticsDetailSplitChartInteractor
import com.example.util.simpletimetracker.feature_statistics_detail.interactor.StatisticsDetailStatsInteractor
import com.example.util.simpletimetracker.feature_statistics_detail.mapper.StatisticsDetailViewDataMapper
import com.example.util.simpletimetracker.feature_statistics_detail.model.ChartGrouping
import com.example.util.simpletimetracker.feature_statistics_detail.model.ChartLength
import com.example.util.simpletimetracker.feature_statistics_detail.model.SplitChartGrouping
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailChartLengthViewData
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailChartViewData
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailGroupingViewData
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailPreviewViewData
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailSplitGroupingViewData
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailStatsViewData
import com.example.util.simpletimetracker.navigation.Router
import com.example.util.simpletimetracker.navigation.Screen
import com.example.util.simpletimetracker.navigation.params.DateTimeDialogParams
import com.example.util.simpletimetracker.navigation.params.DateTimeDialogType
import com.example.util.simpletimetracker.navigation.params.RecordsAllParams
import com.example.util.simpletimetracker.navigation.params.StatisticsDetailParams
import kotlinx.coroutines.launch
import javax.inject.Inject

class StatisticsDetailViewModel @Inject constructor(
    private val router: Router,
    private val prefsInteractor: PrefsInteractor,
    private val chartInteractor: StatisticsDetailChartInteractor,
    private val previewInteractor: StatisticsDetailPreviewInteractor,
    private val statsInteractor: StatisticsDetailStatsInteractor,
    private val splitChartInteractor: StatisticsDetailSplitChartInteractor,
    private val recordTypeCategoryInteractor: RecordTypeCategoryInteractor,
    private val mapper: StatisticsDetailViewDataMapper,
    private val rangeMapper: RangeMapper,
    private val timeMapper: TimeMapper
) : ViewModel() {

    lateinit var extra: StatisticsDetailParams

    val previewViewData: LiveData<StatisticsDetailPreviewViewData> by lazy {
        return@lazy MutableLiveData<StatisticsDetailPreviewViewData>().let { initial ->
            viewModelScope.launch { initial.value = loadPreviewViewData() }
            initial
        }
    }
    val statsViewData: LiveData<StatisticsDetailStatsViewData> by lazy {
        return@lazy MutableLiveData(loadEmptyStatsViewData())
    }
    val chartViewData: LiveData<StatisticsDetailChartViewData> by lazy {
        return@lazy MutableLiveData<StatisticsDetailChartViewData>()
    }
    val chartGroupingViewData: LiveData<List<ViewHolderType>> by lazy {
        return@lazy MutableLiveData(loadChartGroupingViewData())
    }
    val chartLengthViewData: LiveData<List<ViewHolderType>> by lazy {
        return@lazy MutableLiveData(loadChartLengthViewData())
    }
    val splitChartGroupingViewData: LiveData<List<ViewHolderType>> by lazy {
        return@lazy MutableLiveData(loadSplitChartGroupingViewData())
    }
    val splitChartViewData: LiveData<StatisticsDetailChartViewData> by lazy {
        return@lazy MutableLiveData<StatisticsDetailChartViewData>()
    }
    val title: LiveData<String> by lazy {
        return@lazy MutableLiveData(loadTitle())
    }
    val rangeItems: LiveData<RangesViewData> by lazy {
        return@lazy MutableLiveData(loadRanges())
    }
    val rangeButtonsVisibility: LiveData<Boolean> by lazy {
        return@lazy MutableLiveData(loadButtonsVisibility())
    }

    private var chartGrouping: ChartGrouping = ChartGrouping.DAILY
    private var chartLength: ChartLength = ChartLength.TEN
    private var splitChartGrouping: SplitChartGrouping = SplitChartGrouping.DAILY
    private var rangeLength: RangeLength = RangeLength.ALL
    private var rangePosition: Int = 0

    fun onVisible() {
        updateViewData()
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

    fun onSplitChartGroupingClick(viewData: ButtonsRowViewData) {
        if (viewData !is StatisticsDetailSplitGroupingViewData) return
        this.splitChartGrouping = viewData.splitChartGrouping
        updateSplitChartGroupingViewData()
        updateSplitChartViewData()
    }

    fun onRecordsClick() {
        viewModelScope.launch {
            val typeIds = when (extra.filterType) {
                ChartFilterType.ACTIVITY -> {
                    listOf(extra.id)
                }
                ChartFilterType.CATEGORY -> {
                    recordTypeCategoryInteractor.getTypes(extra.id)
                }
            }
            val range = timeMapper.getRangeStartAndEnd(rangeLength, rangePosition)

            router.navigate(
                Screen.RECORDS_ALL,
                RecordsAllParams(
                    typeIds = typeIds,
                    rangeStart = range.first,
                    rangeEnd = range.second
                )
            )
        }
    }

    fun onPreviousClick() {
        updatePosition(rangePosition - 1)
    }

    fun onTodayClick() {
        updatePosition(0)
    }

    fun onNextClick() {
        updatePosition(rangePosition + 1)
    }

    fun onRangeClick(item: CustomSpinner.CustomSpinnerItem) {
        when (item) {
            is SelectDateViewData -> {
                onSelectDateClick()
                updateRanges()
            }
            is RangeViewData -> {
                rangeLength = item.range
                updateChartGroupingViewData()
                updateChartLengthViewData()
                updateSplitChartGroupingViewData()
                updatePosition(0)
            }
        }
    }

    fun onDateTimeSet(timestamp: Long, tag: String?) {
        when (tag) {
            DATE_TAG -> {
                timestamp
                    .let { timeMapper.toTimestampShift(toTime = it, range = rangeLength) }
                    .toInt()
                    .let(::updatePosition)
            }
        }
    }

    private fun onSelectDateClick() = viewModelScope.launch {
        val useMilitaryTime = prefsInteractor.getUseMilitaryTimeFormat()
        val firstDayOfWeek = prefsInteractor.getFirstDayOfWeek()
        val current = timeMapper.toTimestampShifted(
            rangesFromToday = rangePosition,
            range = rangeLength
        )

        router.navigate(
            Screen.DATE_TIME_DIALOG,
            DateTimeDialogParams(
                tag = DATE_TAG,
                type = DateTimeDialogType.DATE,
                timestamp = current,
                useMilitaryTime = useMilitaryTime,
                firstDayOfWeek = firstDayOfWeek
            )
        )
    }

    private fun updatePosition(newPosition: Int) {
        rangePosition = newPosition
        updateTitle()
        updateRanges()
        updateButtonsVisibility()
        updateViewData()
    }

    private fun updateViewData() {
        updateStatsViewData()
        updateChartViewData()
        updateSplitChartViewData()
    }

    private suspend fun loadPreviewViewData(): StatisticsDetailPreviewViewData {
        return previewInteractor.getPreviewData(extra.id, extra.filterType)
    }

    private fun updateStatsViewData() = viewModelScope.launch {
        val data = loadStatsViewData()
        (statsViewData as MutableLiveData).value = data
    }

    private fun loadEmptyStatsViewData(): StatisticsDetailStatsViewData {
        return mapper.mapToEmptyStatsViewData()
    }

    private suspend fun loadStatsViewData(): StatisticsDetailStatsViewData {
        return statsInteractor.getStatsViewData(
            id = extra.id,
            filter = extra.filterType,
            rangeLength = rangeLength,
            rangePosition = rangePosition
        )
    }

    private fun updateChartViewData() = viewModelScope.launch {
        val data = loadChartViewData()
        (chartViewData as MutableLiveData).value = data
    }

    private suspend fun loadChartViewData(): StatisticsDetailChartViewData {
        return chartInteractor.getChartViewData(
            id = extra.id,
            chartGrouping = chartGrouping,
            chartLength = chartLength,
            filter = extra.filterType,
            rangeLength = rangeLength,
            rangePosition = rangePosition
        )
    }

    private fun updateSplitChartViewData() = viewModelScope.launch {
        val data = loadSplitChartViewData()
        (splitChartViewData as MutableLiveData).value = data
    }

    private suspend fun loadSplitChartViewData(): StatisticsDetailChartViewData {
        val grouping = splitChartGrouping
            .takeUnless { rangeLength == RangeLength.DAY }
            ?: SplitChartGrouping.HOURLY

        return splitChartInteractor.getSplitChartViewData(
            id = extra.id,
            filter = extra.filterType,
            rangeLength = rangeLength,
            rangePosition = rangePosition,
            splitChartGrouping = grouping
        )
    }

    private fun updateChartGroupingViewData() {
        (chartGroupingViewData as MutableLiveData).value = loadChartGroupingViewData()
    }

    private fun loadChartGroupingViewData(): List<ViewHolderType> {
        return mapper.mapToChartGroupingViewData(rangeLength, chartGrouping)
    }

    private fun updateChartLengthViewData() {
        (chartLengthViewData as MutableLiveData).value = loadChartLengthViewData()
    }

    private fun loadChartLengthViewData(): List<ViewHolderType> {
        return mapper.mapToChartLengthViewData(rangeLength, chartLength)
    }

    private fun updateSplitChartGroupingViewData() {
        (splitChartGroupingViewData as MutableLiveData).value = loadSplitChartGroupingViewData()
    }

    private fun loadSplitChartGroupingViewData(): List<ViewHolderType> {
        return mapper.mapToSplitChartGroupingViewData(rangeLength, splitChartGrouping)
    }

    private fun updateTitle() {
        (title as MutableLiveData).value = loadTitle()
    }

    private fun loadTitle(): String {
        return rangeMapper.mapToTitle(rangeLength, rangePosition)
    }

    private fun updateRanges() {
        (rangeItems as MutableLiveData).value = loadRanges()
    }

    private fun loadRanges(): RangesViewData {
        return rangeMapper.mapToRanges(rangeLength)
    }

    private fun updateButtonsVisibility() {
        (rangeButtonsVisibility as MutableLiveData).value = loadButtonsVisibility()
    }

    private fun loadButtonsVisibility(): Boolean {
        return rangeLength != RangeLength.ALL
    }

    companion object {
        private const val DATE_TAG = "statistics_detail_date_tag"
    }
}
