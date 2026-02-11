package com.example.util.simpletimetracker.feature_goals.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.util.simpletimetracker.core.base.SingleLiveEvent
import com.example.util.simpletimetracker.core.delegates.dateSelector.mapper.DateSelectorMapper
import com.example.util.simpletimetracker.core.delegates.dateSelector.viewModelDelegate.DateSelectorViewModelDelegate
import com.example.util.simpletimetracker.core.extension.set
import com.example.util.simpletimetracker.core.extension.shiftTimeStamp
import com.example.util.simpletimetracker.core.interactor.StatisticsDetailNavigationInteractor
import com.example.util.simpletimetracker.core.mapper.TimeMapper
import com.example.util.simpletimetracker.core.model.NavigationTab
import com.example.util.simpletimetracker.domain.prefs.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.recordType.extension.toRangeLength
import com.example.util.simpletimetracker.domain.recordType.interactor.RecordTypeGoalInteractor
import com.example.util.simpletimetracker.domain.statistics.model.RangeLength
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.loader.LoaderViewData
import com.example.util.simpletimetracker.feature_base_adapter.statisticsGoal.StatisticsGoalViewData
import com.example.util.simpletimetracker.feature_goals.interactor.GoalsViewDataInteractor
import com.example.util.simpletimetracker.navigation.Router
import com.example.util.simpletimetracker.navigation.params.screen.DateTimeDialogParams
import com.example.util.simpletimetracker.navigation.params.screen.DateTimeDialogType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GoalsViewModel @Inject constructor(
    private val goalsViewDataInteractor: GoalsViewDataInteractor,
    private val statisticsDetailNavigationInteractor: StatisticsDetailNavigationInteractor,
    private val router: Router,
    private val prefsInteractor: PrefsInteractor,
    private val timeMapper: TimeMapper,
    private val recordTypeGoalInteractor: RecordTypeGoalInteractor,
    val dateSelectorViewModelDelegate: DateSelectorViewModelDelegate,
) : ViewModel() {

    private var currentShift: Int = 0

    init {
        dateSelectorViewModelDelegate.attach(getDateSelectorDelegateParent())
    }

    val goals: LiveData<List<ViewHolderType>> by lazy {
        MutableLiveData(listOf(LoaderViewData()))
    }
    val resetScreen: SingleLiveEvent<Unit> = SingleLiveEvent()

    private var isVisible: Boolean = false
    private var timerJob: Job? = null

    fun initialize() {
        viewModelScope.launch {
            dateSelectorViewModelDelegate.initialize(currentShift)
        }
    }

    fun onVisible() {
        isVisible = true
        startUpdate()
    }

    fun onHidden() {
        isVisible = false
        stopUpdate()
    }

    fun onTabReselected(tab: NavigationTab?) {
        if (isVisible && tab is NavigationTab.Goals) {
            resetScreen.set(Unit)
        }
    }

    fun onGoalClick(item: StatisticsGoalViewData) = viewModelScope.launch {
        val goal = recordTypeGoalInteractor.get(item.id) ?: return@launch
        val rangeShift = if (prefsInteractor.getKeepStatisticsRange()) {
            goalsViewDataInteractor.getRangeShift(
                dayShift = currentShift,
                goalRange = goal.range,
            )
        } else {
            0
        }
        statisticsDetailNavigationInteractor.navigateByGoal(
            goalId = item.id,
            shift = rangeShift,
            range = goal.range.toRangeLength() ?: return@launch,
        )
    }

    fun onDateTimeSet(timestamp: Long, tag: String?) = viewModelScope.launch {
        if (tag != DATE_TAG) return@launch

        val startOfDayShift = prefsInteractor.getStartOfDayShift()
        val firstDayOfWeek = prefsInteractor.getFirstDayOfWeek()
        val newShift = timeMapper.toTimestampShift(
            toTime = timestamp.shiftTimeStamp(startOfDayShift),
            range = RangeLength.Day,
            firstDayOfWeek = firstDayOfWeek,
        ).toInt()

        updatePosition(newShift)
    }

    private fun onSelectDateClick() = viewModelScope.launch {
        val useMilitaryTime = prefsInteractor.getUseMilitaryTimeFormat()
        val firstDayOfWeek = prefsInteractor.getFirstDayOfWeek()
        val startOfDayShift = prefsInteractor.getStartOfDayShift()
        val timestamp = timeMapper.toTimestampShifted(
            rangesFromToday = currentShift,
            range = RangeLength.Day,
            startOfDayShift = startOfDayShift,
        )

        router.navigate(
            DateTimeDialogParams(
                tag = DATE_TAG,
                type = DateTimeDialogType.DATE,
                timestamp = timestamp,
                useMilitaryTime = useMilitaryTime,
                firstDayOfWeek = firstDayOfWeek,
            ),
        )
    }

    private fun updateStatistics() = viewModelScope.launch {
        val data = loadStatisticsViewData()
        goals.set(data)
    }

    private suspend fun loadStatisticsViewData(): List<ViewHolderType> {
        return goalsViewDataInteractor.getViewData(currentShift)
    }

    private fun startUpdate() {
        timerJob = viewModelScope.launch {
            timerJob?.cancelAndJoin()
            while (isActive) {
                updateStatistics()
                delay(TIMER_UPDATE)
            }
        }
    }

    private fun stopUpdate() {
        viewModelScope.launch {
            timerJob?.cancelAndJoin()
        }
    }

    private fun updatePosition(newPosition: Int) {
        currentShift = newPosition
        dateSelectorViewModelDelegate.updatePosition(newPosition)
        updateStatistics()
    }

    private fun getDateSelectorDelegateParent(): DateSelectorViewModelDelegate.Parent {
        return object : DateSelectorViewModelDelegate.Parent {
            override val currentPosition: Int
                get() = currentShift

            override fun onDateClick() {
                onSelectDateClick()
            }

            override fun updatePosition(newPosition: Int) {
                this@GoalsViewModel.updatePosition(newPosition)
            }

            override suspend fun getSetupData(): DateSelectorMapper.SetupData.Type {
                return DateSelectorMapper.SetupData.Type.Statistics(
                    optionsButton = DateSelectorMapper.SetupData.Button.Hidden,
                    rangeLength = RangeLength.Day,
                )
            }
        }
    }

    companion object {
        private const val DATE_TAG = "goals_date_tag"
        private const val TIMER_UPDATE = 1000L
    }
}
