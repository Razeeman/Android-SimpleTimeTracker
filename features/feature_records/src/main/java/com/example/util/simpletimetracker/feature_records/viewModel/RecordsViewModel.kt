package com.example.util.simpletimetracker.feature_records.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.util.simpletimetracker.core.base.SingleLiveEvent
import com.example.util.simpletimetracker.core.extension.set
import com.example.util.simpletimetracker.core.extension.toParams
import com.example.util.simpletimetracker.core.extension.toRecordParams
import com.example.util.simpletimetracker.core.extension.toRunningRecordParams
import com.example.util.simpletimetracker.core.mapper.TimeMapper
import com.example.util.simpletimetracker.core.model.NavigationTab
import com.example.util.simpletimetracker.domain.extension.orZero
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordsUpdateInteractor
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.loader.LoaderViewData
import com.example.util.simpletimetracker.feature_base_adapter.record.RecordViewData
import com.example.util.simpletimetracker.feature_base_adapter.runningRecord.RunningRecordViewData
import com.example.util.simpletimetracker.feature_records.extra.RecordsExtra
import com.example.util.simpletimetracker.feature_records.interactor.RecordsViewDataInteractor
import com.example.util.simpletimetracker.feature_records.model.RecordsState
import com.example.util.simpletimetracker.navigation.Router
import com.example.util.simpletimetracker.navigation.params.screen.ChangeRecordFromMainParams
import com.example.util.simpletimetracker.navigation.params.screen.ChangeRecordParams
import com.example.util.simpletimetracker.navigation.params.screen.ChangeRunningRecordFromMainParams
import com.example.util.simpletimetracker.navigation.params.screen.ChangeRunningRecordParams
import com.example.util.simpletimetracker.navigation.params.screen.RecordQuickActionsParams
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
    private val timeMapper: TimeMapper,
    private val recordsUpdateInteractor: RecordsUpdateInteractor,
) : ViewModel() {

    var extra: RecordsExtra? = null

    val isCalendarView: LiveData<Boolean> = MutableLiveData()
    val records: LiveData<List<ViewHolderType>> by lazy {
        MutableLiveData(listOf(LoaderViewData() as ViewHolderType))
    }
    val calendarData: LiveData<RecordsState.CalendarData> by lazy {
        MutableLiveData(RecordsState.CalendarData.Loading)
    }
    val resetScreen: SingleLiveEvent<Unit> = SingleLiveEvent()

    private var isVisible: Boolean = false
    private var timerJob: Job? = null
    private val shift: Int get() = extra?.shift.orZero()

    init {
        subscribeToUpdates()
    }

    fun onCalendarClick(item: ViewHolderType) {
        when (item) {
            is RecordViewData -> onRecordClick(item)
            is RunningRecordViewData -> onRunningRecordClick(item)
        }
    }

    fun onCalendarLongClick(item: ViewHolderType) {
        when (item) {
            is RecordViewData -> onRecordLongClick(item)
            is RunningRecordViewData -> onRunningRecordLongClick(item)
        }
    }

    fun onRunningRecordClick(
        item: RunningRecordViewData,
        sharedElements: Pair<Any, String>? = null,
    ) = viewModelScope.launch {
        val useMilitaryTimeFormat = prefsInteractor.getUseMilitaryTimeFormat()
        val showSeconds = prefsInteractor.getShowSeconds()

        val params = ChangeRunningRecordParams(
            transitionName = sharedElements?.second.orEmpty(),
            id = item.id,
            from = ChangeRunningRecordParams.From.Records,
            preview = ChangeRunningRecordParams.Preview(
                name = item.name,
                tagName = item.tagName,
                timeStarted = item.timeStarted,
                timeStartedDateTime = timeMapper.getFormattedDateTime(
                    time = item.timeStartedTimestamp,
                    useMilitaryTime = useMilitaryTimeFormat,
                    showSeconds = showSeconds,
                ).toRunningRecordParams(),
                duration = item.timer,
                durationTotal = item.timerTotal,
                goalTime = item.goalTime.toParams(),
                iconId = item.iconId.toParams(),
                color = item.color,
                comment = item.comment,
            ),
        )
        router.navigate(
            data = ChangeRunningRecordFromMainParams(params),
            sharedElements = sharedElements?.let(::mapOf).orEmpty(),
        )
    }

    fun onRecordClick(
        item: RecordViewData,
        sharedElements: Pair<Any, String>? = null,
    ) = viewModelScope.launch {
        val useMilitaryTimeFormat = prefsInteractor.getUseMilitaryTimeFormat()
        val showSeconds = prefsInteractor.getShowSeconds()

        val preview = ChangeRecordParams.Preview(
            name = item.name,
            tagName = item.tagName,
            timeStarted = item.timeStarted,
            timeFinished = item.timeFinished,
            timeStartedDateTime = timeMapper.getFormattedDateTime(
                time = item.timeStartedTimestamp,
                useMilitaryTime = useMilitaryTimeFormat,
                showSeconds = showSeconds,
            ).toRecordParams(),
            timeEndedDateTime = timeMapper.getFormattedDateTime(
                time = item.timeEndedTimestamp,
                useMilitaryTime = useMilitaryTimeFormat,
                showSeconds = showSeconds,
            ).toRecordParams(),
            duration = item.duration,
            iconId = item.iconId.toParams(),
            color = item.color,
            comment = item.comment,
        )

        val params = when (item) {
            is RecordViewData.Tracked -> ChangeRecordParams.Tracked(
                transitionName = sharedElements?.second.orEmpty(),
                id = item.id,
                from = ChangeRecordParams.From.Records,
                preview = preview,
            )
            is RecordViewData.Untracked -> ChangeRecordParams.Untracked(
                transitionName = sharedElements?.second.orEmpty(),
                timeStarted = item.timeStartedTimestamp,
                timeEnded = item.timeEndedTimestamp,
                preview = preview,
            )
        }
        router.navigate(
            data = ChangeRecordFromMainParams(params),
            sharedElements = sharedElements?.let(::mapOf).orEmpty(),
        )
    }

    @Suppress("UNUSED_PARAMETER")
    fun onRunningRecordLongClick(
        item: RunningRecordViewData,
        sharedElements: Pair<Any, String>? = null,
    ) {
        RecordQuickActionsParams(
            type = RecordQuickActionsParams.Type.RecordRunning(
                id = item.id,
            ),
            preview = RecordQuickActionsParams.Preview(
                name = item.name,
                iconId = item.iconId.toParams(),
                color = item.color,
            ),
        ).let(router::navigate)
    }

    @Suppress("UNUSED_PARAMETER")
    fun onRecordLongClick(
        item: RecordViewData,
        sharedElements: Pair<Any, String>? = null,
    ) {
        val type = when (item) {
            is RecordViewData.Tracked -> RecordQuickActionsParams.Type.RecordTracked(
                id = item.id,
            )
            is RecordViewData.Untracked -> RecordQuickActionsParams.Type.RecordUntracked(
                timeStarted = item.timeStartedTimestamp,
                timeEnded = item.timeEndedTimestamp,
            )
        }
        RecordQuickActionsParams(
            type = type,
            preview = RecordQuickActionsParams.Preview(
                name = item.name,
                iconId = item.iconId.toParams(),
                color = item.color,
            ),
        ).let(router::navigate)
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

    private fun subscribeToUpdates() = viewModelScope.launch {
        recordsUpdateInteractor.dataUpdated.collect {
            if (isVisible) updateRecords()
        }
    }

    private fun updateRecords() = viewModelScope.launch {
        isCalendarView.set(prefsInteractor.getShowRecordsCalendar())

        when (val state = loadRecordsViewData()) {
            is RecordsState.RecordsData -> records.set(state.data)
            is RecordsState.CalendarData -> calendarData.set(state)
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
