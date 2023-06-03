package com.example.util.simpletimetracker.feature_statistics_detail.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.util.simpletimetracker.core.extension.set
import com.example.util.simpletimetracker.core.extension.toModel
import com.example.util.simpletimetracker.core.extension.toParams
import com.example.util.simpletimetracker.core.interactor.RecordFilterInteractor
import com.example.util.simpletimetracker.core.mapper.RangeViewDataMapper
import com.example.util.simpletimetracker.core.mapper.TimeMapper
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.core.view.buttonsRowView.ButtonsRowViewData
import com.example.util.simpletimetracker.core.viewData.RangeViewData
import com.example.util.simpletimetracker.core.viewData.RangesViewData
import com.example.util.simpletimetracker.core.viewData.SelectDateViewData
import com.example.util.simpletimetracker.core.viewData.SelectRangeViewData
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.model.Range
import com.example.util.simpletimetracker.domain.model.RangeLength
import com.example.util.simpletimetracker.domain.model.RecordBase
import com.example.util.simpletimetracker.domain.model.RecordsFilter
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_statistics_detail.R
import com.example.util.simpletimetracker.feature_statistics_detail.interactor.StatisticsDetailChartInteractor
import com.example.util.simpletimetracker.feature_statistics_detail.interactor.StatisticsDetailPreviewInteractor
import com.example.util.simpletimetracker.feature_statistics_detail.interactor.StatisticsDetailSplitChartInteractor
import com.example.util.simpletimetracker.feature_statistics_detail.interactor.StatisticsDetailStatsInteractor
import com.example.util.simpletimetracker.feature_statistics_detail.interactor.StatisticsDetailStreaksInteractor
import com.example.util.simpletimetracker.feature_statistics_detail.mapper.StatisticsDetailViewDataMapper
import com.example.util.simpletimetracker.feature_statistics_detail.model.ChartGrouping
import com.example.util.simpletimetracker.feature_statistics_detail.model.ChartLength
import com.example.util.simpletimetracker.feature_statistics_detail.model.SplitChartGrouping
import com.example.util.simpletimetracker.feature_statistics_detail.model.StreaksType
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailCardViewData
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailChartCompositeViewData
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailChartLengthViewData
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailChartViewData
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailGroupingViewData
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailPreviewCompositeViewData
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailPreviewViewData
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailSplitGroupingViewData
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailStatsViewData
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailStreaksTypeViewData
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailStreaksViewData
import com.example.util.simpletimetracker.feature_views.spinner.CustomSpinner
import com.example.util.simpletimetracker.navigation.Router
import com.example.util.simpletimetracker.navigation.params.screen.CustomRangeSelectionParams
import com.example.util.simpletimetracker.navigation.params.screen.DateTimeDialogParams
import com.example.util.simpletimetracker.navigation.params.screen.DateTimeDialogType
import com.example.util.simpletimetracker.navigation.params.screen.RecordsAllParams
import com.example.util.simpletimetracker.navigation.params.screen.RecordsFilterParam
import com.example.util.simpletimetracker.navigation.params.screen.RecordsFilterParams
import com.example.util.simpletimetracker.navigation.params.screen.RecordsFilterResultParams
import com.example.util.simpletimetracker.navigation.params.screen.StatisticsDetailParams
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

@HiltViewModel
class StatisticsDetailViewModel @Inject constructor(
    private val router: Router,
    private val resourceRepo: ResourceRepo,
    private val prefsInteractor: PrefsInteractor,
    private val recordFilterInteractor: RecordFilterInteractor,
    private val chartInteractor: StatisticsDetailChartInteractor,
    private val previewInteractor: StatisticsDetailPreviewInteractor,
    private val statsInteractor: StatisticsDetailStatsInteractor,
    private val streaksInteractor: StatisticsDetailStreaksInteractor,
    private val splitChartInteractor: StatisticsDetailSplitChartInteractor,
    private val mapper: StatisticsDetailViewDataMapper,
    private val rangeViewDataMapper: RangeViewDataMapper,
    private val timeMapper: TimeMapper,
) : ViewModel() {

    val previewViewData: LiveData<StatisticsDetailPreviewCompositeViewData> by lazy {
        return@lazy MutableLiveData<StatisticsDetailPreviewCompositeViewData>().let { initial ->
            viewModelScope.launch { initial.value = loadPreviewViewData() }
            initial
        }
    }
    val statsViewData: LiveData<StatisticsDetailStatsViewData> by lazy {
        return@lazy MutableLiveData(loadEmptyStatsViewData())
    }
    val streaksViewData: LiveData<StatisticsDetailStreaksViewData> by lazy {
        return@lazy MutableLiveData(loadEmptyStreaksViewData())
    }
    val streaksTypeViewData: LiveData<List<ViewHolderType>> by lazy {
        return@lazy MutableLiveData(loadStreaksTypeViewData())
    }
    val chartViewData: LiveData<StatisticsDetailChartCompositeViewData> by lazy {
        return@lazy MutableLiveData()
    }
    val emptyRangeAveragesData: LiveData<List<StatisticsDetailCardViewData>> by lazy {
        return@lazy MutableLiveData(loadEmptyRangeAveragesData())
    }
    val splitChartGroupingViewData: LiveData<List<ViewHolderType>> by lazy {
        return@lazy MutableLiveData(loadSplitChartGroupingViewData())
    }
    val splitChartViewData: LiveData<StatisticsDetailChartViewData> by lazy {
        return@lazy MutableLiveData()
    }
    val comparisonSplitChartViewData: LiveData<StatisticsDetailChartViewData> by lazy {
        return@lazy MutableLiveData()
    }
    val durationSplitChartViewData: LiveData<StatisticsDetailChartViewData> by lazy {
        return@lazy MutableLiveData()
    }
    val comparisonDurationSplitChartViewData: LiveData<StatisticsDetailChartViewData> by lazy {
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

    private lateinit var extra: StatisticsDetailParams

    private var chartGrouping: ChartGrouping = ChartGrouping.DAILY
    private var streaksType: StreaksType = StreaksType.LONGEST
    private var chartLength: ChartLength = ChartLength.TEN
    private var splitChartGrouping: SplitChartGrouping = SplitChartGrouping.DAILY
    private var rangeLength: RangeLength = RangeLength.All
    private var rangePosition: Int = 0
    private val filter: MutableList<RecordsFilter> by lazy {
        extra.filter.map(RecordsFilterParam::toModel).toMutableList()
    }
    private val comparisonFilter: MutableList<RecordsFilter> = mutableListOf()
    private var records: List<RecordBase> = emptyList() // all records with selected ids
    private var compareRecords: List<RecordBase> = emptyList() // all records with selected ids
    private var loadJob: Job? = null

    fun initialize(extra: StatisticsDetailParams) {
        if (this::extra.isInitialized) return
        this.extra = extra
        rangeLength = getRangeLength(extra.range)
        rangePosition = extra.shift
    }

    fun onVisible() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            loadRecordsCache()
            updateViewData()
        }
    }

    fun onFilterClick() = viewModelScope.launch {
        val dateFilter = recordFilterInteractor.mapDateFilter(rangeLength, rangePosition)
            ?.let(::listOf).orEmpty()

        router.navigate(
            RecordsFilterParams(
                tag = FILTER_TAG,
                title = resourceRepo.getString(R.string.chart_filter_hint),
                dateSelectionAvailable = false,
                untrackedSelectionAvailable = true,
                filters = filter
                    .plus(dateFilter)
                    .map(RecordsFilter::toParams).toList(),
            )
        )
    }

    fun onCompareClick() = viewModelScope.launch {
        val dateFilter = recordFilterInteractor.mapDateFilter(rangeLength, rangePosition)
            ?.let(::listOf).orEmpty()

        router.navigate(
            RecordsFilterParams(
                tag = COMPARE_TAG,
                title = resourceRepo.getString(R.string.types_compare_hint),
                dateSelectionAvailable = false,
                filters = comparisonFilter
                    .plus(dateFilter)
                    .map(RecordsFilter::toParams).toList(),
            )
        )
    }

    fun onTypesFilterSelected(result: RecordsFilterResultParams) {
        val finalFilters = result.filters.filter { it !is RecordsFilter.Date }

        when (result.tag) {
            FILTER_TAG -> {
                filter.clear()
                filter.addAll(finalFilters)
            }
            COMPARE_TAG -> {
                comparisonFilter.clear()
                comparisonFilter.addAll(finalFilters)
            }
        }

        // Update is on dismiss.
    }

    fun onTypesFilterDismissed(tag: String) {
        if (tag !in listOf(FILTER_TAG, COMPARE_TAG)) return

        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            loadRecordsCache()
            updatePreviewViewData()
            updateViewData()
        }
    }

    fun onStreaksTypeClick(viewData: ButtonsRowViewData) {
        if (viewData !is StatisticsDetailStreaksTypeViewData) return
        this.streaksType = viewData.type
        updateStreaksTypeViewData()
        updateStreaksViewData()
    }

    fun onChartGroupingClick(viewData: ButtonsRowViewData) {
        if (viewData !is StatisticsDetailGroupingViewData) return
        this.chartGrouping = viewData.chartGrouping
        updateChartViewData()
    }

    fun onChartLengthClick(viewData: ButtonsRowViewData) {
        if (viewData !is StatisticsDetailChartLengthViewData) return
        this.chartLength = viewData.chartLength
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
            val dateFilter = recordFilterInteractor.mapDateFilter(rangeLength, rangePosition)
                ?.let(::listOf).orEmpty()
            val finalFilters = filter
                .plus(dateFilter)
                .map(RecordsFilter::toParams).toList()

            router.navigate(RecordsAllParams(finalFilters))
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

    fun onRangeSelected(item: CustomSpinner.CustomSpinnerItem) {
        when (item) {
            is SelectDateViewData -> {
                onSelectDateClick()
                updateRanges()
            }
            is SelectRangeViewData -> {
                onSelectRangeClick()
                updateRanges()
            }
            is RangeViewData -> {
                rangeLength = item.range
                onRangeChanged()
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

    fun onCustomRangeSelected(range: Range) {
        rangeLength = RangeLength.Custom(range)
        onRangeChanged()
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

    private fun onSelectRangeClick() = viewModelScope.launch {
        val currentCustomRange = (rangeLength as? RangeLength.Custom)?.range

        CustomRangeSelectionParams(
            rangeStart = currentCustomRange?.timeStarted,
            rangeEnd = currentCustomRange?.timeEnded,
        ).let(router::navigate)
    }

    private fun onRangeChanged() {
        viewModelScope.launch { prefsInteractor.setStatisticsDetailRange(rangeLength) }
        updateSplitChartGroupingViewData()
        updatePosition(0)
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
        updateStreaksViewData()
        updateChartViewData()
        updateSplitChartViewData()
        updateDurationSplitChartViewData()
    }

    private fun getRangeLength(range: StatisticsDetailParams.RangeLengthParams): RangeLength {
        return when (range) {
            is StatisticsDetailParams.RangeLengthParams.Day -> RangeLength.Day
            is StatisticsDetailParams.RangeLengthParams.Week -> RangeLength.Week
            is StatisticsDetailParams.RangeLengthParams.Month -> RangeLength.Month
            is StatisticsDetailParams.RangeLengthParams.Year -> RangeLength.Year
            is StatisticsDetailParams.RangeLengthParams.All -> RangeLength.All
            is StatisticsDetailParams.RangeLengthParams.Custom -> Range(
                timeStarted = range.start, timeEnded = range.end
            ).let(RangeLength::Custom)
            is StatisticsDetailParams.RangeLengthParams.Last -> RangeLength.Last
        }
    }

    private suspend fun loadRecordsCache() {
        // Load all records without date filter for faster date selection.
        records = recordFilterInteractor.getByFilter(filter)
        compareRecords = recordFilterInteractor.getByFilter(comparisonFilter)
    }

    private fun updatePreviewViewData() = viewModelScope.launch {
        previewViewData.set(loadPreviewViewData())
    }

    private suspend fun loadPreviewViewData(): StatisticsDetailPreviewCompositeViewData {
        val data = previewInteractor.getPreviewData(
            filterParams = filter,
            isForComparison = false,
        )
        val comparisonData = previewInteractor.getPreviewData(
            filterParams = comparisonFilter,
            isForComparison = true,
        )
        return StatisticsDetailPreviewCompositeViewData(
            data = data.firstOrNull() as? StatisticsDetailPreviewViewData,
            additionalData = data.drop(1),
            comparisonData = comparisonData,
        )
    }

    private fun updateStatsViewData() = viewModelScope.launch {
        statsViewData.set(loadStatsViewData())
    }

    private fun loadEmptyStatsViewData(): StatisticsDetailStatsViewData {
        return statsInteractor.getEmptyStatsViewData()
    }

    private suspend fun loadStatsViewData(): StatisticsDetailStatsViewData {
        return statsInteractor.getStatsViewData(
            records = records,
            compareRecords = compareRecords,
            showComparison = comparisonFilter.isNotEmpty(),
            rangeLength = rangeLength,
            rangePosition = rangePosition
        )
    }

    private fun updateStreaksTypeViewData() {
        streaksTypeViewData.set(loadStreaksTypeViewData())
    }

    private fun loadStreaksTypeViewData(): List<ViewHolderType> {
        return streaksInteractor.mapToStreaksTypeViewData(streaksType)
    }

    private fun updateStreaksViewData() = viewModelScope.launch {
        streaksViewData.set(loadStreaksViewData())
    }

    private fun loadEmptyStreaksViewData(): StatisticsDetailStreaksViewData {
        return streaksInteractor.getEmptyStreaksViewData()
    }

    private suspend fun loadStreaksViewData(): StatisticsDetailStreaksViewData {
        return streaksInteractor.getStreaksViewData(
            records = records,
            compareRecords = compareRecords,
            showComparison = comparisonFilter.isNotEmpty(),
            rangeLength = rangeLength,
            rangePosition = rangePosition,
            streaksType = streaksType,
        )
    }

    private fun updateChartViewData() = viewModelScope.launch {
        val data = loadChartViewData()
        chartViewData.set(data)
        chartGrouping = data.appliedChartGrouping
        chartLength = data.appliedChartLength
    }

    private fun loadEmptyRangeAveragesData(): List<StatisticsDetailCardViewData> {
        return chartInteractor.getEmptyRangeAveragesData()
    }

    private suspend fun loadChartViewData(): StatisticsDetailChartCompositeViewData {
        return chartInteractor.getChartViewData(
            records = records,
            compareRecords = compareRecords,
            filter = filter,
            compare = comparisonFilter,
            currentChartGrouping = chartGrouping,
            currentChartLength = chartLength,
            rangeLength = rangeLength,
            rangePosition = rangePosition,
        )
    }

    private fun updateSplitChartViewData() = viewModelScope.launch {
        splitChartViewData.set(loadSplitChartViewData(isForComparison = false))
        comparisonSplitChartViewData.set(loadSplitChartViewData(isForComparison = true))
    }

    private suspend fun loadSplitChartViewData(isForComparison: Boolean): StatisticsDetailChartViewData {
        val grouping = splitChartGrouping
            .takeUnless { rangeLength is RangeLength.Day }
            ?: SplitChartGrouping.HOURLY

        return splitChartInteractor.getSplitChartViewData(
            records = if (isForComparison) compareRecords else records,
            filter = if (isForComparison) comparisonFilter else filter,
            isForComparison = isForComparison,
            rangeLength = rangeLength,
            rangePosition = rangePosition,
            splitChartGrouping = grouping
        )
    }

    private fun updateDurationSplitChartViewData() = viewModelScope.launch {
        durationSplitChartViewData
            .set(loadDurationSplitChartViewData(isForComparison = false))
        comparisonDurationSplitChartViewData
            .set(loadDurationSplitChartViewData(isForComparison = true))
    }

    private suspend fun loadDurationSplitChartViewData(isForComparison: Boolean): StatisticsDetailChartViewData {
        return splitChartInteractor.getDurationSplitViewData(
            records = if (isForComparison) compareRecords else records,
            filter = if (isForComparison) comparisonFilter else filter,
            isForComparison = isForComparison,
            rangeLength = rangeLength,
            rangePosition = rangePosition,
        )
    }

    private fun updateSplitChartGroupingViewData() {
        splitChartGroupingViewData.set(loadSplitChartGroupingViewData())
    }

    private fun loadSplitChartGroupingViewData(): List<ViewHolderType> {
        return mapper.mapToSplitChartGroupingViewData(rangeLength, splitChartGrouping)
    }

    private fun updateTitle() = viewModelScope.launch {
        title.set(loadTitle())
    }

    private suspend fun loadTitle(): String {
        val startOfDayShift = prefsInteractor.getStartOfDayShift()
        val firstDayOfWeek = prefsInteractor.getFirstDayOfWeek()
        return rangeViewDataMapper.mapToTitle(rangeLength, rangePosition, startOfDayShift, firstDayOfWeek)
    }

    private fun updateRanges() {
        rangeItems.set(loadRanges())
    }

    private fun loadRanges(): RangesViewData {
        return rangeViewDataMapper.mapToRanges(rangeLength)
    }

    private fun updateButtonsVisibility() {
        rangeButtonsVisibility.set(loadButtonsVisibility())
    }

    private fun loadButtonsVisibility(): Boolean {
        return when (rangeLength) {
            is RangeLength.All, is RangeLength.Custom, is RangeLength.Last -> false
            else -> true
        }
    }

    companion object {
        private const val DATE_TAG = "statistics_detail_date_tag"
        private const val FILTER_TAG = "statistics_detail_filter_tag"
        private const val COMPARE_TAG = "statistics_detail_compare_tag"
    }
}
