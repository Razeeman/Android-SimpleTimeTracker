package com.example.util.simpletimetracker.feature_goals.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.util.simpletimetracker.core.base.SingleLiveEvent
import com.example.util.simpletimetracker.core.extension.set
import com.example.util.simpletimetracker.core.extension.shiftTimeStamp
import com.example.util.simpletimetracker.core.interactor.StatisticsDetailNavigationInteractor
import com.example.util.simpletimetracker.core.mapper.TimeMapper
import com.example.util.simpletimetracker.core.model.NavigationTab
import com.example.util.simpletimetracker.domain.prefs.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.recordType.extension.toRangeLength
import com.example.util.simpletimetracker.domain.recordType.interactor.RecordTypeGoalInteractor
import com.example.util.simpletimetracker.domain.recordType.model.RecordTypeGoal
import com.example.util.simpletimetracker.domain.statistics.model.RangeLength
import com.example.util.simpletimetracker.feature_base_adapter.InfiniteRecyclerAdapter
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.loader.LoaderViewData
import com.example.util.simpletimetracker.feature_base_adapter.statisticsGoal.StatisticsGoalViewData
import com.example.util.simpletimetracker.feature_date_selection.api.DateSelectorMapper
import com.example.util.simpletimetracker.feature_date_selection.api.DateSelectorViewModelDelegate
import com.example.util.simpletimetracker.feature_goals.interactor.GoalsViewDataInteractor
import com.example.util.simpletimetracker.feature_goals.mapper.GoalsOptionsListMapper
import com.example.util.simpletimetracker.feature_goals.model.GoalsOptionsListItem
import com.example.util.simpletimetracker.feature_goals.model.RangeViewData
import com.example.util.simpletimetracker.navigation.Router
import com.example.util.simpletimetracker.navigation.params.screen.DateTimeDialogParams
import com.example.util.simpletimetracker.navigation.params.screen.DateTimeDialogType
import com.example.util.simpletimetracker.navigation.params.screen.OptionsListParams
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
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
    private val goalsOptionsListMapper: GoalsOptionsListMapper,
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
    private val rangeJobs = mutableMapOf<RecordTypeGoal.Range, Job>()
    private val rangeResults = mutableMapOf<RecordTypeGoal.Range, RangeViewData>()
    private var lastRenderedDateItem: InfiniteRecyclerAdapter.Data? = null

    // Incremented when range jobs are cancelled so stale updates cannot start jobs or publish results.
    private var rangeJobsGeneration: Long = 0

    fun initialize() {
        viewModelScope.launch {
            dateSelectorViewModelDelegate.initialize(currentShift)
            lastRenderedDateItem = dateSelectorViewModelDelegate.dataProvider.getItem(currentShift)
        }
    }

    fun onVisible() {
        isVisible = true
        startUpdate()
        val dataProvider = dateSelectorViewModelDelegate.dataProvider
        if (!dataProvider.isInitialized()) return

        // System date-change events refresh it at midnight, but a custom
        // logical boundary such as 04:00 produces no system event.
        // This will update date selector on date change.
        viewModelScope.launch {
            dateSelectorViewModelDelegate.setup()
            updateDateSelectorPosition(currentShift)
        }
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

    fun onOptionsClick() = viewModelScope.launch {
        router.navigate(OptionsListParams(goalsOptionsListMapper.map()))
    }

    fun onOptionsItemClick(id: OptionsListParams.Item.Id) = viewModelScope.launch {
        if (id !is GoalsOptionsListItem) return@launch
        when (id) {
            is GoalsOptionsListItem.HideFinished -> {
                val newValue = !prefsInteractor.getHideFinishedGoals()
                prefsInteractor.setHideFinishedGoals(newValue)
                cancelRangeJobs()
                updateStatistics()
            }
        }
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

    private suspend fun updateStatistics() {
        val generation = rangeJobsGeneration
        val shift = currentShift
        val setup = goalsViewDataInteractor.getSetupData()
        if (generation != rangeJobsGeneration || shift != currentShift) return

        for (range in GOAL_RANGES_ORDER) {
            if (rangeJobs[range]?.isActive == true) continue
            rangeJobs[range] = viewModelScope.launch {
                val result = goalsViewDataInteractor.getViewDataForRange(
                    dayShift = shift,
                    goalRange = range,
                    setup = setup,
                )
                if (isActive && isVisible && generation == rangeJobsGeneration) {
                    rangeResults[range] = result
                    publishResults()
                }
            }
        }
    }

    private fun publishResults() {
        val orderedResults = GOAL_RANGES_ORDER.map(rangeResults::get)
        goals.set(goalsViewDataInteractor.combineRanges(orderedResults))
    }

    private fun startUpdate() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (isActive) {
                updateStatistics()
                delay(TIMER_UPDATE)
            }
        }
    }

    private fun stopUpdate() {
        timerJob?.cancel()
        timerJob = null
        cancelRangeJobs()
    }

    private fun cancelRangeJobs() {
        rangeJobsGeneration++
        rangeJobs.values.forEach(Job::cancel)
        rangeJobs.clear()
    }

    private fun updatePosition(newPosition: Int) {
        if (currentShift == newPosition) return
        currentShift = newPosition
        updateDateSelectorPosition(newPosition)
        cancelRangeJobs()
        viewModelScope.launch { updateStatistics() }
    }

    private fun updateDateSelectorPosition(newPosition: Int) {
        val currentItem = dateSelectorViewModelDelegate.dataProvider.getItem(currentShift)
        if (lastRenderedDateItem != currentItem) {
            dateSelectorViewModelDelegate.updatePosition(newPosition)
            lastRenderedDateItem = dateSelectorViewModelDelegate.dataProvider.getItem(newPosition)
        }
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
                    optionsButton = dateSelectorViewModelDelegate.getOptionsButton(
                        options = goalsOptionsListMapper.map(),
                    ),
                    rangeLength = RangeLength.Day,
                )
            }
        }
    }

    companion object {
        private const val DATE_TAG = "goals_date_tag"
        private const val TIMER_UPDATE = 1000L
        private val GOAL_RANGES_ORDER = listOf(
            RecordTypeGoal.Range.Daily,
            RecordTypeGoal.Range.Weekly,
            RecordTypeGoal.Range.Monthly,
            RecordTypeGoal.Range.Yearly,
            RecordTypeGoal.Range.Overall,
        )
    }
}
