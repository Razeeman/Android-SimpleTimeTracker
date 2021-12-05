package com.example.util.simpletimetracker.feature_statistics_detail.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.core.extension.set
import com.example.util.simpletimetracker.core.mapper.RangeMapper
import com.example.util.simpletimetracker.core.mapper.TimeMapper
import com.example.util.simpletimetracker.core.view.buttonsRowView.ButtonsRowViewData
import com.example.util.simpletimetracker.feature_views.spinner.CustomSpinner
import com.example.util.simpletimetracker.core.viewData.RangeViewData
import com.example.util.simpletimetracker.core.viewData.RangesViewData
import com.example.util.simpletimetracker.core.viewData.SelectDateViewData
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.model.RangeLength
import com.example.util.simpletimetracker.feature_statistics_detail.interactor.StatisticsDetailChartInteractor
import com.example.util.simpletimetracker.feature_statistics_detail.interactor.StatisticsDetailPreviewInteractor
import com.example.util.simpletimetracker.feature_statistics_detail.interactor.StatisticsDetailSplitChartInteractor
import com.example.util.simpletimetracker.feature_statistics_detail.interactor.StatisticsDetailStatsInteractor
import com.example.util.simpletimetracker.feature_statistics_detail.mapper.StatisticsDetailViewDataMapper
import com.example.util.simpletimetracker.feature_statistics_detail.model.ChartGrouping
import com.example.util.simpletimetracker.feature_statistics_detail.model.ChartLength
import com.example.util.simpletimetracker.feature_statistics_detail.model.SplitChartGrouping
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailChartCompositeViewData
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailChartLengthViewData
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailChartViewData
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailGroupingViewData
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailSplitGroupingViewData
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailStatsViewData
import com.example.util.simpletimetracker.navigation.Router
import com.example.util.simpletimetracker.navigation.params.screen.DateTimeDialogParams
import com.example.util.simpletimetracker.navigation.params.screen.DateTimeDialogType
import com.example.util.simpletimetracker.navigation.params.screen.RecordsAllParams
import com.example.util.simpletimetracker.navigation.params.screen.StatisticsDetailParams
import com.example.util.simpletimetracker.navigation.params.screen.TypesFilterDialogParams
import com.example.util.simpletimetracker.navigation.params.screen.TypesFilterParams
import kotlinx.coroutines.launch
import javax.inject.Inject

class StatisticsDetailViewModel @Inject constructor(
    private val router: Router,
    private val prefsInteractor: PrefsInteractor,
    private val chartInteractor: StatisticsDetailChartInteractor,
    private val previewInteractor: StatisticsDetailPreviewInteractor,
    private val statsInteractor: StatisticsDetailStatsInteractor,
    private val splitChartInteractor: StatisticsDetailSplitChartInteractor,
    private val mapper: StatisticsDetailViewDataMapper,
    private val rangeMapper: RangeMapper,
    private val timeMapper: TimeMapper,
) : ViewModel() {

    lateinit var extra: StatisticsDetailParams

    val previewViewData: LiveData<List<ViewHolderType>> by lazy {
        return@lazy MutableLiveData<List<ViewHolderType>>().let { initial ->
            viewModelScope.launch { initial.value = loadPreviewViewData() }
            initial
        }
    }
    val statsViewData: LiveData<StatisticsDetailStatsViewData> by lazy {
        return@lazy MutableLiveData(loadEmptyStatsViewData())
    }
    val chartViewData: LiveData<StatisticsDetailChartCompositeViewData> by lazy {
        return@lazy MutableLiveData()
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
        return@lazy MutableLiveData()
    }
    val title: LiveData<String> by lazy {
        return@lazy MutableLiveData<String>().let { initial ->
            viewModelScope.launch { initial.value = loadTitle() }
            initial
        }
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
    private var rangeLength: RangeLength = RangeLength.All
    private var rangePosition: Int = 0
    private val typesFilter: TypesFilterParams get() = typesFilterContainer.first()
    private val typesFilterContainer: MutableList<TypesFilterParams> by lazy {
        mutableListOf(extra.filter)
    }

    fun onVisible() {
        updateViewData()
    }

    fun onFilterClick() {
        router.navigate(TypesFilterDialogParams(typesFilter))
    }

    fun onTypesFilterSelected(newFilter: TypesFilterParams) {
        typesFilterContainer.clear()
        typesFilterContainer.add(newFilter)
    }

    fun onTypesFilterDismissed() {
        updatePreviewViewData()
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
            val firstDayOfWeek = prefsInteractor.getFirstDayOfWeek()
            val startOfDayShift = prefsInteractor.getStartOfDayShift()
            val range = timeMapper.getRangeStartAndEnd(
                rangeLength = rangeLength,
                shift = rangePosition,
                firstDayOfWeek = firstDayOfWeek,
                startOfDayShift = startOfDayShift
            )

            router.navigate(
                RecordsAllParams(
                    filter = typesFilter,
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

    fun onDateTimeSet(timestamp: Long, tag: String?) = viewModelScope.launch {
        when (tag) {
            DATE_TAG -> {
                timeMapper.toTimestampShift(
                    toTime = timestamp,
                    range = rangeLength,
                    firstDayOfWeek = prefsInteractor.getFirstDayOfWeek()
                ).toInt().let(::updatePosition)
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

    private fun updatePreviewViewData() = viewModelScope.launch {
        val data = loadPreviewViewData()
        previewViewData.set(data)
    }

    private suspend fun loadPreviewViewData(): List<ViewHolderType> {
        return previewInteractor.getPreviewData(typesFilter)
    }

    private fun updateStatsViewData() = viewModelScope.launch {
        val data = loadStatsViewData()
        statsViewData.set(data)
    }

    private fun loadEmptyStatsViewData(): StatisticsDetailStatsViewData {
        return mapper.mapToEmptyStatsViewData()
    }

    private suspend fun loadStatsViewData(): StatisticsDetailStatsViewData {
        return statsInteractor.getStatsViewData(
            filter = typesFilter,
            rangeLength = rangeLength,
            rangePosition = rangePosition
        )
    }

    private fun updateChartViewData() = viewModelScope.launch {
        val data = loadChartViewData()
        chartViewData.set(data)
    }

    private suspend fun loadChartViewData(): StatisticsDetailChartCompositeViewData {
        return chartInteractor.getChartViewData(
            filter = typesFilter,
            chartGrouping = chartGrouping,
            chartLength = chartLength,
            rangeLength = rangeLength,
            rangePosition = rangePosition,
        )
    }

    private fun updateSplitChartViewData() = viewModelScope.launch {
        val data = loadSplitChartViewData()
        splitChartViewData.set(data)
    }

    private suspend fun loadSplitChartViewData(): StatisticsDetailChartViewData {
        val grouping = splitChartGrouping
            .takeUnless { rangeLength is RangeLength.Day }
            ?: SplitChartGrouping.HOURLY

        return splitChartInteractor.getSplitChartViewData(
            filter = typesFilter,
            rangeLength = rangeLength,
            rangePosition = rangePosition,
            splitChartGrouping = grouping
        )
    }

    private fun updateChartGroupingViewData() {
        val data = loadChartGroupingViewData()
        chartGroupingViewData.set(data)
    }

    private fun loadChartGroupingViewData(): List<ViewHolderType> {
        return mapper.mapToChartGroupingViewData(rangeLength, chartGrouping)
    }

    private fun updateChartLengthViewData() {
        val data = loadChartLengthViewData()
        chartLengthViewData.set(data)
    }

    private fun loadChartLengthViewData(): List<ViewHolderType> {
        return mapper.mapToChartLengthViewData(rangeLength, chartLength)
    }

    private fun updateSplitChartGroupingViewData() {
        val data = loadSplitChartGroupingViewData()
        splitChartGroupingViewData.set(data)
    }

    private fun loadSplitChartGroupingViewData(): List<ViewHolderType> {
        return mapper.mapToSplitChartGroupingViewData(rangeLength, splitChartGrouping)
    }

    private fun updateTitle() = viewModelScope.launch {
        val data = loadTitle()
        title.set(data)
    }

    private suspend fun loadTitle(): String {
        val firstDayOfWeek = prefsInteractor.getFirstDayOfWeek()
        return rangeMapper.mapToTitle(rangeLength, rangePosition, firstDayOfWeek)
    }

    private fun updateRanges() {
        val data = loadRanges()
        rangeItems.set(data)
    }

    private fun loadRanges(): RangesViewData {
        return rangeMapper.mapToRanges(rangeLength)
    }

    private fun updateButtonsVisibility() {
        val data = loadButtonsVisibility()
        rangeButtonsVisibility.set(data)
    }

    private fun loadButtonsVisibility(): Boolean {
        return rangeLength !is RangeLength.All
    }

    companion object {
        private const val DATE_TAG = "statistics_detail_date_tag"
    }
}
