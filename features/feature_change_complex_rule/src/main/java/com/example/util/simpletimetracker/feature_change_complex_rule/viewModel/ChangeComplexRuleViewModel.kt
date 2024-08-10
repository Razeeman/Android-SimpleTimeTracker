package com.example.util.simpletimetracker.feature_change_complex_rule.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.util.simpletimetracker.core.base.BaseViewModel
import com.example.util.simpletimetracker.core.extension.set
import com.example.util.simpletimetracker.core.interactor.SnackBarMessageNavigationInteractor
import com.example.util.simpletimetracker.domain.extension.addOrRemove
import com.example.util.simpletimetracker.domain.extension.orZero
import com.example.util.simpletimetracker.domain.interactor.ComplexRuleInteractor
import com.example.util.simpletimetracker.domain.interactor.ComplexRulesDataUpdateInteractor
import com.example.util.simpletimetracker.domain.model.ComplexRule
import com.example.util.simpletimetracker.domain.model.DayOfWeek
import com.example.util.simpletimetracker.feature_base_adapter.dayOfWeek.DayOfWeekViewData
import com.example.util.simpletimetracker.feature_base_adapter.recordType.RecordTypeViewData
import com.example.util.simpletimetracker.feature_change_complex_rule.R
import com.example.util.simpletimetracker.feature_change_complex_rule.adapter.ChangeComplexRuleActionViewData
import com.example.util.simpletimetracker.feature_change_complex_rule.interactor.ChangeComplexRuleViewDataInteractor
import com.example.util.simpletimetracker.feature_change_complex_rule.mapper.ChangeComplexRuleViewDataMapper
import com.example.util.simpletimetracker.feature_change_complex_rule.viewData.ChangeComplexRuleActionChooserViewData
import com.example.util.simpletimetracker.feature_change_complex_rule.viewData.ChangeComplexRuleChooserState
import com.example.util.simpletimetracker.feature_change_complex_rule.viewData.ChangeComplexRuleTypesChooserViewData
import com.example.util.simpletimetracker.navigation.Router
import com.example.util.simpletimetracker.navigation.params.screen.ChangeComplexRuleParams
import com.example.util.simpletimetracker.navigation.params.screen.TypesSelectionDialogParams
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChangeComplexRuleViewModel @Inject constructor(
    private val router: Router,
    private val complexRuleInteractor: ComplexRuleInteractor,
    private val changeComplexRuleViewDataMapper: ChangeComplexRuleViewDataMapper,
    private val changeComplexRuleViewDataInteractor: ChangeComplexRuleViewDataInteractor,
    private val snackBarMessageNavigationInteractor: SnackBarMessageNavigationInteractor,
    private val complexRulesDataUpdateInteractor: ComplexRulesDataUpdateInteractor,
) : BaseViewModel() {

    lateinit var extra: ChangeComplexRuleParams

    val actionViewData: LiveData<ChangeComplexRuleActionChooserViewData> = MutableLiveData()
    val startingTypesViewData: LiveData<ChangeComplexRuleTypesChooserViewData> = MutableLiveData()
    val currentTypesViewData: LiveData<ChangeComplexRuleTypesChooserViewData> = MutableLiveData()
    val daysOfWeekViewData: LiveData<ChangeComplexRuleTypesChooserViewData> = MutableLiveData()
    val chooserState: LiveData<ChangeComplexRuleChooserState> = MutableLiveData(
        ChangeComplexRuleChooserState(
            current = ChangeComplexRuleChooserState.State.Closed,
            previous = ChangeComplexRuleChooserState.State.Closed,
        ),
    )
    val deleteButtonEnabled: LiveData<Boolean> = MutableLiveData(true)
    val saveButtonEnabled: LiveData<Boolean> = MutableLiveData(true)
    val deleteIconVisibility: LiveData<Boolean> by lazy { MutableLiveData(ruleId != 0L) }

    private val ruleId: Long get() = (extra as? ChangeComplexRuleParams.Change)?.id.orZero()
    private var initialized: Boolean = false
    private var newAction: ComplexRule.Action? = null
    private var newAssignTagIds: Set<Long> = emptySet()
    private var originalAssignTagIds: Set<Long> = emptySet()
    private var newStartingTypeIds: Set<Long> = emptySet()
    private var originalStartingTypeIds: Set<Long> = emptySet()
    private var newCurrentTypeIds: Set<Long> = emptySet()
    private var originalCurrentTypeIds: Set<Long> = emptySet()
    private var newDaysOfWeek: Set<DayOfWeek> = emptySet()
    private var newDisabled: Boolean = false

    fun initialize() = viewModelScope.launch {
        if (initialized) return@launch
        initializeData()
        updateActionViewData()
        updateStartingTypesViewData()
        updateCurrentTypesViewData()
        updateDaysOfWeekViewData()
        initialized = true
    }

    fun onActionTypeChooserClick() {
        onNewChooserState(ChangeComplexRuleChooserState.State.Action)
    }

    fun onStartingTypesChooserClick() {
        onNewChooserState(ChangeComplexRuleChooserState.State.StartingTypes)
    }

    fun onCurrentTypesChooserClick() {
        onNewChooserState(ChangeComplexRuleChooserState.State.CurrentTypes)
    }

    fun onDaysOfWeekChooserClick() {
        onNewChooserState(ChangeComplexRuleChooserState.State.DayOfWeek)
    }

    fun onActionClick(item: ChangeComplexRuleActionViewData) {
        when (item.type) {
            ChangeComplexRuleActionViewData.Type.AllowMultitasking -> {
                newAssignTagIds = emptySet()
                newAction = changeComplexRuleViewDataMapper.mapAction(item.type)
                updateActionViewData()
                onActionTypeChooserClick()
            }
            ChangeComplexRuleActionViewData.Type.DisallowMultitasking -> {
                newAssignTagIds = emptySet()
                newAction = changeComplexRuleViewDataMapper.mapAction(item.type)
                updateActionViewData()
                onActionTypeChooserClick()
            }
            ChangeComplexRuleActionViewData.Type.AssignTag -> {
                TypesSelectionDialogParams(
                    tag = RECORD_TAG_SELECTION_DIALOG_TAG,
                    title = "",
                    subtitle = "",
                    type = TypesSelectionDialogParams.Type.Tag,
                    selectedTypeIds = newAssignTagIds.toList(),
                    isMultiSelectAvailable = true,
                    idsShouldBeVisible = originalAssignTagIds.toList(),
                ).let(router::navigate)
            }
        }
    }

    fun onStartingTypeClick(item: RecordTypeViewData) {
        newStartingTypeIds = newStartingTypeIds.toMutableSet().apply { addOrRemove(item.id) }
        updateStartingTypesViewData()
    }

    fun onCurrentTypeClick(item: RecordTypeViewData) {
        newCurrentTypeIds = newCurrentTypeIds.toMutableSet().apply { addOrRemove(item.id) }
        updateCurrentTypesViewData()
    }

    fun onDayOfWeekClick(data: DayOfWeekViewData) {
        newDaysOfWeek = newDaysOfWeek.toMutableSet().apply { addOrRemove(data.dayOfWeek) }
        updateDaysOfWeekViewData()
    }

    fun onDataSelected(dataIds: List<Long>, tag: String?) {
        when (tag) {
            RECORD_TAG_SELECTION_DIALOG_TAG -> {
                if (dataIds.isNotEmpty()) {
                    newAction = ComplexRule.Action.AssignTag
                    newAssignTagIds = dataIds.toSet()
                } else {
                    newAction = ComplexRule.Action.AllowMultitasking
                }
                updateActionViewData()
                onActionTypeChooserClick()
            }
        }
    }

    fun onDeleteClick() {
        (deleteButtonEnabled as MutableLiveData).value = false
        viewModelScope.launch {
            if (ruleId != 0L) {
                complexRuleInteractor.remove(ruleId)
                showMessage(R.string.change_complex_rule_removed)
                complexRulesDataUpdateInteractor.send()
                router.back()
            }
        }
    }

    fun onSaveClick() {
        val newAction = this.newAction
        if (newAction == null) {
            showMessage(R.string.change_complex_rule_choose_action)
            return
        }

        val isConditionsSelected = getCurrentRule(newAction).hasConditions
        if (!isConditionsSelected) {
            showMessage(R.string.change_complex_rule_choose_condition)
            return
        }

        saveButtonEnabled.set(false)
        viewModelScope.launch {
            getCurrentRule(newAction).let {
                complexRuleInteractor.add(it)
                complexRulesDataUpdateInteractor.send()
                router.back()
            }
        }
    }

    fun onBackPressed() {
        if (chooserState.value?.current !is ChangeComplexRuleChooserState.State.Closed) {
            onNewChooserState(ChangeComplexRuleChooserState.State.Closed)
        } else {
            router.back()
        }
    }

    private fun getCurrentRule(
        action: ComplexRule.Action,
    ): ComplexRule {
        return ComplexRule(
            id = ruleId,
            disabled = newDisabled,
            action = action,
            actionAssignTagIds = newAssignTagIds,
            conditionStartingTypeIds = newStartingTypeIds,
            conditionCurrentTypeIds = newCurrentTypeIds,
            conditionDaysOfWeek = newDaysOfWeek,
        )
    }

    private fun onNewChooserState(
        newState: ChangeComplexRuleChooserState.State,
    ) {
        val current = chooserState.value?.current
            ?: ChangeComplexRuleChooserState.State.Closed

        if (current == newState) {
            chooserState.set(
                ChangeComplexRuleChooserState(
                    current = ChangeComplexRuleChooserState.State.Closed,
                    previous = current,
                ),
            )
        } else {
            chooserState.set(
                ChangeComplexRuleChooserState(
                    current = newState,
                    previous = current,
                ),
            )
        }
    }

    private suspend fun initializeData() {
        val rule = complexRuleInteractor.get(ruleId) ?: return
        newAction = rule.action
        newAssignTagIds = rule.actionAssignTagIds
        originalAssignTagIds = rule.actionAssignTagIds
        newStartingTypeIds = rule.conditionStartingTypeIds
        originalStartingTypeIds = rule.conditionStartingTypeIds
        newCurrentTypeIds = rule.conditionCurrentTypeIds
        originalCurrentTypeIds = rule.conditionCurrentTypeIds
        newDaysOfWeek = rule.conditionDaysOfWeek
        newDisabled = rule.disabled
    }

    private fun updateActionViewData() = viewModelScope.launch {
        val data = loadActionViewData()
        actionViewData.set(data)
    }

    private fun loadActionViewData(): ChangeComplexRuleActionChooserViewData {
        return changeComplexRuleViewDataInteractor.getActionViewData(
            newActionType = newAction,
            newAssignTagIds = newAssignTagIds,
        )
    }

    private fun updateStartingTypesViewData() = viewModelScope.launch {
        val data = loadStartingTypesViewData()
        startingTypesViewData.set(data)
    }

    private suspend fun loadStartingTypesViewData(): ChangeComplexRuleTypesChooserViewData {
        return changeComplexRuleViewDataInteractor.getTypesViewData(
            selectedIds = newStartingTypeIds,
            originalSelectedIds = originalStartingTypeIds,
        )
    }

    private fun updateCurrentTypesViewData() = viewModelScope.launch {
        val data = loadCurrentTypesViewData()
        currentTypesViewData.set(data)
    }

    private suspend fun loadCurrentTypesViewData(): ChangeComplexRuleTypesChooserViewData {
        return changeComplexRuleViewDataInteractor.getTypesViewData(
            selectedIds = newCurrentTypeIds,
            originalSelectedIds = originalCurrentTypeIds,
        )
    }

    private fun updateDaysOfWeekViewData() = viewModelScope.launch {
        val data = loadDaysOfWeekViewData()
        daysOfWeekViewData.set(data)
    }

    private suspend fun loadDaysOfWeekViewData(): ChangeComplexRuleTypesChooserViewData {
        return changeComplexRuleViewDataInteractor.getDaysOfWeek(
            daysOfWeek = newDaysOfWeek,
        )
    }

    private fun showMessage(stringResId: Int) {
        snackBarMessageNavigationInteractor.showMessage(stringResId)
    }

    companion object {
        private const val RECORD_TAG_SELECTION_DIALOG_TAG = "RECORD_TAG_SELECTION_DIALOG_TAG"
    }
}
