package com.example.util.simpletimetracker.feature_statistics.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.util.simpletimetracker.core.base.BaseViewModel
import com.example.util.simpletimetracker.core.delegates.dateSelector.mapper.DateSelectorMapper
import com.example.util.simpletimetracker.core.delegates.dateSelector.viewModelDelegate.DateSelectorViewModelDelegate
import com.example.util.simpletimetracker.core.extension.set
import com.example.util.simpletimetracker.core.extension.shiftTimeStamp
import com.example.util.simpletimetracker.core.extension.toModel
import com.example.util.simpletimetracker.core.mapper.RangeViewDataMapper
import com.example.util.simpletimetracker.core.mapper.TimeMapper
import com.example.util.simpletimetracker.core.viewData.RangeSelectionOptionsListItem
import com.example.util.simpletimetracker.domain.daysOfWeek.interactor.GetProcessedLastDaysCountInteractor
import com.example.util.simpletimetracker.domain.extension.orZero
import com.example.util.simpletimetracker.domain.prefs.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.record.interactor.StatisticsUpdateInteractor
import com.example.util.simpletimetracker.domain.record.model.Range
import com.example.util.simpletimetracker.domain.statistics.model.RangeLength
import com.example.util.simpletimetracker.feature_statistics.api.StatisticsContainerOptionsListItem
import com.example.util.simpletimetracker.feature_statistics.api.StatisticsContainerOptionsListMapper
import com.example.util.simpletimetracker.navigation.Router
import com.example.util.simpletimetracker.navigation.params.screen.CustomRangeSelectionParams
import com.example.util.simpletimetracker.navigation.params.screen.DateTimeDialogParams
import com.example.util.simpletimetracker.navigation.params.screen.DateTimeDialogType
import com.example.util.simpletimetracker.navigation.params.screen.DurationDialogParams
import com.example.util.simpletimetracker.navigation.params.screen.OptionsListParams
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StatisticsContainerViewModel @Inject constructor(
    val dateSelectorViewModelDelegate: DateSelectorViewModelDelegate,
    private val router: Router,
    private val timeMapper: TimeMapper,
    private val rangeViewDataMapper: RangeViewDataMapper,
    private val prefsInteractor: PrefsInteractor,
    private val statisticsUpdateInteractor: StatisticsUpdateInteractor,
    private val statisticsContainerOptionsListMapper: StatisticsContainerOptionsListMapper,
    private val getProcessedLastDaysCountInteractor: GetProcessedLastDaysCountInteractor,
) : BaseViewModel() {

    val position: LiveData<Int> by lazy {
        return@lazy MutableLiveData(0)
    }

    private var rangeLength: RangeLength? = null
    private val currentPosition: Int get() = position.value.orZero()

    init {
        dateSelectorViewModelDelegate.attach(getDateSelectorDelegateParent())
    }

    fun initialize() {
        viewModelScope.launch {
            dateSelectorViewModelDelegate.initialize(currentPosition)
        }
    }

    fun onVisible() {
        // TODO update only when necessary?
        viewModelScope.launch {
            dateSelectorViewModelDelegate.setup()
            dateSelectorViewModelDelegate.updatePosition(currentPosition)
        }
    }

    fun onOptionsClick() = viewModelScope.launch {
        val items = statisticsContainerOptionsListMapper.map(
            filterHidden = true,
            rangeLength = getRangeLength(),
        )
        when {
            items.isEmpty() -> return@launch
            items.size == 1 -> items.firstOrNull()?.id?.let(::onOptionsItemClick)
            else -> router.navigate(OptionsListParams(items))
        }
    }

    fun onOptionsLongClick() = viewModelScope.launch {
        statisticsUpdateInteractor.sendFilterClicked()
    }

    fun onRangeSelected(id: RangeSelectionOptionsListItem) {
        when (id) {
            is RangeSelectionOptionsListItem.SelectDate -> {
                onSelectDateClick()
            }
            is RangeSelectionOptionsListItem.Custom -> {
                onSelectCustomRangeClick()
            }
            is RangeSelectionOptionsListItem.Last -> {
                onSelectLastDaysClick()
            }
            is RangeSelectionOptionsListItem.Simple -> viewModelScope.launch {
                onRangeUpdated(id.rangeLengthParams.toModel())
            }
        }
    }

    fun onDateTimeSet(timestamp: Long, tag: String?) = viewModelScope.launch {
        val startOfDayShift = prefsInteractor.getStartOfDayShift()
        when (tag) {
            DATE_TAG -> {
                timeMapper.toTimestampShift(
                    toTime = timestamp.shiftTimeStamp(startOfDayShift),
                    range = prefsInteractor.getStatisticsRange(),
                    firstDayOfWeek = prefsInteractor.getFirstDayOfWeek(),
                ).toInt().let(::updatePosition)
            }
        }
    }

    fun onCustomRangeSelected(range: Range) = viewModelScope.launch {
        onRangeUpdated(RangeLength.Custom(range))
    }

    fun onCountSet(count: Long, tag: String?) = viewModelScope.launch {
        if (tag != LAST_DAYS_COUNT_TAG) return@launch

        val lastDaysCount = getProcessedLastDaysCountInteractor.execute(count)
        onRangeUpdated(RangeLength.Last(lastDaysCount))
    }

    fun onOptionsItemClick(id: OptionsListParams.Item.Id) = viewModelScope.launch {
        if (id !is StatisticsContainerOptionsListItem) return@launch
        when (id) {
            is StatisticsContainerOptionsListItem.Filter -> {
                statisticsUpdateInteractor.sendFilterClicked()
            }
            is StatisticsContainerOptionsListItem.Share -> {
                statisticsUpdateInteractor.sendShareClicked()
            }
            is StatisticsContainerOptionsListItem.BackToToday -> {
                onBackToTodayClick()
            }
            is StatisticsContainerOptionsListItem.SelectDate -> {
                onSelectDateClick()
            }
            is StatisticsContainerOptionsListItem.SelectRange -> {
                onSelectRangeClick()
            }
        }
    }

    fun onOptionsDialogOpened() {
        viewModelScope.launch {
            statisticsUpdateInteractor.sendOptionsVisible(isVisible = true)
        }
    }

    fun onOptionsDialogClosed() {
        viewModelScope.launch {
            statisticsUpdateInteractor.sendOptionsVisible(isVisible = false)
        }
    }

    private suspend fun onRangeUpdated(newRange: RangeLength) {
        prefsInteractor.setStatisticsRange(newRange)
        statisticsUpdateInteractor.sendRangeChanged(newRange)

        if (newRange != rangeLength) {
            rangeLength = newRange
            dateSelectorViewModelDelegate.setup()
            updatePosition(0)
        }
    }

    private fun onBackToTodayClick() {
        updatePosition(0)
    }

    private fun onSelectDateClick() = viewModelScope.launch {
        val useMilitaryTime = prefsInteractor.getUseMilitaryTimeFormat()
        val firstDayOfWeek = prefsInteractor.getFirstDayOfWeek()
        val startOfDayShift = prefsInteractor.getStartOfDayShift()
        val current = timeMapper.toTimestampShifted(
            rangesFromToday = currentPosition,
            range = prefsInteractor.getStatisticsRange(),
            startOfDayShift = startOfDayShift,
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

    private fun onSelectCustomRangeClick() = viewModelScope.launch {
        val currentCustomRange = (prefsInteractor.getStatisticsRange() as? RangeLength.Custom)?.range

        CustomRangeSelectionParams(
            rangeStart = currentCustomRange?.timeStarted,
            rangeEnd = currentCustomRange?.timeEnded,
        ).let(router::navigate)
    }

    private fun onSelectLastDaysClick() = viewModelScope.launch {
        DurationDialogParams(
            tag = LAST_DAYS_COUNT_TAG,
            value = DurationDialogParams.Value.Count(
                getCurrentLastDaysCount().toLong(),
            ),
            hideDisableButton = true,
        ).let(router::navigate)
    }

    private fun onSelectRangeClick() = viewModelScope.launch {
        val data = rangeViewDataMapper.mapToRangesOptions(
            currentRange = getRangeLength(),
            addSelection = true,
            lastDaysCount = getCurrentLastDaysCount(),
        )
        router.navigate(data)
    }

    private suspend fun getCurrentLastDaysCount(): Int {
        return prefsInteractor.getStatisticsLastDays()
    }

    private suspend fun getRangeLength(): RangeLength {
        return rangeLength ?: prefsInteractor.getStatisticsRange()
    }

    private fun getDateSelectorDelegateParent(): DateSelectorViewModelDelegate.Parent {
        return object : DateSelectorViewModelDelegate.Parent {
            override val currentPosition: Int
                get() = this@StatisticsContainerViewModel.currentPosition

            override fun onDateClick() {
                this@StatisticsContainerViewModel.onSelectRangeClick()
            }

            override fun updatePosition(newPosition: Int) =
                this@StatisticsContainerViewModel.updatePosition(newPosition)

            override suspend fun getSetupData(): DateSelectorMapper.SetupData.Type {
                return DateSelectorMapper.SetupData.Type.Statistics(
                    optionsButton = dateSelectorViewModelDelegate.getOptionsButton(
                        options = statisticsContainerOptionsListMapper.map(
                            filterHidden = true,
                            rangeLength = getRangeLength(),
                        ),
                    ),
                    rangeLength = getRangeLength(),
                )
            }
        }
    }

    private fun updatePosition(newPosition: Int) {
        dateSelectorViewModelDelegate.updatePosition(newPosition)
        position.set(newPosition)
    }

    companion object {
        private const val LAST_DAYS_COUNT_TAG = "statistics_last_days_count_tag"
        private const val DATE_TAG = "statistics_date_tag"
    }
}
