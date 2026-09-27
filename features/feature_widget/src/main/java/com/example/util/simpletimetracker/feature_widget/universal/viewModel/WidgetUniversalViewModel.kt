package com.example.util.simpletimetracker.feature_widget.universal.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.util.simpletimetracker.core.extension.allowDiskRead
import com.example.util.simpletimetracker.core.extension.set
import com.example.util.simpletimetracker.core.extension.toParams
import com.example.util.simpletimetracker.core.interactor.ActivityFilterViewDataInteractor
import com.example.util.simpletimetracker.core.interactor.ActivitySuggestionViewDataInteractor
import com.example.util.simpletimetracker.core.interactor.FilterGoalsByDayOfWeekInteractor
import com.example.util.simpletimetracker.core.interactor.GetCurrentRecordsDurationInteractor
import com.example.util.simpletimetracker.core.interactor.RecordRepeatInteractor
import com.example.util.simpletimetracker.core.mapper.RecordTypeViewDataMapper
import com.example.util.simpletimetracker.domain.activityFilter.interactor.ChangeSelectedActivityFilterMediator
import com.example.util.simpletimetracker.domain.extension.addBetweenEach
import com.example.util.simpletimetracker.domain.extension.orZero
import com.example.util.simpletimetracker.domain.extension.plus
import com.example.util.simpletimetracker.domain.prefs.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.record.interactor.AddRunningRecordMediator
import com.example.util.simpletimetracker.domain.record.interactor.RemoveRunningRecordMediator
import com.example.util.simpletimetracker.domain.record.interactor.RunningRecordInteractor
import com.example.util.simpletimetracker.domain.record.model.RecordBase
import com.example.util.simpletimetracker.domain.record.model.RecordDataSelectionDialogResult
import com.example.util.simpletimetracker.domain.record.model.RunningRecord
import com.example.util.simpletimetracker.domain.recordTag.interactor.RecordTagInteractor
import com.example.util.simpletimetracker.domain.recordType.interactor.RecordTypeGoalInteractor
import com.example.util.simpletimetracker.domain.recordType.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.domain.recordType.model.RecordType
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.activityFilter.ActivityFilterAddViewData
import com.example.util.simpletimetracker.feature_base_adapter.activityFilter.ActivityFilterViewData
import com.example.util.simpletimetracker.feature_base_adapter.button.ButtonViewData
import com.example.util.simpletimetracker.feature_base_adapter.divider.DividerViewData
import com.example.util.simpletimetracker.feature_base_adapter.emptySpace.EmptySpaceViewData
import com.example.util.simpletimetracker.feature_base_adapter.loader.LoaderViewData
import com.example.util.simpletimetracker.feature_base_adapter.recordShortcut.RecordShortcutViewData
import com.example.util.simpletimetracker.feature_base_adapter.recordType.RecordTypeViewData
import com.example.util.simpletimetracker.feature_base_adapter.recordTypeSpecial.RunningRecordTypeSpecialViewData
import com.example.util.simpletimetracker.feature_dialogs.api.interactor.CardOrderChangedInteractor
import com.example.util.simpletimetracker.feature_running_records.api.OnShortcutClickInteractor
import com.example.util.simpletimetracker.feature_running_records.api.RecordsShortcutsViewDataInteractor
import com.example.util.simpletimetracker.feature_widget.universal.mapper.WidgetUniversalViewDataMapper
import com.example.util.simpletimetracker.feature_widget.universal.viewData.WidgetUniversalButtonViewData
import com.example.util.simpletimetracker.navigation.Router
import com.example.util.simpletimetracker.navigation.params.screen.RecordTagSelectionParams
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WidgetUniversalViewModel @Inject constructor(
    private val router: Router,
    private val addRunningRecordMediator: AddRunningRecordMediator,
    private val removeRunningRecordMediator: RemoveRunningRecordMediator,
    private val recordTypeInteractor: RecordTypeInteractor,
    private val recordTagInteractor: RecordTagInteractor,
    private val runningRecordInteractor: RunningRecordInteractor,
    private val prefsInteractor: PrefsInteractor,
    private val changeSelectedActivityFilterMediator: ChangeSelectedActivityFilterMediator,
    private val activityFilterViewDataInteractor: ActivityFilterViewDataInteractor,
    private val widgetUniversalViewDataMapper: WidgetUniversalViewDataMapper,
    private val recordTypeViewDataMapper: RecordTypeViewDataMapper,
    private val recordRepeatInteractor: RecordRepeatInteractor,
    private val recordTypeGoalInteractor: RecordTypeGoalInteractor,
    private val getCurrentRecordsDurationInteractor: GetCurrentRecordsDurationInteractor,
    private val filterGoalsByDayOfWeekInteractor: FilterGoalsByDayOfWeekInteractor,
    private val activitySuggestionViewDataInteractor: ActivitySuggestionViewDataInteractor,
    private val recordsShortcutsViewDataInteractor: RecordsShortcutsViewDataInteractor,
    private val onShortcutClickInteractor: OnShortcutClickInteractor,
    private val cardOrderChangedInteractor: CardOrderChangedInteractor,
) : ViewModel() {

    val recordTypes: LiveData<List<ViewHolderType>> by lazy {
        return@lazy MutableLiveData<List<ViewHolderType>>().let { initial ->
            allowDiskRead { viewModelScope }.launch {
                initial.value = listOf(LoaderViewData())
                initial.value = loadRecordTypesViewData()
            }
            initial
        }
    }

    val exit: LiveData<Unit> = MutableLiveData()

    private var completeTypeJob: Job? = null
    private var completeTypeIds: Set<Long> = emptySet()
    private var navBarHeightDp: Int = 0

    init {
        subscribeToUpdates()
    }

    fun onChangeInsets(navBarHeight: Int) {
        if (navBarHeightDp != navBarHeight) {
            navBarHeightDp = navBarHeight
            updateRecordTypesViewData()
        }
    }

    fun onRecordTypeClick(item: RecordTypeViewData) {
        viewModelScope.launch {
            val runningRecord = runningRecordInteractor.get(item.id)
            var wasStarted = false

            if (runningRecord != null) {
                // Stop running record, add new record
                removeRunningRecordMediator.removeWithRecordAdd(runningRecord)
            } else {
                // Start running record
                wasStarted = addRunningRecordMediator.tryStartTimer(
                    typeId = item.id,
                    onNeedToShowTagSelection = { showTagSelection(item.id, it) },
                )
                if (wasStarted) {
                    onRecordTypeWithDefaultDurationClick(item.id)
                }
            }

            updateRecordTypesViewData()
            if (wasStarted) exit.set(Unit)
        }
    }

    fun onSpecialRecordTypeClick(item: RunningRecordTypeSpecialViewData) {
        viewModelScope.launch {
            val started: Boolean

            when (item.type) {
                is RunningRecordTypeSpecialViewData.Type.Repeat -> {
                    val result = recordRepeatInteractor.repeat()
                    started = result is RecordRepeatInteractor.ActionResult.Started
                }
                else -> return@launch
            }

            updateRecordTypesViewData()
            if (started) exit.set(Unit)
        }
    }

    fun onActivityFilterClick(item: ActivityFilterViewData) {
        viewModelScope.launch {
            changeSelectedActivityFilterMediator.onFilterClicked(
                id = item.id,
                type = item.type,
                selected = item.selected,
            )
            updateRecordTypesViewData()
        }
    }

    fun onActivityFilterSpecialClick(item: ActivityFilterAddViewData) = viewModelScope.launch {
        when (item.type) {
            ActivityFilterAddViewData.Type.ADD -> {
                // Currently not supported.
            }
            ActivityFilterAddViewData.Type.TOGGLE_VISIBILITY -> {
                val newState = !prefsInteractor.getIsActivityFiltersCollapsed()
                prefsInteractor.setIsActivityFiltersCollapsed(newState)
                updateRecordTypesViewData()
            }
        }
    }

    fun onShortcutClick(item: RecordShortcutViewData) = viewModelScope.launch {
        onShortcutClickInteractor.execute(item)
        updateRecordTypesViewData()
        exit.set(Unit)
    }

    fun onShortcutSpinnerPositionSelected(block: RecordShortcutViewData, position: Int) = viewModelScope.launch {
        onShortcutClickInteractor.onSpinnerPositionSelected(block, position)
        updateRecordTypesViewData()
    }

    fun onShortcutButtonClick(
        item: RecordShortcutViewData,
    ) {
        onShortcutClickInteractor.onButtonClick(item)
    }

    fun onButtonClick(data: ButtonViewData) {
        if (data.id !is WidgetUniversalButtonViewData) return
        router.startApp()
    }

    fun onTagSelected() {
        updateRecordTypesViewData()
        exit.set(Unit)
    }

    private suspend fun onRecordTypeWithDefaultDurationClick(typeId: Long) {
        val defaultDuration = recordTypeInteractor.get(typeId)?.defaultDuration
        if (defaultDuration.orZero() <= 0L) return

        completeTypeIds = completeTypeIds + typeId
        completeTypeJob?.cancel()
        completeTypeJob = viewModelScope.launch {
            delay(COMPLETE_TYPE_ANIMATION_MS)
            completeTypeIds = completeTypeIds - typeId
            updateRecordTypesViewData()
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

    private fun subscribeToUpdates() {
        allowDiskRead { viewModelScope }.launch {
            cardOrderChangedInteractor.update.collect { updateRecordTypesViewData() }
        }
    }

    private fun updateRecordTypesViewData() = viewModelScope.launch {
        val data = loadRecordTypesViewData()
        recordTypes.set(data)
    }

    private suspend fun loadRecordTypesViewData(): List<ViewHolderType> {
        val runningRecords = runningRecordInteractor.getAll()
        val recordTypes = recordTypeInteractor.getAll()
        val recordTypesMap = recordTypes.associateBy(RecordType::id)
        val recordTags = recordTagInteractor.getAll()
        val recordTypesRunning = runningRecords.map(RunningRecord::id)
        val numberOfCards = prefsInteractor.getNumberOfCards()
        val isDarkTheme = prefsInteractor.getDarkMode()
        val retroactiveTrackingMode = prefsInteractor.getRetroactiveTrackingMode()
        val isFiltersCollapsed = prefsInteractor.getIsActivityFiltersCollapsed()
        val goals = filterGoalsByDayOfWeekInteractor
            .execute(recordTypeGoalInteractor.getAllTypeGoals())
            .groupBy { it.idData.value }
        val allDailyCurrents = if (goals.isNotEmpty()) {
            getCurrentRecordsDurationInteractor.getAllDailyCurrents(
                typeIds = recordTypes.map(RecordType::id).toSet(),
                runningRecords = runningRecords,
            )
        } else {
            // No goals - no need to calculate durations.
            emptyMap()
        }

        val filter = activityFilterViewDataInteractor.getFilter()
        val filtersViewData = activityFilterViewDataInteractor.getFilterViewData(
            filter = filter,
            searchText = "",
            isDarkTheme = isDarkTheme,
            isFiltersCollapsed = isFiltersCollapsed,
            appendAddButton = false,
        )

        val suggestionsViewData = activitySuggestionViewDataInteractor.getSuggestionsViewData(
            recordTypesMap = recordTypesMap,
            goals = goals,
            runningRecords = runningRecords,
            allDailyCurrents = allDailyCurrents,
            completeTypeIds = completeTypeIds,
            searchText = "",
            numberOfCards = numberOfCards,
            isDarkTheme = isDarkTheme,
        )

        val recordTypesViewData = recordTypes
            .filterNot { it.hidden }
            .let { list ->
                activityFilterViewDataInteractor.applyFilter(list, filter)
            }
            .map {
                recordTypeViewDataMapper.mapFiltered(
                    recordType = it,
                    isFiltered = it.id in recordTypesRunning,
                    numberOfCards = numberOfCards,
                    isDarkTheme = isDarkTheme,
                    checkStates = recordTypeViewDataMapper.mapGoalCheckmarks(
                        type = it,
                        goals = goals,
                        allDailyCurrents = allDailyCurrents,
                    ),
                    isComplete = it.id in completeTypeIds,
                )
            }.let {
                if (it.isEmpty()) {
                    recordTypeViewDataMapper.mapToEmpty()
                } else {
                    val repeatViewData = recordTypeViewDataMapper.mapToRepeatItem(
                        numberOfCards = numberOfCards,
                        isDarkTheme = isDarkTheme,
                    )
                    val hintViewData = widgetUniversalViewDataMapper.mapToHint(
                        retroactiveTrackingMode = retroactiveTrackingMode,
                    )
                    it + repeatViewData + hintViewData
                }
            }

        val shortcutsViewData = recordsShortcutsViewDataInteractor.getShortcutsViewData(
            filter = filter,
            recordTypesMap = recordTypesMap,
            recordTags = recordTags,
            runningRecords = runningRecords,
            searchText = "",
            isDarkTheme = isDarkTheme,
        )

        val openInAppButtonViewData = widgetUniversalViewDataMapper.mapOpenInAppButton(
            isDarkTheme = isDarkTheme,
        )

        val bottomSpace = EmptySpaceViewData(
            id = "widgets_universal_nav_bar_space".hashCode().toLong(),
            height = EmptySpaceViewData.ViewDimension.ExactSizeDp(navBarHeightDp),
            wrapBefore = true,
        )

        return listOf(
            suggestionsViewData,
            filtersViewData,
            recordTypesViewData,
            shortcutsViewData,
        ).filter {
            it.isNotEmpty()
        }.addBetweenEach { index ->
            listOf(DividerViewData(index.toLong()))
        }.flatten()
            .plus(openInAppButtonViewData)
            .plus(bottomSpace)
    }

    companion object {
        private const val COMPLETE_TYPE_ANIMATION_MS = 1000L
    }
}
