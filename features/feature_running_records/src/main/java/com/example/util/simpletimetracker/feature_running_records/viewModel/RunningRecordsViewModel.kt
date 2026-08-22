package com.example.util.simpletimetracker.feature_running_records.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.util.simpletimetracker.core.base.BaseViewModel
import com.example.util.simpletimetracker.core.base.SingleLiveEvent
import com.example.util.simpletimetracker.core.extension.set
import com.example.util.simpletimetracker.core.extension.toPreview
import com.example.util.simpletimetracker.core.extension.toParams
import com.example.util.simpletimetracker.core.interactor.GetChangeRecordNavigationParamsInteractor
import com.example.util.simpletimetracker.core.interactor.RecordRepeatInteractor
import com.example.util.simpletimetracker.core.model.NavigationTab
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.activityFilter.interactor.ChangeSelectedActivityFilterMediator
import com.example.util.simpletimetracker.domain.activityFilter.model.ActivityFilterType
import com.example.util.simpletimetracker.domain.base.UNTRACKED_ITEM_ID
import com.example.util.simpletimetracker.domain.darkMode.interactor.ThemeChangedInteractor
import com.example.util.simpletimetracker.domain.extension.orZero
import com.example.util.simpletimetracker.domain.prefs.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.record.interactor.AddRunningRecordMediator
import com.example.util.simpletimetracker.domain.record.interactor.RecordInteractor
import com.example.util.simpletimetracker.domain.record.interactor.RemoveRunningRecordMediator
import com.example.util.simpletimetracker.domain.record.interactor.RunningRecordInteractor
import com.example.util.simpletimetracker.domain.record.interactor.UpdateRunningRecordsInteractor
import com.example.util.simpletimetracker.domain.record.model.RecordBase
import com.example.util.simpletimetracker.domain.record.model.RecordDataSelectionDialogResult
import com.example.util.simpletimetracker.domain.recordType.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.activityFilter.ActivityFilterAddViewData
import com.example.util.simpletimetracker.feature_base_adapter.activityFilter.ActivityFilterViewData
import com.example.util.simpletimetracker.feature_base_adapter.loader.LoaderViewData
import com.example.util.simpletimetracker.feature_base_adapter.record.RecordViewData
import com.example.util.simpletimetracker.feature_base_adapter.recordShortcut.RecordShortcutViewData
import com.example.util.simpletimetracker.feature_base_adapter.recordType.RecordTypeViewData
import com.example.util.simpletimetracker.feature_base_adapter.recordTypeSpecial.RunningRecordTypeSpecialViewData
import com.example.util.simpletimetracker.feature_base_adapter.recordWithHint.RecordWithHintViewData
import com.example.util.simpletimetracker.feature_base_adapter.runningRecord.RunningRecordViewData
import com.example.util.simpletimetracker.feature_dialogs.api.interactor.CardOrderChangedInteractor
import com.example.util.simpletimetracker.feature_running_records.R
import com.example.util.simpletimetracker.feature_running_records.api.OnShortcutClickInteractor
import com.example.util.simpletimetracker.feature_running_records.interactor.RunningRecordsViewDataInteractor
import com.example.util.simpletimetracker.navigation.Router
import com.example.util.simpletimetracker.navigation.params.screen.ChangeActivityFilterParams
import com.example.util.simpletimetracker.navigation.params.screen.ChangeRecordFromMainParams
import com.example.util.simpletimetracker.navigation.params.screen.ChangeRecordParams
import com.example.util.simpletimetracker.navigation.params.screen.ChangeRecordTypeParams
import com.example.util.simpletimetracker.navigation.params.screen.ChangeRunningRecordFromMainParams
import com.example.util.simpletimetracker.navigation.params.screen.ChangeRunningRecordParams
import com.example.util.simpletimetracker.navigation.params.screen.ChangeShortcutParams
import com.example.util.simpletimetracker.navigation.params.screen.DefaultTypesSelectionDialogParams
import com.example.util.simpletimetracker.navigation.params.screen.PomodoroParams
import com.example.util.simpletimetracker.navigation.params.screen.RecordTagSelectionParams
import com.example.util.simpletimetracker.navigation.params.screen.StandardDialogParams
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RunningRecordsViewModel @Inject constructor(
    private val router: Router,
    private val resourceRepo: ResourceRepo,
    private val addRunningRecordMediator: AddRunningRecordMediator,
    private val removeRunningRecordMediator: RemoveRunningRecordMediator,
    private val runningRecordInteractor: RunningRecordInteractor,
    private val recordInteractor: RecordInteractor,
    private val recordRepeatInteractor: RecordRepeatInteractor,
    private val runningRecordsViewDataInteractor: RunningRecordsViewDataInteractor,
    private val changeSelectedActivityFilterMediator: ChangeSelectedActivityFilterMediator,
    private val prefsInteractor: PrefsInteractor,
    private val updateRunningRecordsInteractor: UpdateRunningRecordsInteractor,
    private val recordTypeInteractor: RecordTypeInteractor,
    private val getChangeRecordNavigationParamsInteractor: GetChangeRecordNavigationParamsInteractor,
    private val themeChangedInteractor: ThemeChangedInteractor,
    private val onShortcutClickInteractor: OnShortcutClickInteractor,
    private val cardOrderChangedInteractor: CardOrderChangedInteractor,
) : BaseViewModel() {

    override var delayDataLoad: Boolean = false

    val runningRecords: LiveData<List<ViewHolderType>> by lazy {
        MutableLiveData(listOf(LoaderViewData()))
    }
    val resetScreen: SingleLiveEvent<Unit> =
        SingleLiveEvent()
    val previewUpdate: SingleLiveEvent<UpdateRunningRecordsInteractor.Update> =
        SingleLiveEvent()

    private var timerJob: Job? = null
    private var updateJob: Job? = null
    private var searchJob: Job? = null
    private var completeTypeJob: Job? = null
    private var completeTypeIds: Set<Long> = emptySet()
    private var navBarHeightDp: Int = 0
    private var searchText: String = ""

    init {
        subscribeToUpdates()
    }

    fun onRecordTypeClick(
        item: RecordTypeViewData,
        sharedElements: Pair<Any, String>,
    ) = viewModelScope.launch {
        val startByLongClick = prefsInteractor.getStartTimerByLongClick()
        if (!startByLongClick) {
            onRecordTypeStart(item)
        } else {
            onRecordTypeEdit(item, sharedElements)
        }
    }

    private fun onRecordTypeStart(item: RecordTypeViewData) {
        viewModelScope.launch {
            val runningRecord = runningRecordInteractor.get(item.id)

            if (runningRecord != null) {
                // Stop running record, add new record
                removeRunningRecordMediator.removeWithRecordAdd(runningRecord)
            } else {
                // Start running record
                val wasStarted = addRunningRecordMediator.tryStartTimer(
                    typeId = item.id,
                    onNeedToShowTagSelection = { showTagSelection(item.id, it) },
                )
                if (wasStarted) {
                    onRecordTypeWithDefaultDurationClick(item.id)
                }
            }
            updateRunningRecords()
        }
    }

    fun onRecordTypeLongClick(
        item: RecordTypeViewData,
        sharedElements: Pair<Any, String>,
    ) = viewModelScope.launch {
        val startByLongClick = prefsInteractor.getStartTimerByLongClick()
        if (!startByLongClick) {
            onRecordTypeEdit(item, sharedElements)
        } else {
            onRecordTypeStart(item)
        }
    }

    fun onRecordTypeEdit(item: RecordTypeViewData, sharedElements: Pair<Any, String>) {
        router.navigate(
            data = ChangeRecordTypeParams.Change(
                transitionName = sharedElements.second,
                id = item.id,
                sizePreview = ChangeRecordTypeParams.SizePreview(
                    width = item.width,
                    height = item.height,
                    asRow = item.asRow,
                ),
                preview = ChangeRecordTypeParams.Change.Preview(
                    name = item.name,
                    iconId = item.iconId.toParams(),
                    color = item.color,
                ),
            ),
            sharedElements = mapOf(sharedElements),
        )
    }

    fun onSpecialRecordTypeClick(item: RunningRecordTypeSpecialViewData) {
        when (item.type) {
            is RunningRecordTypeSpecialViewData.Type.Add -> {
                delayDataLoad = true
                router.navigate(
                    data = ChangeRecordTypeParams.New(
                        sizePreview = ChangeRecordTypeParams.SizePreview(
                            width = item.width,
                            height = item.height,
                            asRow = item.asRow,
                        ),
                    ),
                )
            }
            is RunningRecordTypeSpecialViewData.Type.Default -> {
                router.navigate(
                    data = DefaultTypesSelectionDialogParams,
                )
            }
            is RunningRecordTypeSpecialViewData.Type.Repeat -> viewModelScope.launch {
                recordRepeatInteractor.repeat()
            }
            is RunningRecordTypeSpecialViewData.Type.Pomodoro -> {
                delayDataLoad = true
                router.navigate(PomodoroParams)
            }
        }
    }

    @Suppress("UNUSED_PARAMETER")
    fun onRunningRecordClick(
        item: RunningRecordViewData,
        sharedElements: Pair<Any, String>,
    ) = viewModelScope.launch {
        val startByLongClick = prefsInteractor.getStartTimerByLongClick()
        if (!startByLongClick) {
            onRunningRecordStop(item)
        } else {
            onRunningRecordEdit(item, sharedElements)
        }
    }

    private suspend fun onRunningRecordStop(
        item: RunningRecordViewData,
    ) {
        runningRecordInteractor.get(item.id)
            ?.let { removeRunningRecordMediator.removeWithRecordAdd(it) }
        updateRunningRecords()
    }

    fun onRecordClick(
        item: RecordWithHintViewData,
    ) = viewModelScope.launch {
        val startByLongClick = prefsInteractor.getStartTimerByLongClick()
        if (!startByLongClick) {
            onRecordStart(item)
        } else {
            onRecordEdit(item)
        }
    }

    fun onRecordLongClick(
        item: RecordWithHintViewData,
    ) = viewModelScope.launch {
        val startByLongClick = prefsInteractor.getStartTimerByLongClick()
        if (!startByLongClick) {
            onRecordEdit(item)
        } else {
            onRecordStart(item)
        }
    }

    private suspend fun onRecordStart(
        item: RecordWithHintViewData,
    ) {
        val prevRecord = recordInteractor.get(item.record.id) ?: return
        addRunningRecordMediator.startTimer(
            typeId = prevRecord.typeId,
            tags = prevRecord.tags,
            comment = prevRecord.comment,
        )
        updateRunningRecords()
    }

    private suspend fun onRecordEdit(
        item: RecordWithHintViewData,
    ) {
        val useMilitaryTimeFormat = prefsInteractor.getUseMilitaryTimeFormat()
        val showSeconds = prefsInteractor.getShowSeconds()
        val durationFormat = prefsInteractor.getDurationFormat()

        val params = getChangeRecordNavigationParamsInteractor.execute(
            item = item.record,
            from = ChangeRecordParams.From.Records,
            shift = 0,
            useMilitaryTimeFormat = useMilitaryTimeFormat,
            showSeconds = showSeconds,
            durationFormat = durationFormat,
            // Doesn't have transitions because untracked edit also doesn't have them.
            sharedElements = null,
        )

        router.navigate(
            data = ChangeRecordFromMainParams(params = params),
        )
    }

    fun onRunningRecordLongClick(
        item: RunningRecordViewData,
        sharedElements: Pair<Any, String>,
    ) {
        viewModelScope.launch {
            val startByLongClick = prefsInteractor.getStartTimerByLongClick()
            if (!startByLongClick) {
                onRunningRecordEdit(item, sharedElements)
            } else {
                onRunningRecordStop(item)
            }
        }
    }

    private suspend fun onRunningRecordEdit(
        item: RunningRecordViewData,
        sharedElements: Pair<Any, String>,
    ) {
        val useMilitaryTimeFormat = prefsInteractor.getUseMilitaryTimeFormat()
        val showSeconds = prefsInteractor.getShowSeconds()
        val durationFormat = prefsInteractor.getDurationFormat()

        if (item.id == UNTRACKED_ITEM_ID) {
            // Currently possible in retroactive mode.
            // Open running record as an untracked record.
            val timeEndedTimestamp = System.currentTimeMillis()
            val recordItem = RecordViewData.Untracked(
                timeStartedTimestamp = item.timeStartedTimestamp,
                timeEndedTimestamp = timeEndedTimestamp,
                name = item.name,
                timeStarted = item.timeStarted,
                timeFinished = "",
                duration = item.timer,
                durationTotal = "",
                iconId = item.iconId,
                color = item.color,
            )
            val params = getChangeRecordNavigationParamsInteractor.execute(
                item = recordItem,
                from = ChangeRecordParams.From.Records,
                shift = 0,
                useMilitaryTimeFormat = useMilitaryTimeFormat,
                showSeconds = showSeconds,
                durationFormat = durationFormat,
                sharedElements = null,
            )
            router.navigate(
                data = ChangeRecordFromMainParams(params = params),
            )
        } else {
            val params = getChangeRecordNavigationParamsInteractor.execute(
                item = item,
                from = ChangeRunningRecordParams.From.RunningRecords,
                useMilitaryTimeFormat = useMilitaryTimeFormat,
                showSeconds = showSeconds,
                durationFormat = durationFormat,
                sharedElements = sharedElements,
            )
            router.navigate(
                data = ChangeRunningRecordFromMainParams(params = params),
                sharedElements = sharedElements.let(::mapOf),
            )
        }
    }

    fun onActivityFilterClick(item: ActivityFilterViewData) {
        viewModelScope.launch {
            changeSelectedActivityFilterMediator.onFilterClicked(
                id = item.id,
                type = item.type,
                selected = item.selected,
            )
            updateRunningRecords()
        }
    }

    fun onActivityFilterLongClick(item: ActivityFilterViewData, sharedElements: Pair<Any, String>) {
        if (item.type !is ActivityFilterType.Default) return
        router.navigate(
            data = ChangeActivityFilterParams.Change(
                transitionName = sharedElements.second,
                id = item.id,
                preview = ChangeActivityFilterParams.Change.Preview(
                    name = item.name,
                    color = item.color,
                ),
            ),
            sharedElements = sharedElements.let(::mapOf),
        )
    }

    fun onActivityFilterSpecialClick(item: ActivityFilterAddViewData) = viewModelScope.launch {
        when (item.type) {
            ActivityFilterAddViewData.Type.ADD -> {
                delayDataLoad = true
                router.navigate(
                    data = ChangeActivityFilterParams.New,
                )
            }
            ActivityFilterAddViewData.Type.TOGGLE_VISIBILITY -> {
                val newState = !prefsInteractor.getIsActivityFiltersCollapsed()
                prefsInteractor.setIsActivityFiltersCollapsed(newState)
                updateRunningRecords()
            }
        }
    }

    fun onShortcutClick(
        item: RecordShortcutViewData,
        sharedElements: Pair<Any, String>?,
    ) = viewModelScope.launch {
        val startByLongClick = prefsInteractor.getStartTimerByLongClick()
        if (!startByLongClick) {
            onShortcutStart(item)
        } else {
            onShortcutEdit(item, sharedElements)
        }
    }

    fun onShortcutLongClick(
        item: RecordShortcutViewData,
        sharedElements: Pair<Any, String>?,
    ) = viewModelScope.launch {
        val startByLongClick = prefsInteractor.getStartTimerByLongClick()
        if (!startByLongClick) {
            onShortcutEdit(item, sharedElements)
        } else {
            onShortcutStart(item)
        }
    }

    fun onShortcutSpinnerPositionSelected(block: RecordShortcutViewData, position: Int) = viewModelScope.launch {
        onShortcutClickInteractor.onSpinnerPositionSelected(block, position)
        updateRunningRecords()
    }

    fun onShortcutButtonClick(
        item: RecordShortcutViewData,
    ) {
        onShortcutClickInteractor.onButtonClick(item)
    }

    private suspend fun onShortcutStart(item: RecordShortcutViewData) {
        onShortcutClickInteractor.execute(item)
        updateRunningRecords()
    }

    private fun onShortcutEdit(
        item: RecordShortcutViewData,
        sharedElements: Pair<Any, String>?,
    ) {
        router.navigate(
            data = ChangeShortcutParams.Change(
                id = item.id,
                transitionName = sharedElements?.second.orEmpty(),
                preview = item.toPreview(),
            ),
            sharedElements = sharedElements?.let { mapOf(it) },
        )
    }

    fun onSearchTextChange(text: String) {
        if (text != searchText) {
            searchJob?.cancel()
            searchJob = viewModelScope.launch {
                searchText = text
                // Do not delay on clear.
                if (text.isNotEmpty()) delay(500)
                updateRunningRecords(fromValueChange = true)
            }
        }
    }

    fun onVisible() {
        startUpdate()
        checkForRetroActiveMultitaskHint()
    }

    fun onHidden() {
        stopUpdate()
    }

    fun onTagSelected() {
        updateRunningRecords()
    }

    fun onTabReselected(tab: NavigationTab?) {
        if (tab is NavigationTab.RunningRecords) {
            resetScreen.set(Unit)
        }
    }

    fun onChangeInsets(navBarHeight: Int) {
        if (navBarHeightDp != navBarHeight) {
            navBarHeightDp = navBarHeight
            updateRunningRecords()
        }
    }

    fun onPositiveClick(tag: String?) = viewModelScope.launch {
        when (tag) {
            RETRO_MULTITASKING_HINT_TAG -> {
                prefsInteractor.setRetroactiveMultitaskingHintWasHidden(true)
            }
        }
    }

    private suspend fun onRecordTypeWithDefaultDurationClick(typeId: Long) {
        val defaultDuration = recordTypeInteractor.get(typeId)?.defaultDuration
        if (defaultDuration.orZero() <= 0L) return

        completeTypeIds = completeTypeIds + typeId
        completeTypeJob?.cancel()
        completeTypeJob = viewModelScope.launch {
            delay(COMPLETE_TYPE_ANIMATION_MS)
            completeTypeIds = completeTypeIds - typeId
            updateRunningRecords()
        }
    }

    private fun showTagSelection(
        typeId: Long,
        result: RecordDataSelectionDialogResult,
    ) {
        router.navigate(
            RecordTagSelectionParams(
                typeId = typeId,
                fields = result.fields.toParams(),
                preselectedTags = result.preselectedTags.map(RecordBase.Tag::toParams),
                requiredValueSelectionTagIds = result.requiredValueSelectionTagIds,
            ),
        )
    }

    private fun checkForRetroActiveMultitaskHint() = viewModelScope.launch {
        val needToShow = !prefsInteractor.getRetroactiveMultitaskingHintWasHidden() &&
            prefsInteractor.getRetroactiveTrackingMode() &&
            prefsInteractor.getAllowMultitasking()

        if (needToShow) {
            router.navigate(
                StandardDialogParams(
                    tag = RETRO_MULTITASKING_HINT_TAG,
                    message = resourceRepo.getString(R.string.settings_retroactive_multitasking_hint),
                    btnPositive = resourceRepo.getString(R.string.ok),
                ),
            )
        }
    }

    private fun subscribeToUpdates() {
        viewModelScope.launch {
            updateRunningRecordsInteractor.dataUpdated.collect { onUpdateReceived(it) }
        }
        viewModelScope.launch {
            updateRunningRecordsInteractor.fullUpdate.collect { updateRunningRecords() }
        }
        viewModelScope.launch {
            themeChangedInteractor.themeChanged.collect { updateRunningRecords() }
        }
        viewModelScope.launch {
            cardOrderChangedInteractor.update.collect { updateRunningRecords() }
        }
    }

    private fun onUpdateReceived(
        update: UpdateRunningRecordsInteractor.Update,
    ) {
        previewUpdate.set(update)
    }

    private fun updateRunningRecords(
        fromValueChange: Boolean = false,
    ) {
        updateJob?.cancel()
        updateJob = viewModelScope.launch {
            val data = loadRunningRecordsViewData(fromValueChange)
            runningRecords.set(data)
        }
    }

    private suspend fun loadRunningRecordsViewData(
        fromSearchChange: Boolean,
    ): List<ViewHolderType> {
        return runningRecordsViewDataInteractor.getViewData(
            completeTypeIds = completeTypeIds,
            navBarHeightDp = navBarHeightDp,
            searchText = searchText,
            fromSearchChange = fromSearchChange,
        )
    }

    private fun startUpdate() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            delayLoad()
            while (isActive) {
                updateRunningRecords()
                delay(TIMER_UPDATE_MS)
            }
        }
    }

    private fun stopUpdate() {
        timerJob?.cancel()
    }

    companion object {
        private const val TIMER_UPDATE_MS = 1000L
        private const val COMPLETE_TYPE_ANIMATION_MS = 1000L
        private const val RETRO_MULTITASKING_HINT_TAG = "RETRO_MULTITASKING_HINT_TAG"
    }
}
