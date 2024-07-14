package com.example.util.simpletimetracker.feature_statistics_detail.viewModel.delegate

import androidx.lifecycle.LiveData
import com.example.util.simpletimetracker.core.base.ViewModelDelegate
import com.example.util.simpletimetracker.core.extension.lazySuspend
import com.example.util.simpletimetracker.core.extension.set
import com.example.util.simpletimetracker.core.interactor.RecordFilterInteractor
import com.example.util.simpletimetracker.core.mapper.RangeViewDataMapper
import com.example.util.simpletimetracker.core.mapper.TimeMapper
import com.example.util.simpletimetracker.core.viewData.RangeViewData
import com.example.util.simpletimetracker.core.viewData.RangesViewData
import com.example.util.simpletimetracker.core.viewData.SelectDateViewData
import com.example.util.simpletimetracker.core.viewData.SelectLastDaysViewData
import com.example.util.simpletimetracker.core.viewData.SelectRangeViewData
import com.example.util.simpletimetracker.domain.interactor.GetProcessedLastDaysCountInteractor
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.model.Range
import com.example.util.simpletimetracker.domain.model.RangeLength
import com.example.util.simpletimetracker.domain.model.RecordsFilter
import com.example.util.simpletimetracker.feature_views.spinner.CustomSpinner
import com.example.util.simpletimetracker.navigation.Router
import com.example.util.simpletimetracker.navigation.params.screen.CustomRangeSelectionParams
import com.example.util.simpletimetracker.navigation.params.screen.DateTimeDialogParams
import com.example.util.simpletimetracker.navigation.params.screen.DateTimeDialogType
import com.example.util.simpletimetracker.navigation.params.screen.DurationDialogParams
import com.example.util.simpletimetracker.navigation.params.screen.StatisticsDetailParams
import kotlinx.coroutines.launch
import javax.inject.Inject

class StatisticsDetailRangeViewModelDelegate @Inject constructor(
    private val router: Router,
    private val rangeViewDataMapper: RangeViewDataMapper,
    private val prefsInteractor: PrefsInteractor,
    private val timeMapper: TimeMapper,
    private val recordFilterInteractor: RecordFilterInteractor,
    private val getProcessedLastDaysCountInteractor: GetProcessedLastDaysCountInteractor,
) : StatisticsDetailViewModelDelegate, ViewModelDelegate() {

    val title: LiveData<String> by lazySuspend { loadTitle() }
    val rangeItems: LiveData<RangesViewData> by lazySuspend { loadRanges() }
    val rangeButtonsVisibility: LiveData<Boolean> by lazySuspend { loadButtonsVisibility() }

    private var parent: StatisticsDetailViewModelDelegate.Parent? = null
    private var rangeLength: RangeLength = RangeLength.All
    private var rangePosition: Int = 0

    override fun attach(parent: StatisticsDetailViewModelDelegate.Parent) {
        this.parent = parent
    }

    fun initialize(extra: StatisticsDetailParams) {
        rangeLength = getRangeLength(extra.range)
        rangePosition = extra.shift
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
            is SelectLastDaysViewData -> {
                onSelectLastDaysClick()
                updateRanges()
            }
            is RangeViewData -> {
                rangeLength = item.range
                onRangeChanged()
            }
        }
    }

    fun onDateTimeSet(timestamp: Long, tag: String?) = delegateScope.launch {
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

    fun onCountSet(count: Long, tag: String?) {
        if (tag != LAST_DAYS_COUNT_TAG) return

        val lastDaysCount = getProcessedLastDaysCountInteractor.execute(count)
        rangeLength = RangeLength.Last(lastDaysCount)
        onRangeChanged()
    }

    private fun onSelectDateClick() = delegateScope.launch {
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

    suspend fun getDateFilter(): List<RecordsFilter> {
        return recordFilterInteractor.mapDateFilter(
            rangeLength = rangeLength,
            rangePosition = rangePosition,
        )?.let(::listOf).orEmpty()
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
            is StatisticsDetailParams.RangeLengthParams.Last -> RangeLength.Last(
                days = range.days,
            )
        }
    }

    fun provideRangeLength(): RangeLength {
        return rangeLength
    }

    fun provideRangePosition(): Int {
        return rangePosition
    }

    private fun onSelectRangeClick() = delegateScope.launch {
        val currentCustomRange = (rangeLength as? RangeLength.Custom)?.range

        CustomRangeSelectionParams(
            rangeStart = currentCustomRange?.timeStarted,
            rangeEnd = currentCustomRange?.timeEnded,
        ).let(router::navigate)
    }

    // TODO add custom range reopen same as last days
    private fun onSelectLastDaysClick() = delegateScope.launch {
        DurationDialogParams(
            tag = LAST_DAYS_COUNT_TAG,
            value = DurationDialogParams.Value.Count(
                getCurrentLastDaysCount().toLong(),
            ),
            hideDisableButton = true,
        ).let(router::navigate)
    }

    private suspend fun getCurrentLastDaysCount(): Int {
        return (rangeLength as? RangeLength.Last)?.days
            ?: prefsInteractor.getStatisticsDetailLastDays()
    }

    private fun onRangeChanged() = delegateScope.launch {
        prefsInteractor.setStatisticsDetailRange(rangeLength)
        parent?.onRangeChanged()
        updatePosition(0)
    }

    private fun updatePosition(newPosition: Int) {
        rangePosition = newPosition
        updateTitle()
        updateRanges()
        updateButtonsVisibility()
        parent?.updateViewData()
    }

    private fun updateTitle() = delegateScope.launch {
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

    private fun updateRanges() = delegateScope.launch {
        rangeItems.set(loadRanges())
    }

    private suspend fun loadRanges(): RangesViewData {
        return rangeViewDataMapper.mapToRanges(
            currentRange = rangeLength,
            addSelection = true,
            lastDaysCount = getCurrentLastDaysCount(),
        )
    }

    private fun updateButtonsVisibility() {
        rangeButtonsVisibility.set(loadButtonsVisibility())
    }

    private fun loadButtonsVisibility(): Boolean {
        return when (rangeLength) {
            is RangeLength.All,
            is RangeLength.Custom,
            is RangeLength.Last,
            -> false
            else -> true
        }
    }

    companion object {
        private const val LAST_DAYS_COUNT_TAG = "statistics_detail_last_days_count_tag"
        private const val DATE_TAG = "statistics_detail_date_tag"
    }
}