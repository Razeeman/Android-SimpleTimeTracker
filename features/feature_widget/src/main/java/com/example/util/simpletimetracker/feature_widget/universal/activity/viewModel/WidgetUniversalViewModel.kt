package com.example.util.simpletimetracker.feature_widget.universal.activity.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.util.simpletimetracker.core.extension.set
import com.example.util.simpletimetracker.core.interactor.ActivityFilterViewDataInteractor
import com.example.util.simpletimetracker.core.interactor.GetCurrentRecordsDurationInteractor
import com.example.util.simpletimetracker.core.interactor.RecordRepeatInteractor
import com.example.util.simpletimetracker.core.mapper.RecordTypeViewDataMapper
import com.example.util.simpletimetracker.domain.interactor.ActivityFilterInteractor
import com.example.util.simpletimetracker.domain.interactor.AddRunningRecordMediator
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTypeGoalInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.domain.interactor.RemoveRunningRecordMediator
import com.example.util.simpletimetracker.domain.interactor.RunningRecordInteractor
import com.example.util.simpletimetracker.domain.model.RecordType
import com.example.util.simpletimetracker.domain.model.RunningRecord
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.activityFilter.ActivityFilterViewData
import com.example.util.simpletimetracker.feature_base_adapter.divider.DividerViewData
import com.example.util.simpletimetracker.feature_base_adapter.loader.LoaderViewData
import com.example.util.simpletimetracker.feature_base_adapter.recordType.RecordTypeViewData
import com.example.util.simpletimetracker.feature_base_adapter.recordTypeSpecial.RunningRecordTypeSpecialViewData
import com.example.util.simpletimetracker.feature_widget.universal.mapper.WidgetUniversalViewDataMapper
import com.example.util.simpletimetracker.navigation.Router
import com.example.util.simpletimetracker.navigation.params.screen.RecordTagSelectionParams
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WidgetUniversalViewModel @Inject constructor(
    private val router: Router,
    private val addRunningRecordMediator: AddRunningRecordMediator,
    private val removeRunningRecordMediator: RemoveRunningRecordMediator,
    private val recordTypeInteractor: RecordTypeInteractor,
    private val runningRecordInteractor: RunningRecordInteractor,
    private val prefsInteractor: PrefsInteractor,
    private val activityFilterInteractor: ActivityFilterInteractor,
    private val activityFilterViewDataInteractor: ActivityFilterViewDataInteractor,
    private val widgetUniversalViewDataMapper: WidgetUniversalViewDataMapper,
    private val recordTypeViewDataMapper: RecordTypeViewDataMapper,
    private val recordRepeatInteractor: RecordRepeatInteractor,
    private val recordTypeGoalInteractor: RecordTypeGoalInteractor,
    private val getCurrentRecordsDurationInteractor: GetCurrentRecordsDurationInteractor,
) : ViewModel() {

    val recordTypes: LiveData<List<ViewHolderType>> by lazy {
        return@lazy MutableLiveData<List<ViewHolderType>>().let { initial ->
            viewModelScope.launch {
                initial.value = listOf(LoaderViewData())
                initial.value = loadRecordTypesViewData()
            }
            initial
        }
    }

    val exit: LiveData<Unit> = MutableLiveData()

    fun onRecordTypeClick(item: RecordTypeViewData) {
        viewModelScope.launch {
            val runningRecord = runningRecordInteractor.get(item.id)
            var started = false

            if (runningRecord != null) {
                // Stop running record, add new record
                removeRunningRecordMediator.removeWithRecordAdd(runningRecord)
            } else {
                // Start running record
                started = addRunningRecordMediator.tryStartTimer(
                    typeId = item.id,
                    onNeedToShowTagSelection = { showTagSelection(item.id) },
                )
            }

            updateRecordTypesViewData()
            if (started) exit.set(Unit)
        }
    }

    fun onSpecialRecordTypeClick(item: RunningRecordTypeSpecialViewData) {
        viewModelScope.launch {
            val started: Boolean

            when (item.type) {
                is RunningRecordTypeSpecialViewData.Type.Repeat -> {
                    started = recordRepeatInteractor.repeat()
                }
                else -> return@launch
            }

            updateRecordTypesViewData()
            if (started) exit.set(Unit)
        }
    }

    fun onActivityFilterClick(item: ActivityFilterViewData) {
        viewModelScope.launch {
            activityFilterInteractor.changeSelected(item.id, !item.selected)
            updateRecordTypesViewData()
        }
    }

    fun onTagSelected() {
        updateRecordTypesViewData()
        exit.set(Unit)
    }

    private fun showTagSelection(typeId: Long) {
        router.navigate(RecordTagSelectionParams(typeId))
    }

    private fun updateRecordTypesViewData() = viewModelScope.launch {
        val data = loadRecordTypesViewData()
        (recordTypes as MutableLiveData).value = data
    }

    private suspend fun loadRecordTypesViewData(): List<ViewHolderType> {
        val runningRecords = runningRecordInteractor.getAll()
        val recordTypes = recordTypeInteractor.getAll()
        val goals = recordTypeGoalInteractor.getAllTypeGoals().groupBy { it.idData.value }
        val recordTypesRunning = runningRecords.map(RunningRecord::id)
        val numberOfCards = prefsInteractor.getNumberOfCards()
        val isDarkTheme = prefsInteractor.getDarkMode()
        val allDailyCurrents = if (goals.isNotEmpty()) {
            getCurrentRecordsDurationInteractor.getAllDailyCurrents(
                typeIds = recordTypes.map(RecordType::id),
                runningRecords = runningRecords,
            )
        } else {
            // No goals - no need to calculate durations.
            emptyMap()
        }

        val filter = activityFilterViewDataInteractor.getFilter()
        val filtersViewData = activityFilterViewDataInteractor.getFilterViewData(
            filter = filter,
            isDarkTheme = isDarkTheme,
            appendAddButton = false,
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
                    isChecked = recordTypeViewDataMapper.mapGoalCheckmark(
                        type = it,
                        goals = goals,
                        allDailyCurrents = allDailyCurrents,
                    ),
                )
            }
        val repeatViewData = recordTypeViewDataMapper.mapToRepeatItem(
            numberOfCards = numberOfCards,
            isDarkTheme = isDarkTheme,
        )

        return mutableListOf<ViewHolderType>().apply {
            if (filtersViewData.isNotEmpty()) {
                filtersViewData.let(::addAll)
                DividerViewData(1).let(::add)
            }

            if (recordTypesViewData.isEmpty()) {
                recordTypeViewDataMapper.mapToEmpty().let(::addAll)
            } else {
                recordTypesViewData.let(::addAll)
                repeatViewData.let(::add)
                widgetUniversalViewDataMapper.mapToHint().let(::add)
            }
        }
    }
}
