package com.example.util.simpletimetracker.feature_records.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.util.simpletimetracker.core.base.BaseViewModel
import com.example.util.simpletimetracker.core.base.SingleLiveEvent
import com.example.util.simpletimetracker.core.extension.set
import com.example.util.simpletimetracker.core.extension.toParams
import com.example.util.simpletimetracker.core.interactor.GetChangeRecordNavigationParamsInteractor
import com.example.util.simpletimetracker.core.interactor.SharingInteractor
import com.example.util.simpletimetracker.core.mapper.RangeViewDataMapper
import com.example.util.simpletimetracker.core.model.NavigationTab
import com.example.util.simpletimetracker.domain.darkMode.interactor.ThemeChangedInteractor
import com.example.util.simpletimetracker.domain.extension.orZero
import com.example.util.simpletimetracker.domain.prefs.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.record.interactor.RecordsContainerMultiselectInteractor
import com.example.util.simpletimetracker.domain.record.interactor.RecordsShareUpdateInteractor
import com.example.util.simpletimetracker.domain.record.interactor.RecordsUpdateInteractor
import com.example.util.simpletimetracker.domain.record.interactor.RunningRecordInteractor
import com.example.util.simpletimetracker.domain.record.interactor.UpdateRunningRecordsInteractor
import com.example.util.simpletimetracker.domain.record.model.MultiSelectedRecordId
import com.example.util.simpletimetracker.domain.statistics.model.ChartFilterType
import com.example.util.simpletimetracker.domain.statistics.model.RangeLength
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RecordsViewModel @Inject constructor(
    private val router: Router,
    private val recordsViewDataInteractor: RecordsViewDataInteractor,
    private val prefsInteractor: PrefsInteractor,
    private val runningRecordInteractor: RunningRecordInteractor,
    private val recordsUpdateInteractor: RecordsUpdateInteractor,
    private val recordsShareUpdateInteractor: RecordsShareUpdateInteractor,
    private val sharingInteractor: SharingInteractor,
    private val rangeViewDataMapper: RangeViewDataMapper,
    private val recordsViewDataMapper: RecordsViewDataMapper,
    private val updateRunningRecordsInteractor: UpdateRunningRecordsInteractor,
    private val getChangeRecordNavigationParamsInteractor: GetChangeRecordNavigationParamsInteractor,
    private val recordsContainerMultiselectInteractor: RecordsContainerMultiselectInteractor,
    private val themeChangedInteractor: ThemeChangedInteractor,
) : BaseViewModel() {

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
    val previewUpdate: SingleLiveEvent<UpdateRunningRecordsInteractor.Update> = SingleLiveEvent()

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
        if (runningRecordInteractor.get(item.id) == null) return@launch
        if (recordsContainerMultiselectInteractor.isEnabled) {
            onMultiselectRunningRecordClick(item)
            return@launch
        }
        val useMilitaryTimeFormat = prefsInteractor.getUseMilitaryTimeFormat()
        val showSeconds = prefsInteractor.getShowSeconds()
        val durationFormat = prefsInteractor.getDurationFormat()
        val params = getChangeRecordNavigationParamsInteractor.execute(
            item = item,
            from = ChangeRunningRecordParams.From.Records,
            useMilitaryTimeFormat = useMilitaryTimeFormat,
            showSeconds = showSeconds,
            durationFormat = durationFormat,
            sharedElements = sharedElements,
        )
        throttle {
            router.navigate(
                data = ChangeRunningRecordFromMainParams(params),
                sharedElements = sharedElements?.let(::mapOf).orEmpty(),
            )
        }.invoke()
    }

    fun onRecordClick(
        item: RecordViewData,
        sharedElements: Pair<Any, String>? = null,
    ) = viewModelScope.launch {
        if (recordsContainerMultiselectInteractor.isEnabled) {
            onMultiselectRecordClick(item)
            return@launch
        }
        val useMilitaryTimeFormat = prefsInteractor.getUseMilitaryTimeFormat()
        val showSeconds = prefsInteractor.getShowSeconds()
        val durationFormat = prefsInteractor.getDurationFormat()
        val params = getChangeRecordNavigationParamsInteractor.execute(
            item = item,
            from = ChangeRecordParams.From.Records,
            shift = shift,
            useMilitaryTimeFormat = useMilitaryTimeFormat,
            showSeconds = showSeconds,
            durationFormat = durationFormat,
            sharedElements = sharedElements,
        )
        throttle {
            router.navigate(
                data = ChangeRecordFromMainParams(params),
                sharedElements = sharedElements?.let(::mapOf).orEmpty(),
            )
        }.invoke()
    }

    @Suppress("UNUSED_PARAMETER")
    fun onRunningRecordLongClick(
        item: RunningRecordViewData,
        sharedElements: Pair<Any, String>? = null,
    ) {
        if (recordsContainerMultiselectInteractor.isEnabled) {
            val id = MultiSelectedRecordId.Running(item.id)
            if (id !in recordsContainerMultiselectInteractor.selectedRecordIds) {
                recordsContainerMultiselectInteractor.onRecordClick(id)
                updateRecords()
                return
            }
        }
        val navParams = RecordQuickActionsParams(
            type = RecordQuickActionsParams.Type.RecordRunning(
                id = item.id,
            ),
            preview = RecordQuickActionsParams.Preview(
                name = item.name,
                iconId = item.iconId.toParams(),
                color = item.color,
            ),
        )
        throttle {
            router.navigate(navParams)
        }.invoke()
    }

    @Suppress("UNUSED_PARAMETER")
    fun onRecordLongClick(
        item: RecordViewData,
        sharedElements: Pair<Any, String>? = null,
    ) {
        if (recordsContainerMultiselectInteractor.isEnabled) {
            val id = when (item) {
                is RecordViewData.Tracked -> MultiSelectedRecordId.Tracked(item.id)
                is RecordViewData.Untracked -> MultiSelectedRecordId.Untracked(
                    timeStartedTimestamp = item.timeStartedTimestamp,
                    timeEndedTimestamp = item.timeEndedTimestamp,
                )
            }
            if (id !in recordsContainerMultiselectInteractor.selectedRecordIds) {
                recordsContainerMultiselectInteractor.onRecordClick(id)
                updateRecords()
                return
            }
        }
        val type = when (item) {
            is RecordViewData.Tracked -> RecordQuickActionsParams.Type.RecordTracked(
                id = item.id,
            )
            is RecordViewData.Untracked -> RecordQuickActionsParams.Type.RecordUntracked(
                timeStarted = item.timeStartedTimestamp,
                timeEnded = item.timeEndedTimestamp,
            )
        }
        val navParams = RecordQuickActionsParams(
            type = type,
            preview = RecordQuickActionsParams.Preview(
                name = item.name,
                iconId = item.iconId.toParams(),
                color = item.color,
            ),
        )
        throttle {
            router.navigate(navParams)
        }.invoke()
    }

    fun onVisible() {
        isVisible = true
        startUpdate()
    }

    fun onHidden() {
        isVisible = false
        stopUpdate()
    }

    fun onNeedUpdate() {
        if (isVisible) updateRecords()
    }

    fun onTabReselected(tab: NavigationTab?) {
        if (isVisible && tab is NavigationTab.Records) {
            resetScreen.set(Unit)
        }
    }

    fun onShareView(view: Any) = viewModelScope.launch {
        sharingInteractor.execute(view = view, filename = SHARING_NAME)
    }

    fun onFilterApplied(
        chartFilterType: ChartFilterType,
        dataIds: List<Long>,
    ) = viewModelScope.launch {
        if (!isVisible) return@launch
        prefsInteractor.setListFilterType(chartFilterType)
        when (chartFilterType) {
            ChartFilterType.ACTIVITY -> prefsInteractor.setFilteredTypesOnList(dataIds)
            ChartFilterType.CATEGORY -> prefsInteractor.setFilteredCategoriesOnList(dataIds)
            ChartFilterType.RECORD_TAG -> prefsInteractor.setFilteredTagsOnList(dataIds)
        }
    }

    private fun subscribeToUpdates() {
        viewModelScope.launch {
            recordsUpdateInteractor.dataUpdated.collect { if (isVisible) updateRecords() }
        }
        viewModelScope.launch {
            recordsShareUpdateInteractor.shareClicked.collect { if (isVisible) onShareClicked() }
        }
        viewModelScope.launch {
            updateRunningRecordsInteractor.dataUpdated.collect { onUpdateReceived(it) }
        }
        viewModelScope.launch {
            themeChangedInteractor.themeChanged.collect { updateRecords() }
        }
    }

    private fun onUpdateReceived(
        update: UpdateRunningRecordsInteractor.Update,
    ) {
        // No need to update.
        if (shift != 0) return

        previewUpdate.set(update)
    }

    private fun onMultiselectRunningRecordClick(item: RunningRecordViewData) {
        val id = MultiSelectedRecordId.Running(item.id)
        recordsContainerMultiselectInteractor.onRecordClick(id)
        updateRecords()
    }

    private fun onMultiselectRecordClick(item: RecordViewData) {
        val id = when (item) {
            is RecordViewData.Tracked -> MultiSelectedRecordId.Tracked(item.id)
            is RecordViewData.Untracked -> MultiSelectedRecordId.Untracked(
                timeStartedTimestamp = item.timeStartedTimestamp,
                timeEndedTimestamp = item.timeEndedTimestamp,
            )
        }
        recordsContainerMultiselectInteractor.onRecordClick(id)
        updateRecords()
    }

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
                    recordsViewDataMapper.mapToShareCalendarTitle(
                        shift = shift,
                        startOfDayShift = prefsInteractor.getStartOfDayShift(),
                        isCalendarView = prefsInteractor.getShowRecordsCalendar(),
                        daysInCalendar = prefsInteractor.getDaysInCalendar(),
                        firstDayOfWeek = prefsInteractor.getFirstDayOfWeek(),
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
        timerJob?.cancel()
        if (shift != 0) {
            updateRecords()
            return
        }
        timerJob = viewModelScope.launch {
            while (isActive) {
                updateRecords()
                delay(TIMER_UPDATE)
            }
        }
    }

    private fun stopUpdate() {
        timerJob?.cancel()
    }

    companion object {
        private const val TIMER_UPDATE = 1000L
        private const val SHARING_NAME = "stt_records"
    }
}
