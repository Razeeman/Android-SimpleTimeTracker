package com.example.util.simpletimetracker.feature_statistics_detail.viewModel

import android.graphics.Color
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.util.simpletimetracker.core.base.BaseViewModel
import com.example.util.simpletimetracker.core.extension.lazySuspend
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
import com.example.util.simpletimetracker.domain.extension.getDaily
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.model.Coordinates
import com.example.util.simpletimetracker.domain.model.Range
import com.example.util.simpletimetracker.domain.model.RangeLength
import com.example.util.simpletimetracker.domain.model.RecordBase
import com.example.util.simpletimetracker.domain.model.RecordTypeGoal
import com.example.util.simpletimetracker.domain.model.RecordsFilter
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_statistics_detail.R
import com.example.util.simpletimetracker.feature_statistics_detail.adapter.StatisticsDetailBarChartViewData
import com.example.util.simpletimetracker.feature_statistics_detail.adapter.StatisticsDetailBlock
import com.example.util.simpletimetracker.feature_statistics_detail.adapter.StatisticsDetailButtonsRowViewData
import com.example.util.simpletimetracker.feature_statistics_detail.adapter.StatisticsDetailCardDoubleViewData
import com.example.util.simpletimetracker.feature_statistics_detail.adapter.StatisticsDetailCardViewData
import com.example.util.simpletimetracker.feature_statistics_detail.adapter.StatisticsDetailHintViewData
import com.example.util.simpletimetracker.feature_statistics_detail.adapter.StatisticsDetailNextActivitiesViewData
import com.example.util.simpletimetracker.feature_statistics_detail.adapter.StatisticsDetailPreviewsViewData
import com.example.util.simpletimetracker.feature_statistics_detail.adapter.StatisticsDetailSeriesCalendarViewData
import com.example.util.simpletimetracker.feature_statistics_detail.adapter.StatisticsDetailSeriesChartViewData
import com.example.util.simpletimetracker.feature_statistics_detail.customView.SeriesCalendarView
import com.example.util.simpletimetracker.feature_statistics_detail.interactor.StatisticsDetailAdjacentActivitiesInteractor
import com.example.util.simpletimetracker.feature_statistics_detail.interactor.StatisticsDetailChartInteractor
import com.example.util.simpletimetracker.feature_statistics_detail.interactor.StatisticsDetailGetGoalFromFilterInteractor
import com.example.util.simpletimetracker.feature_statistics_detail.interactor.StatisticsDetailPreviewInteractor
import com.example.util.simpletimetracker.feature_statistics_detail.interactor.StatisticsDetailSplitChartInteractor
import com.example.util.simpletimetracker.feature_statistics_detail.interactor.StatisticsDetailStatsInteractor
import com.example.util.simpletimetracker.feature_statistics_detail.interactor.StatisticsDetailStreaksInteractor
import com.example.util.simpletimetracker.feature_statistics_detail.mapper.StatisticsDetailViewDataMapper
import com.example.util.simpletimetracker.feature_statistics_detail.model.ChartGrouping
import com.example.util.simpletimetracker.feature_statistics_detail.model.ChartLength
import com.example.util.simpletimetracker.feature_statistics_detail.model.SplitChartGrouping
import com.example.util.simpletimetracker.feature_statistics_detail.model.StreaksGoal
import com.example.util.simpletimetracker.feature_statistics_detail.model.StreaksType
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailCardInternalViewData
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailChartCompositeViewData
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailChartLengthViewData
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailChartViewData
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailClickableLongest
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailClickableShortest
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailClickableTracked
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailGroupingViewData
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailPreviewCompositeViewData
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailPreviewViewData
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailSplitGroupingViewData
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailStatsViewData
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailStreaksGoalViewData
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailStreaksTypeViewData
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailStreaksViewData
import com.example.util.simpletimetracker.feature_views.spinner.CustomSpinner
import com.example.util.simpletimetracker.navigation.Router
import com.example.util.simpletimetracker.navigation.params.notification.PopupParams
import com.example.util.simpletimetracker.navigation.params.screen.CustomRangeSelectionParams
import com.example.util.simpletimetracker.navigation.params.screen.DateTimeDialogParams
import com.example.util.simpletimetracker.navigation.params.screen.DateTimeDialogType
import com.example.util.simpletimetracker.navigation.params.screen.RecordsAllParams
import com.example.util.simpletimetracker.navigation.params.screen.RecordsFilterParam
import com.example.util.simpletimetracker.navigation.params.screen.RecordsFilterParams
import com.example.util.simpletimetracker.navigation.params.screen.RecordsFilterResultParams
import com.example.util.simpletimetracker.navigation.params.screen.StatisticsDetailParams
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

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
    private val adjacentActivitiesInteractor: StatisticsDetailAdjacentActivitiesInteractor,
    private val statisticsDetailGetGoalFromFilterInteractor: StatisticsDetailGetGoalFromFilterInteractor,
    private val mapper: StatisticsDetailViewDataMapper,
    private val rangeViewDataMapper: RangeViewDataMapper,
    private val timeMapper: TimeMapper,
) : BaseViewModel() {

    val previewViewData: LiveData<StatisticsDetailPreviewCompositeViewData> by lazySuspend {
        loadPreviewViewData().also { updateContent() }
    }
    val title: LiveData<String> by lazySuspend {
        loadTitle()
    }
    val rangeItems: LiveData<RangesViewData> by lazy {
        return@lazy MutableLiveData(loadRanges())
    }
    val rangeButtonsVisibility: LiveData<Boolean> by lazy {
        return@lazy MutableLiveData(loadButtonsVisibility())
    }
    val content: LiveData<List<ViewHolderType>> by lazySuspend {
        loadContent()
    }

    private val statsViewData: LiveData<StatisticsDetailStatsViewData> by lazySuspend {
        loadEmptyStatsViewData().also { updateContent() }
    }
    private val streaksViewData: LiveData<StatisticsDetailStreaksViewData> by lazySuspend {
        loadEmptyStreaksViewData().also { updateContent() }
    }
    private val streaksTypeViewData: LiveData<List<ViewHolderType>> by lazySuspend {
        loadStreaksTypeViewData().also { updateContent() }
    }
    private val streaksGoalViewData: LiveData<List<ViewHolderType>> by lazySuspend {
        loadStreaksGoalViewData().also { updateContent() }
    }
    private val chartViewData: LiveData<StatisticsDetailChartCompositeViewData> by lazySuspend {
        loadEmptyChartViewData().also { updateContent() }
    }
    private val splitChartGroupingViewData: LiveData<List<ViewHolderType>> by lazySuspend {
        loadSplitChartGroupingViewData().also { updateContent() }
    }
    private val nextActivitiesViewData: LiveData<List<ViewHolderType>> by lazy {
        return@lazy MutableLiveData()
    }
    private val splitChartViewData: LiveData<StatisticsDetailChartViewData> by lazy {
        return@lazy MutableLiveData()
    }
    private val comparisonSplitChartViewData: LiveData<StatisticsDetailChartViewData> by lazy {
        return@lazy MutableLiveData()
    }
    private val durationSplitChartViewData: LiveData<StatisticsDetailChartViewData> by lazy {
        return@lazy MutableLiveData()
    }
    private val comparisonDurationSplitChartViewData: LiveData<StatisticsDetailChartViewData> by lazy {
        return@lazy MutableLiveData()
    }

    private lateinit var extra: StatisticsDetailParams

    private var chartGrouping: ChartGrouping = ChartGrouping.DAILY
    private var streaksType: StreaksType = StreaksType.LONGEST
    private var streaksGoal: StreaksGoal = StreaksGoal.ANY
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
    private var dailyGoal: Result<RecordTypeGoal.Type?>? = null
    private var compareDailyGoal: Result<RecordTypeGoal.Type?>? = null

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
        openFilter(
            tag = FILTER_TAG,
            title = resourceRepo.getString(R.string.chart_filter_hint),
            filters = filter,
        )
    }

    fun onCompareClick() = viewModelScope.launch {
        openFilter(
            tag = COMPARE_TAG,
            title = resourceRepo.getString(R.string.types_compare_hint),
            filters = comparisonFilter,
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
            dailyGoal = Result.success(getDailyGoalType(filter))
            compareDailyGoal = Result.success(getDailyGoalType(comparisonFilter))
            loadRecordsCache()
            updatePreviewViewData()
            updateViewData()
            updateStreaksGoalViewData()
        }
    }

    private fun onStreaksTypeClick(viewData: ButtonsRowViewData) {
        if (viewData !is StatisticsDetailStreaksTypeViewData) return
        streaksType = viewData.type
        updateStreaksTypeViewData()
        updateStreaksViewData()
    }

    private fun onStreaksGoalClick(viewData: ButtonsRowViewData) {
        if (viewData !is StatisticsDetailStreaksGoalViewData) return
        streaksGoal = viewData.type
        updateStreaksGoalViewData()
        updateStreaksViewData()
    }

    fun onButtonsRowClick(block: StatisticsDetailBlock, viewData: ButtonsRowViewData) {
        when (block) {
            StatisticsDetailBlock.ChartGrouping -> onChartGroupingClick(viewData)
            StatisticsDetailBlock.ChartLength -> onChartLengthClick(viewData)
            StatisticsDetailBlock.SeriesGoal -> onStreaksGoalClick(viewData)
            StatisticsDetailBlock.SeriesType -> onStreaksTypeClick(viewData)
            StatisticsDetailBlock.SplitChartGrouping -> onSplitChartGroupingClick(viewData)
            else -> {
                // Do nothing
            }
        }
    }

    private fun onChartGroupingClick(viewData: ButtonsRowViewData) {
        if (viewData !is StatisticsDetailGroupingViewData) return
        this.chartGrouping = viewData.chartGrouping
        updateChartViewData()
    }

    private fun onChartLengthClick(viewData: ButtonsRowViewData) {
        if (viewData !is StatisticsDetailChartLengthViewData) return
        this.chartLength = viewData.chartLength
        updateChartViewData()
    }

    private fun onSplitChartGroupingClick(viewData: ButtonsRowViewData) {
        if (viewData !is StatisticsDetailSplitGroupingViewData) return
        this.splitChartGrouping = viewData.splitChartGrouping
        updateSplitChartGroupingViewData()
        updateSplitChartViewData()
    }

    fun onCardClick(
        type: StatisticsDetailCardInternalViewData.ClickableType,
        coordinates: Coordinates,
    ) {
        when (type) {
            is StatisticsDetailClickableTracked -> {
                onRecordsClick()
            }
            is StatisticsDetailClickableShortest -> {
                PopupParams(
                    message = type.message,
                    anchorCoordinates = coordinates,
                ).let(router::show)
            }
            is StatisticsDetailClickableLongest -> {
                PopupParams(
                    message = type.message,
                    anchorCoordinates = coordinates,
                ).let(router::show)
            }
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
                    firstDayOfWeek = prefsInteractor.getFirstDayOfWeek(),
                ).toInt().let(::updatePosition)
            }
        }
    }

    fun onCustomRangeSelected(range: Range) {
        rangeLength = RangeLength.Custom(range)
        onRangeChanged()
    }

    fun onStreaksCalendarClick(
        viewData: SeriesCalendarView.ViewData,
        coordinates: Coordinates,
    ) {
        PopupParams(
            message = timeMapper.formatDayDateYear(viewData.rangeStart),
            anchorCoordinates = coordinates,
        ).let(router::show)
    }

    private fun onRecordsClick() {
        viewModelScope.launch {
            val dateFilter = recordFilterInteractor.mapDateFilter(rangeLength, rangePosition)
                ?.let(::listOf).orEmpty()
            val finalFilters = filter
                .plus(dateFilter)
                .map(RecordsFilter::toParams).toList()

            router.navigate(RecordsAllParams(finalFilters))
        }
    }

    private fun onSelectDateClick() = viewModelScope.launch {
        val useMilitaryTime = prefsInteractor.getUseMilitaryTimeFormat()
        val firstDayOfWeek = prefsInteractor.getFirstDayOfWeek()
        val current = timeMapper.toTimestampShifted(
            rangesFromToday = rangePosition,
            range = rangeLength,
        )

        router.navigate(
            DateTimeDialogParams(
                tag = DATE_TAG,
                type = DateTimeDialogType.DATE,
                timestamp = current,
                useMilitaryTime = useMilitaryTime,
                firstDayOfWeek = firstDayOfWeek,
            ),
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
        updateStreaksGoalViewData()
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
        updateNextActivitiesViewData()
    }

    private fun getRangeLength(range: StatisticsDetailParams.RangeLengthParams): RangeLength {
        return when (range) {
            is StatisticsDetailParams.RangeLengthParams.Day -> RangeLength.Day
            is StatisticsDetailParams.RangeLengthParams.Week -> RangeLength.Week
            is StatisticsDetailParams.RangeLengthParams.Month -> RangeLength.Month
            is StatisticsDetailParams.RangeLengthParams.Year -> RangeLength.Year
            is StatisticsDetailParams.RangeLengthParams.All -> RangeLength.All
            is StatisticsDetailParams.RangeLengthParams.Custom -> Range(
                timeStarted = range.start, timeEnded = range.end,
            ).let(RangeLength::Custom)
            is StatisticsDetailParams.RangeLengthParams.Last -> RangeLength.Last
        }
    }

    private suspend fun openFilter(
        tag: String,
        title: String,
        filters: List<RecordsFilter>,
    ) {
        val dateFilter = recordFilterInteractor.mapDateFilter(rangeLength, rangePosition)
            ?.let(::listOf).orEmpty()

        router.navigate(
            RecordsFilterParams(
                tag = tag,
                title = title,
                dateSelectionAvailable = false,
                untrackedSelectionAvailable = true,
                multitaskSelectionAvailable = true,
                filters = filters
                    .plus(dateFilter)
                    .map(RecordsFilter::toParams).toList(),
            ),
        )
    }

    private suspend fun getDailyGoalType(
        filters: List<RecordsFilter>,
    ): RecordTypeGoal.Type? {
        return statisticsDetailGetGoalFromFilterInteractor.execute(filters)
            .getDaily()?.type
    }

    private suspend fun loadRecordsCache() {
        // Load all records without date filter for faster date selection.
        records = recordFilterInteractor.getByFilter(filter)
        compareRecords = recordFilterInteractor.getByFilter(comparisonFilter)
    }

    private suspend fun getDailyGoal(): RecordTypeGoal.Type? {
        // Initialize if null.
        val goal = dailyGoal
        return if (goal == null) {
            getDailyGoalType(filter)
                .also { dailyGoal = Result.success(it) }
        } else {
            goal.getOrNull()
        }
    }

    private suspend fun getCompareDailyGoal(): RecordTypeGoal.Type? {
        // Initialize if null.
        val goal = compareDailyGoal
        return if (goal == null) {
            getDailyGoalType(comparisonFilter)
                .also { compareDailyGoal = Result.success(it) }
        } else {
            goal.getOrNull()
        }
    }

    private fun updatePreviewViewData() = viewModelScope.launch {
        previewViewData.set(loadPreviewViewData())
        updateContent()
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
        updateContent()
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
            rangePosition = rangePosition,
        )
    }

    private fun updateStreaksTypeViewData() {
        streaksTypeViewData.set(loadStreaksTypeViewData())
        updateContent()
    }

    private fun loadStreaksTypeViewData(): List<ViewHolderType> {
        return streaksInteractor.mapToStreaksTypeViewData(streaksType)
    }

    private fun updateStreaksGoalViewData() = viewModelScope.launch {
        streaksGoalViewData.set(loadStreaksGoalViewData())
        updateContent()
    }

    private suspend fun loadStreaksGoalViewData(): List<ViewHolderType> {
        return streaksInteractor.mapToStreaksGoalViewData(
            streaksGoal = streaksGoal,
            dailyGoal = getDailyGoal(),
            compareGoalType = getCompareDailyGoal(),
            rangeLength = rangeLength,
        )
    }

    private fun updateStreaksViewData() = viewModelScope.launch {
        streaksViewData.set(loadStreaksViewData())
        updateContent()
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
            streaksGoal = streaksGoal,
            goalType = getDailyGoal(),
            compareGoalType = getCompareDailyGoal(),
        )
    }

    private fun updateChartViewData() = viewModelScope.launch {
        val data = loadChartViewData()
        chartViewData.set(data)
        chartGrouping = data.appliedChartGrouping
        chartLength = data.appliedChartLength
        updateContent()
    }

    private fun loadEmptyChartViewData(): StatisticsDetailChartCompositeViewData {
        return chartInteractor.getEmptyChartViewData()
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
        updateContent()
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
            splitChartGrouping = grouping,
        )
    }

    private fun updateDurationSplitChartViewData() = viewModelScope.launch {
        durationSplitChartViewData
            .set(loadDurationSplitChartViewData(isForComparison = false))
        comparisonDurationSplitChartViewData
            .set(loadDurationSplitChartViewData(isForComparison = true))
        updateContent()
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
        updateContent()
    }

    private fun loadSplitChartGroupingViewData(): List<ViewHolderType> {
        return mapper.mapToSplitChartGroupingViewData(rangeLength, splitChartGrouping)
    }

    private fun updateNextActivitiesViewData() = viewModelScope.launch {
        nextActivitiesViewData.set(loadNextActivitiesViewData())
        updateContent()
    }

    private suspend fun loadNextActivitiesViewData(): List<ViewHolderType> {
        return adjacentActivitiesInteractor.getNextActivitiesViewData(
            filter = filter,
            rangeLength = rangeLength,
            rangePosition = rangePosition,
        )
    }

    private fun updateTitle() = viewModelScope.launch {
        title.set(loadTitle())
    }

    private suspend fun loadTitle(): String {
        val startOfDayShift = prefsInteractor.getStartOfDayShift()
        val firstDayOfWeek = prefsInteractor.getFirstDayOfWeek()
        return rangeViewDataMapper.mapToTitle(
            rangeLength = rangeLength,
            position = rangePosition,
            startOfDayShift = startOfDayShift,
            firstDayOfWeek = firstDayOfWeek,
        )
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

    private fun updateContent() {
        content.set(loadContent())
    }

    // TODO STATS expand appbar on short list.
    // TODO STATS fix charts always animating on appear
    private fun loadContent(): List<ViewHolderType> {
        val result = mutableListOf<ViewHolderType>()

        fun getPreviewColor(): Int {
            return previewViewData.value
                ?.data?.color
                ?: Color.BLACK
        }

        fun getPreviewColorComparison(): Int {
            return previewViewData.value
                ?.comparisonData
                ?.filterIsInstance<StatisticsDetailPreviewViewData>()
                ?.firstOrNull()
                ?.color
                ?: Color.BLACK
        }

        previewViewData.value?.let { viewData ->
            val rest: List<ViewHolderType> = viewData.additionalData + viewData.comparisonData
            result += StatisticsDetailPreviewsViewData(
                block = StatisticsDetailBlock.PreviewItems,
                data = rest,
            )
        }

        chartViewData.value?.let { viewData ->
            val comparisonChartIsVisible = viewData.showComparison &&
                viewData.chartData.visible &&
                viewData.compareChartData.visible

            if (viewData.chartData.visible) {
                result += StatisticsDetailBarChartViewData(
                    block = StatisticsDetailBlock.ChartData,
                    color = getPreviewColor(),
                    marginTopDp = 16,
                    data = viewData.chartData,
                )
            }

            if (viewData.compareChartData.visible && comparisonChartIsVisible) {
                result += StatisticsDetailBarChartViewData(
                    block = StatisticsDetailBlock.ChartDataComparison,
                    color = getPreviewColorComparison(),
                    marginTopDp = 16,
                    data = viewData.compareChartData,
                )
            }

            if (viewData.chartGroupingVisible) {
                result += StatisticsDetailButtonsRowViewData(
                    block = StatisticsDetailBlock.ChartGrouping,
                    marginTopDp = 4,
                    data = viewData.chartGroupingViewData,
                )
            }

            if (viewData.chartLengthVisible) {
                result += StatisticsDetailButtonsRowViewData(
                    block = StatisticsDetailBlock.ChartLength,
                    marginTopDp = -10,
                    data = viewData.chartLengthViewData,
                )
            }

            val rangeAveragesData = viewData.rangeAverages
            if (rangeAveragesData.isNotEmpty()) {
                result += StatisticsDetailCardViewData(
                    block = StatisticsDetailBlock.RangeAverages,
                    title = viewData.rangeAveragesTitle,
                    marginTopDp = 0,
                    data = rangeAveragesData,
                )
            }
        }

        statsViewData.value?.let { viewData ->
            result += StatisticsDetailCardDoubleViewData(
                block = StatisticsDetailBlock.Total,
                first = viewData.totalDuration,
                second = viewData.timesTracked,
            )
            result += StatisticsDetailCardViewData(
                block = StatisticsDetailBlock.Average,
                title = resourceRepo.getString(R.string.statistics_detail_record_length),
                marginTopDp = 4,
                data = viewData.averageRecord,
            )
            result += StatisticsDetailCardViewData(
                block = StatisticsDetailBlock.Dates,
                title = resourceRepo.getString(R.string.statistics_detail_record_time),
                marginTopDp = 4,
                data = viewData.datesTracked,
            )
        }

        streaksViewData.value?.let { viewData ->
            result += StatisticsDetailCardViewData(
                block = StatisticsDetailBlock.Series,
                title = resourceRepo.getString(R.string.statistics_detail_streaks),
                marginTopDp = 4,
                data = viewData.streaks,
            )
        }

        streaksGoalViewData.value?.let { viewData ->
            if (viewData.isNotEmpty()) {
                result += StatisticsDetailButtonsRowViewData(
                    block = StatisticsDetailBlock.SeriesGoal,
                    marginTopDp = 0,
                    data = viewData,
                )
            }
        }

        streaksViewData.value?.let { viewData ->
            if (viewData.showData) {
                result += StatisticsDetailSeriesChartViewData(
                    block = StatisticsDetailBlock.SeriesChart,
                    color = getPreviewColor(),
                    data = viewData.data,
                )
            }
            if (viewData.showComparison) {
                result += StatisticsDetailSeriesChartViewData(
                    block = StatisticsDetailBlock.SeriesChartComparison,
                    color = getPreviewColorComparison(),
                    data = viewData.compareData,
                )
            }
            if (viewData.showData) {
                result += StatisticsDetailButtonsRowViewData(
                    block = StatisticsDetailBlock.SeriesType,
                    marginTopDp = 4,
                    data = streaksTypeViewData.value.orEmpty(),
                )
            }
            if (viewData.showCalendar) {
                result += StatisticsDetailSeriesCalendarViewData(
                    block = StatisticsDetailBlock.SeriesCalendar,
                    color = getPreviewColor(),
                    data = viewData.calendarData,
                )
            }
            if (viewData.showComparisonCalendar) {
                result += StatisticsDetailSeriesCalendarViewData(
                    block = StatisticsDetailBlock.SeriesCalendarComparison,
                    color = getPreviewColorComparison(),
                    data = viewData.compareCalendarData,
                )
            }
        }

        splitChartViewData.value?.let { viewData ->
            if (viewData.visible) {
                result += StatisticsDetailHintViewData(
                    block = StatisticsDetailBlock.SplitHint,
                    text = resourceRepo.getString(R.string.statistics_detail_day_split_hint),
                )
                result += StatisticsDetailBarChartViewData(
                    block = StatisticsDetailBlock.SplitChart,
                    color = getPreviewColor(),
                    marginTopDp = 0,
                    data = viewData,
                )
            }
        }

        comparisonSplitChartViewData.value?.let { viewData ->
            if (viewData.visible) {
                result += StatisticsDetailBarChartViewData(
                    block = StatisticsDetailBlock.SplitChartComparison,
                    color = getPreviewColorComparison(),
                    marginTopDp = 0,
                    data = viewData,
                )
            }
        }

        splitChartGroupingViewData.value?.let { viewData ->
            if (viewData.isNotEmpty()) {
                result += StatisticsDetailButtonsRowViewData(
                    block = StatisticsDetailBlock.SplitChartGrouping,
                    marginTopDp = 4,
                    data = viewData,
                )
            }
        }

        durationSplitChartViewData.value?.let { viewData ->
            if (viewData.visible) {
                result += StatisticsDetailHintViewData(
                    block = StatisticsDetailBlock.DurationSplitHint,
                    text = resourceRepo.getString(R.string.statistics_detail_duration_split_hint),
                )
                result += StatisticsDetailBarChartViewData(
                    block = StatisticsDetailBlock.DurationSplitChart,
                    color = getPreviewColor(),
                    marginTopDp = 0,
                    data = viewData,
                )
            }
        }

        comparisonDurationSplitChartViewData.value?.let { viewData ->
            if (viewData.visible) {
                result += StatisticsDetailBarChartViewData(
                    block = StatisticsDetailBlock.DurationSplitChartComparison,
                    color = getPreviewColorComparison(),
                    marginTopDp = 0,
                    data = viewData,
                )
            }
        }

        nextActivitiesViewData.value?.let { viewData ->
            if (viewData.isNotEmpty()) {
                result += StatisticsDetailNextActivitiesViewData(
                    block = StatisticsDetailBlock.NextActivities,
                    data = viewData,
                )
            }
        }

        statsViewData.value?.let { viewData ->
            result += viewData.splitData
        }

        return result
    }

    companion object {
        private const val DATE_TAG = "statistics_detail_date_tag"
        private const val FILTER_TAG = "statistics_detail_filter_tag"
        private const val COMPARE_TAG = "statistics_detail_compare_tag"
    }
}
