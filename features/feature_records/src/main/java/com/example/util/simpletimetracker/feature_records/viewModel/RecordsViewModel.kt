package com.example.util.simpletimetracker.feature_records.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.util.simpletimetracker.core.base.SingleLiveEvent
import com.example.util.simpletimetracker.core.extension.set
import com.example.util.simpletimetracker.core.extension.toParams
import com.example.util.simpletimetracker.core.extension.toRecordParams
import com.example.util.simpletimetracker.core.interactor.SharingInteractor
import com.example.util.simpletimetracker.core.mapper.ChangeRecordDateTimeMapper
import com.example.util.simpletimetracker.core.mapper.RangeViewDataMapper
import com.example.util.simpletimetracker.core.model.NavigationTab
import com.example.util.simpletimetracker.domain.extension.orZero
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordsShareUpdateInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordsUpdateInteractor
import com.example.util.simpletimetracker.domain.interactor.UpdateRunningRecordFromChangeScreenInteractor
import com.example.util.simpletimetracker.domain.model.RangeLength
import com.example.util.simpletimetracker.domain.model.count
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.loader.LoaderViewData
import com.example.util.simpletimetracker.feature_base_adapter.record.RecordViewData
import com.example.util.simpletimetracker.feature_base_adapter.runningRecord.RunningRecordViewData
import com.example.util.simpletimetracker.feature_records.extra.RecordsExtra
import com.example.util.simpletimetracker.feature_records.interactor.RecordsViewDataInteractor
import com.example.util.simpletimetracker.feature_records.mapper.RecordsViewDataMapper
import com.example.util.simpletimetracker.feature_records.model.RecordsShareState
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
    private val recordsUpdateInteractor: RecordsUpdateInteractor,
    private val recordsShareUpdateInteractor: RecordsShareUpdateInteractor,
    private val sharingInteractor: SharingInteractor,
    private val rangeViewDataMapper: RangeViewDataMapper,
    private val recordsViewDataMapper: RecordsViewDataMapper,
    private val updateRunningRecordFromChangeScreenInteractor: UpdateRunningRecordFromChangeScreenInteractor,
    private val changeRecordDateTimeMapper: ChangeRecordDateTimeMapper,
) : ViewModel() {

    var extra: RecordsExtra? = null

    val isCalendarView: LiveData<Boolean> = MutableLiveData()
    val records: LiveData<List<ViewHolderType>> by lazy {
        MutableLiveData(listOf(LoaderViewData() as ViewHolderType))
    }
    val calendarData: LiveData<RecordsState.CalendarData> by lazy {
        MutableLiveData(RecordsState.CalendarData.Loading)
    }
    val sharingData: SingleLiveEvent<RecordsShareState> = SingleLiveEvent()
    val resetScreen: SingleLiveEvent<Unit> = SingleLiveEvent()
    val previewUpdate: SingleLiveEvent<UpdateRunningRecordFromChangeScreenInteractor.Update> = SingleLiveEvent()

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
                timeStartedDateTime = changeRecordDateTimeMapper.map(
                    param = ChangeRecordDateTimeMapper.Param.DateTime(item.timeStartedTimestamp),
                    field = ChangeRecordDateTimeMapper.Field.Start,
                    useMilitaryTimeFormat = useMilitaryTimeFormat,
                    showSeconds = showSeconds,
                ).toRecordParams(),
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
            timeStartedDateTime = changeRecordDateTimeMapper.map(
                param = ChangeRecordDateTimeMapper.Param.DateTime(item.timeStartedTimestamp),
                field = ChangeRecordDateTimeMapper.Field.Start,
                useMilitaryTimeFormat = useMilitaryTimeFormat,
                showSeconds = showSeconds,
            ).toRecordParams(),
            timeEndedDateTime = changeRecordDateTimeMapper.map(
                param = ChangeRecordDateTimeMapper.Param.DateTime(item.timeEndedTimestamp),
                field = ChangeRecordDateTimeMapper.Field.End,
                useMilitaryTimeFormat = useMilitaryTimeFormat,
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
                daysFromToday = shift,
                preview = preview,
            )
            is RecordViewData.Untracked -> ChangeRecordParams.Untracked(
                transitionName = sharedElements?.second.orEmpty(),
                timeStarted = item.timeStartedTimestamp,
                timeEnded = item.timeEndedTimestamp,
                daysFromToday = shift,
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

    fun onShareView(view: Any) = viewModelScope.launch {
        sharingInteractor.execute(view = view, filename = SHARING_NAME)
    }

    private fun subscribeToUpdates() {
        viewModelScope.launch {
            recordsUpdateInteractor.dataUpdated.collect {
                if (isVisible) updateRecords()
            }
        }
        viewModelScope.launch {
            recordsShareUpdateInteractor.shareClicked.collect {
                if (isVisible) onShareClicked()
            }
        }
        viewModelScope.launch {
            updateRunningRecordFromChangeScreenInteractor.dataUpdated.collect {
                onUpdateReceived(it)
            }
        }
    }

    private fun onUpdateReceived(
        update: UpdateRunningRecordFromChangeScreenInteractor.Update,
    ) {
        // No need to update.
        if (shift != 0) return

        previewUpdate.set(update)
    }

    @Suppress("MoveVariableDeclarationIntoWhen")
    private suspend fun onShareClicked() {
        val state = loadRecordsViewData(true)
        val data = when (state) {
            is RecordsState.RecordsData -> {
                RecordsShareState(
                    rangeViewDataMapper.mapToShareTitle(
                        rangeLength = RangeLength.Day,
                        position = shift,
                        startOfDayShift = prefsInteractor.getStartOfDayShift(),
                        firstDayOfWeek = prefsInteractor.getFirstDayOfWeek(),
                    ),
                    RecordsShareState.State.Records(state.data),
                )
            }
            is RecordsState.CalendarData.Data -> {
                RecordsShareState(
                    recordsViewDataMapper.mapTitle(
                        shift = shift,
                        startOfDayShift = prefsInteractor.getStartOfDayShift(),
                        isCalendarView = prefsInteractor.getShowRecordsCalendar(),
                        calendarDayCount = prefsInteractor.getDaysInCalendar().count,
                    ),
                    RecordsShareState.State.Calendar(state.data),
                )
            }
            else -> return
        }
        sharingData.set(data)
    }

    private fun updateRecords() = viewModelScope.launch {
        isCalendarView.set(prefsInteractor.getShowRecordsCalendar())

        when (val state = loadRecordsViewData()) {
            is RecordsState.RecordsData -> records.set(state.data)
            is RecordsState.CalendarData -> calendarData.set(state)
        }
    }

    private suspend fun loadRecordsViewData(forSharing: Boolean = false): RecordsState {
        return recordsViewDataInteractor.getViewData(
            shift = shift,
            forSharing = forSharing,
        )
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
        private const val SHARING_NAME = "stt_records"
    }
}
