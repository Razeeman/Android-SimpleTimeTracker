package com.example.util.simpletimetracker.feature_records.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.util.simpletimetracker.core.base.SingleLiveEvent
import com.example.util.simpletimetracker.core.extension.set
import com.example.util.simpletimetracker.core.extension.toParams
import com.example.util.simpletimetracker.core.model.NavigationTab
import com.example.util.simpletimetracker.domain.extension.orZero
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.loader.LoaderViewData
import com.example.util.simpletimetracker.feature_base_adapter.record.RecordViewData
import com.example.util.simpletimetracker.feature_base_adapter.runningRecord.RunningRecordViewData
import com.example.util.simpletimetracker.feature_records.customView.RecordsCalendarViewData
import com.example.util.simpletimetracker.feature_records.extra.RecordsExtra
import com.example.util.simpletimetracker.feature_records.interactor.RecordsViewDataInteractor
import com.example.util.simpletimetracker.feature_records.model.RecordsState
import com.example.util.simpletimetracker.navigation.Router
import com.example.util.simpletimetracker.navigation.params.screen.ChangeRecordFromMainParams
import com.example.util.simpletimetracker.navigation.params.screen.ChangeRecordParams
import com.example.util.simpletimetracker.navigation.params.screen.ChangeRunningRecordParams
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RecordsViewModel @Inject constructor(
    private val router: Router,
    private val recordsViewDataInteractor: RecordsViewDataInteractor,
    private val prefsInteractor: PrefsInteractor,
) : ViewModel() {

    var extra: RecordsExtra? = null

    val isCalendarView: LiveData<Boolean> = MutableLiveData()
    val records: LiveData<List<ViewHolderType>> by lazy {
        MutableLiveData(listOf(LoaderViewData() as ViewHolderType))
    }
    val calendarData: LiveData<RecordsCalendarViewData> = MutableLiveData()
    val resetScreen: SingleLiveEvent<Unit> = SingleLiveEvent()

    private var isVisible: Boolean = false
    private var timerJob: Job? = null
    private val shift: Int get() = extra?.shift.orZero()

    fun onCalendarClick(item: ViewHolderType) {
        when (item) {
            is RecordViewData -> onRecordClick(item)
            is RunningRecordViewData -> onRunningRecordClick(item)
        }
    }

    fun onRunningRecordClick(item: RunningRecordViewData, sharedElements: Pair<Any, String>? = null) {
        router.navigate(
            data = ChangeRunningRecordParams(
                transitionName = sharedElements?.second.orEmpty(),
                id = item.id,
                from = ChangeRunningRecordParams.From.Records,
                preview = ChangeRunningRecordParams.Preview(
                    name = item.name,
                    tagName = item.tagName,
                    timeStarted = item.timeStarted,
                    duration = item.timer,
                    goalTime = item.goalTime.toParams(),
                    goalTime2 = item.goalTime2.toParams(),
                    goalTime3 = item.goalTime3.toParams(),
                    goalTime4 = item.goalTime4.toParams(),
                    iconId = item.iconId.toParams(),
                    color = item.color,
                    comment = item.comment
                )
            ),
            sharedElements = sharedElements?.let(::mapOf) ?: emptyMap()
        )
    }

    fun onRecordClick(item: RecordViewData, sharedElements: Pair<Any, String>? = null) {
        val preview = ChangeRecordParams.Preview(
            name = item.name,
            tagName = item.tagName,
            timeStarted = item.timeStarted,
            timeFinished = item.timeFinished,
            duration = item.duration,
            iconId = item.iconId.toParams(),
            color = item.color,
            comment = item.comment
        )

        val params = when (item) {
            is RecordViewData.Tracked -> ChangeRecordParams.Tracked(
                transitionName = sharedElements?.second.orEmpty(),
                id = item.id,
                from = ChangeRecordParams.From.Records,
                preview = preview
            )
            is RecordViewData.Untracked -> ChangeRecordParams.Untracked(
                transitionName = sharedElements?.second.orEmpty(),
                timeStarted = item.timeStartedTimestamp,
                timeEnded = item.timeEndedTimestamp,
                preview = preview
            )
        }
        router.navigate(
            data = ChangeRecordFromMainParams(params),
            sharedElements = sharedElements?.let(::mapOf) ?: emptyMap()
        )
    }

    fun onVisible() {
        isVisible = true
        if (shift == 0) {
            startUpdate()
        } else {
            updateRecords()
        }
    }

    fun onHidden() {
        isVisible = false
        stopUpdate()
    }

    fun onNeedUpdate() {
        updateRecords()
    }

    fun onTabReselected(tab: NavigationTab?) {
        if (isVisible && tab is NavigationTab.Records) {
            resetScreen.set(Unit)
        }
    }

    private fun updateRecords() = viewModelScope.launch {
        isCalendarView.set(prefsInteractor.getShowRecordsCalendar())

        when (val state = loadRecordsViewData()) {
            is RecordsState.RecordsData -> records.set(state.data)
            is RecordsState.CalendarData -> calendarData.set(state.data)
        }
    }

    private suspend fun loadRecordsViewData(): RecordsState {
        return recordsViewDataInteractor.getViewData(shift)
    }

    private fun startUpdate() {
        timerJob = viewModelScope.launch {
            timerJob?.cancelAndJoin()
            while (isActive) {
                updateRecords()
                delay(TIMER_UPDATE)
            }
        }
    }

    private fun stopUpdate() {
        viewModelScope.launch {
            timerJob?.cancelAndJoin()
        }
    }

    companion object {
        private const val TIMER_UPDATE = 1000L
    }
}
