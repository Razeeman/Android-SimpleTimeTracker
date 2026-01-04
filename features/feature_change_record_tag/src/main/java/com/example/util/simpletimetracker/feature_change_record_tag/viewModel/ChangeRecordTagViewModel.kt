package com.example.util.simpletimetracker.feature_change_record_tag.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.util.simpletimetracker.core.base.BaseViewModel
import com.example.util.simpletimetracker.core.base.ViewModelDelegate
import com.example.util.simpletimetracker.core.delegates.colorSelection.ColorSelectionViewModelDelegate
import com.example.util.simpletimetracker.core.delegates.colorSelection.ColorSelectionViewModelDelegateImpl
import com.example.util.simpletimetracker.core.delegates.iconSelection.viewModelDelegate.IconSelectionViewModelDelegate
import com.example.util.simpletimetracker.core.delegates.iconSelection.viewModelDelegate.IconSelectionViewModelDelegateImpl
import com.example.util.simpletimetracker.core.extension.set
import com.example.util.simpletimetracker.core.extension.trimIfNotBlank
import com.example.util.simpletimetracker.core.interactor.SnackBarMessageNavigationInteractor
import com.example.util.simpletimetracker.core.interactor.StatisticsDetailNavigationInteractor
import com.example.util.simpletimetracker.core.mapper.CategoryViewDataMapper
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.core.view.ViewChooserStateDelegate
import com.example.util.simpletimetracker.domain.color.model.AppColor
import com.example.util.simpletimetracker.domain.extension.addOrRemove
import com.example.util.simpletimetracker.domain.extension.orZero
import com.example.util.simpletimetracker.domain.notifications.interactor.UpdateExternalViewsInteractor
import com.example.util.simpletimetracker.domain.prefs.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.recordTag.interactor.RecordTagInteractor
import com.example.util.simpletimetracker.domain.recordTag.interactor.RecordTypeToDefaultTagInteractor
import com.example.util.simpletimetracker.domain.recordTag.interactor.RecordTypeToTagInteractor
import com.example.util.simpletimetracker.domain.recordTag.interactor.RemoveRecordTagMediator
import com.example.util.simpletimetracker.domain.recordTag.model.RecordTag
import com.example.util.simpletimetracker.domain.recordTag.model.RecordTagValueType
import com.example.util.simpletimetracker.domain.recordType.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.domain.recordType.model.RecordTypeGoal
import com.example.util.simpletimetracker.domain.statistics.model.ChartFilterType
import com.example.util.simpletimetracker.feature_base_adapter.buttonsRow.ButtonsRowItemViewData
import com.example.util.simpletimetracker.feature_base_adapter.buttonsRow.view.ButtonsRowViewData
import com.example.util.simpletimetracker.feature_base_adapter.category.CategoryViewData
import com.example.util.simpletimetracker.feature_base_adapter.recordType.RecordTypeViewData
import com.example.util.simpletimetracker.feature_change_goals.api.GoalsViewModelDelegate
import com.example.util.simpletimetracker.feature_change_record_tag.R
import com.example.util.simpletimetracker.feature_change_record_tag.interactor.ChangeRecordTagViewDataInteractor
import com.example.util.simpletimetracker.feature_change_record_tag.viewData.ChangeRecordTagButtonsRowId
import com.example.util.simpletimetracker.feature_change_record_tag.viewData.ChangeRecordTagChooserState
import com.example.util.simpletimetracker.feature_change_record_tag.viewData.ChangeRecordTagFieldsState
import com.example.util.simpletimetracker.feature_change_record_tag.viewData.ChangeRecordTagTypesViewData
import com.example.util.simpletimetracker.feature_change_record_tag.viewData.ChangeRecordTagValueTypeViewData
import com.example.util.simpletimetracker.feature_change_record_tag.viewData.ChangeRecordTagValueViewData
import com.example.util.simpletimetracker.navigation.Router
import com.example.util.simpletimetracker.navigation.params.screen.ChangeTagData
import com.example.util.simpletimetracker.navigation.params.screen.StandardDialogParams
import com.example.util.simpletimetracker.navigation.params.screen.TypesSelectionDialogParams
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChangeRecordTagViewModel @Inject constructor(
    private val router: Router,
    private val resourceRepo: ResourceRepo,
    private val changeRecordTagViewDataInteractor: ChangeRecordTagViewDataInteractor,
    private val recordTagInteractor: RecordTagInteractor,
    private val recordTypeInteractor: RecordTypeInteractor,
    private val recordTypeToTagInteractor: RecordTypeToTagInteractor,
    private val recordTypeToDefaultTagInteractor: RecordTypeToDefaultTagInteractor,
    private val prefsInteractor: PrefsInteractor,
    private val categoryViewDataMapper: CategoryViewDataMapper,
    private val snackBarMessageNavigationInteractor: SnackBarMessageNavigationInteractor,
    private val statisticsDetailNavigationInteractor: StatisticsDetailNavigationInteractor,
    private val removeRecordTagMediator: RemoveRecordTagMediator,
    private val externalViewsInteractor: UpdateExternalViewsInteractor,
    private val goalsViewModelDelegate: GoalsViewModelDelegate,
    private val colorSelectionViewModelDelegateImpl: ColorSelectionViewModelDelegateImpl,
    private val iconSelectionViewModelDelegateImpl: IconSelectionViewModelDelegateImpl,
) : BaseViewModel(),
    GoalsViewModelDelegate by goalsViewModelDelegate,
    ColorSelectionViewModelDelegate by colorSelectionViewModelDelegateImpl,
    IconSelectionViewModelDelegate by iconSelectionViewModelDelegateImpl {

    lateinit var extra: ChangeTagData

    val preview: LiveData<CategoryViewData.Record> by lazy {
        return@lazy MutableLiveData<CategoryViewData.Record>().let { initial ->
            viewModelScope.launch {
                initializePreviewViewData()
                initial.value = loadPreviewViewData()
            }
            initial
        }
    }
    val types: LiveData<ChangeRecordTagTypesViewData> by lazy {
        return@lazy MutableLiveData<ChangeRecordTagTypesViewData>().let { initial ->
            viewModelScope.launch {
                initializeTypes()
                initial.value = loadTypesViewData()
            }
            initial
        }
    }
    val defaultTypes: LiveData<ChangeRecordTagTypesViewData> by lazy {
        return@lazy MutableLiveData<ChangeRecordTagTypesViewData>().let { initial ->
            viewModelScope.launch {
                initializeDefaultTypes()
                initial.value = loadDefaultTypesViewData()
            }
            initial
        }
    }
    val chooserState: LiveData<ChangeRecordTagFieldsState> by lazy {
        return@lazy MutableLiveData<ChangeRecordTagFieldsState>(
            ChangeRecordTagFieldsState(
                chooserState = ViewChooserStateDelegate.States(
                    current = ChangeRecordTagChooserState.Closed,
                    previous = ChangeRecordTagChooserState.Closed,
                ),
                additionalFieldsVisible = false,
            ),
        ).also { viewModelScope.launch { initializeChooserState() } }
    }
    val noteState: LiveData<String> by lazy {
        return@lazy MutableLiveData<String>().let { initial ->
            viewModelScope.launch {
                initial.value = loadNoteState()
            }
            initial
        }
    }
    val valueState: LiveData<ChangeRecordTagValueViewData> by lazy {
        return@lazy MutableLiveData<ChangeRecordTagValueViewData>().let { initial ->
            viewModelScope.launch {
                initial.value = loadValueState(fromValueChange = false)
            }
            initial
        }
    }
    val archiveButtonEnabled: LiveData<Boolean> = MutableLiveData(true)
    val deleteButtonEnabled: LiveData<Boolean> = MutableLiveData(true)
    val saveButtonEnabled: LiveData<Boolean> = MutableLiveData(true)
    val nameErrorMessage: LiveData<String> = MutableLiveData("")
    val iconColorSourceSelected: LiveData<Boolean> = MutableLiveData(false)
    val archiveIconVisibility: LiveData<Boolean> by lazy { MutableLiveData(recordTagId != 0L) }
    val deleteIconVisibility: LiveData<Boolean> by lazy { MutableLiveData(recordTagId != 0L) }
    val statsIconVisibility: LiveData<Boolean> by lazy { MutableLiveData(recordTagId != 0L) }
    val keyboardVisibility: LiveData<Boolean> by lazy { MutableLiveData(recordTagId == 0L) }

    private val recordTagId: Long get() = (extra as? ChangeTagData.Change)?.id.orZero()
    private var newName: String = ""
    private var newIconColorSource: Long = 0L
    private var newTypeIds: Set<Long> = emptySet()
    private var newDefaultTypeIds: Set<Long> = emptySet()
    private var newNote: String = ""
    private var initialTypeIds: Set<Long> = emptySet()
    private var initialDefaultTypeIds: Set<Long> = emptySet()
    private var newValueType: RecordTagValueType = RecordTagValueType.NONE
    private var newValueSuffix: String = ""

    init {
        colorSelectionViewModelDelegateImpl.attach(getColorSelectionDelegateParent())
        iconSelectionViewModelDelegateImpl.attach(getIconSelectionDelegateParent())
    }

    override fun onCleared() {
        (goalsViewModelDelegate as? ViewModelDelegate)?.clear()
        colorSelectionViewModelDelegateImpl.clear()
        iconSelectionViewModelDelegateImpl.clear()
        super.onCleared()
    }

    fun onNameChange(name: String) {
        viewModelScope.launch {
            if (name != newName) {
                newName = name
                updatePreview()
            }
        }
        viewModelScope.launch {
            val items = recordTagInteractor.get(name).filter { it.id != recordTagId }
            val error = if (items.isNotEmpty()) {
                resourceRepo.getString(R.string.change_record_message_name_exist)
            } else {
                ""
            }
            nameErrorMessage.set(error)
        }
    }

    fun onNoteChange(note: String) {
        if (note != newNote) {
            newNote = note
            updateNoteState()
        }
    }

    fun onColorChooserClick() {
        onNewChooserState(ChangeRecordTagChooserState.Color)
    }

    fun onIconChooserClick() {
        onNewChooserState(ChangeRecordTagChooserState.Icon)
    }

    fun onTypeChooserClick() {
        onNewChooserState(ChangeRecordTagChooserState.Type)
    }

    fun onDefaultTypeChooserClick() {
        onNewChooserState(ChangeRecordTagChooserState.DefaultType)
    }

    fun onGoalTimeChooserClick() {
        onNewChooserState(ChangeRecordTagChooserState.GoalTime)
    }

    fun onValueTypeChooserClick() {
        onNewChooserState(ChangeRecordTagChooserState.ValueType)
    }

    fun onTypeClick(item: RecordTypeViewData) {
        viewModelScope.launch {
            newTypeIds = newTypeIds.toMutableSet().apply { addOrRemove(item.id) }
            updateTypesViewData()
        }
    }

    fun onDefaultTypeClick(item: RecordTypeViewData) {
        viewModelScope.launch {
            newDefaultTypeIds = newDefaultTypeIds.toMutableSet().apply {
                if (item.id in this) remove(item.id) else add(item.id)
            }
            updateDefaultTypesViewData()
        }
    }

    fun onSelectActivityClick() {
        TypesSelectionDialogParams(
            tag = TYPE_SELECTION_TAG,
            title = resourceRepo.getString(R.string.change_record_message_choose_type),
            subtitle = "",
            type = TypesSelectionDialogParams.Type.Activity,
            selectedTypeIds = listOf(newIconColorSource),
            selectedTagValues = emptyList(),
            isMultiSelectAvailable = false,
            idsShouldBeVisible = listOf(newIconColorSource),
            showHints = true,
            allowTagValueSelection = false,
        ).let(router::navigate)
    }

    fun onTypesSelected(typeIds: List<Long>, tag: String?) = viewModelScope.launch {
        if (tag != TYPE_SELECTION_TAG) return@launch

        val typeId = typeIds.firstOrNull() ?: return@launch
        val type = recordTypeInteractor.get(typeId) ?: return@launch

        iconSelectionViewModelDelegateImpl.newIcon = type.icon
        colorSelectionViewModelDelegateImpl.newColor = type.color
        newIconColorSource = type.id
        colorSelectionViewModelDelegateImpl.update()
        updatePreview()
        updateIconColorSourceSelected()
    }

    fun onButtonsRowClick(
        block: ButtonsRowItemViewData.ButtonsRowId,
        viewData: ButtonsRowViewData,
    ) {
        if (block !is ChangeRecordTagButtonsRowId) return
        newValueType = when (viewData) {
            is ChangeRecordTagValueTypeViewData -> viewData.valueType
            else -> return
        }
        updateValueState()
    }

    fun onValueChange(valueText: String) {
        if (valueText != newValueSuffix) {
            newValueSuffix = valueText
            updateValueState(fromValueChange = true)
        }
    }

    fun onArchiveClick() {
        archiveButtonEnabled.set(false)
        viewModelScope.launch {
            if (recordTagId != 0L) {
                recordTagInteractor.archive(recordTagId)
                externalViewsInteractor.onTagArchive()
                showArchivedMessage(R.string.change_record_tag_archived)
                (keyboardVisibility as MutableLiveData).value = false
                router.back()
            }
        }
    }

    fun onDeleteClick() {
        router.navigate(
            StandardDialogParams(
                tag = DELETE_ALERT_DIALOG_TAG,
                title = resourceRepo.getString(R.string.change_record_type_delete_alert),
                message = resourceRepo.getString(R.string.archive_deletion_alert),
                btnPositive = resourceRepo.getString(R.string.ok),
                btnNegative = resourceRepo.getString(R.string.cancel),
            ),
        )
    }

    fun onStatisticsClick() = viewModelScope.launch {
        if (recordTagId == 0L) return@launch
        val preview = preview.value ?: return@launch

        statisticsDetailNavigationInteractor.navigate(
            transitionName = "",
            filterType = ChartFilterType.RECORD_TAG,
            shift = 0,
            sharedElements = emptyMap(),
            itemId = recordTagId,
            itemName = preview.name,
            itemIcon = preview.icon,
            itemColor = preview.color,
        )
    }

    fun onMoreFieldsClick() = viewModelScope.launch {
        val newValue = !prefsInteractor.getTagAdditionalFieldsShown()
        prefsInteractor.setTagAdditionalFieldsShown(newValue)

        val currentState = chooserState.value ?: return@launch
        val newState = ChangeRecordTagFieldsState(
            chooserState = ViewChooserStateDelegate.States(
                current = currentState.chooserState.current,
                previous = currentState.chooserState.current,
            ),
            additionalFieldsVisible = newValue,
        )
        chooserState.set(newState)
    }

    fun onSaveClick() {
        if (newName.isEmpty()) {
            showMessage(R.string.change_category_message_choose_name)
            return
        }
        (saveButtonEnabled as MutableLiveData).value = false
        viewModelScope.launch {
            // Zero id creates new record
            RecordTag(
                id = recordTagId,
                name = newName.trimIfNotBlank(),
                icon = iconSelectionViewModelDelegateImpl.newIcon,
                color = colorSelectionViewModelDelegateImpl.newColor,
                iconColorSource = newIconColorSource,
                note = newNote,
                valueType = newValueType,
                valueSuffix = newValueSuffix,
            ).let {
                val addedId = recordTagInteractor.add(it)
                saveTypes(addedId)
                saveDefaultTypes(addedId)
                goalsViewModelDelegate.saveGoals(RecordTypeGoal.IdData.Tag(addedId))
                externalViewsInteractor.onTagAddOrChange()
                (keyboardVisibility as MutableLiveData).value = false
                router.back()
            }
        }
    }

    fun onBackPressed() {
        if (chooserState.value?.chooserState?.current !is ChangeRecordTagChooserState.Closed) {
            onNewChooserState(ChangeRecordTagChooserState.Closed)
        } else {
            router.back()
        }
    }

    fun onPositiveDialogClick(tag: String?) {
        when (tag) {
            DELETE_ALERT_DIALOG_TAG -> delete()
        }
    }

    private fun delete() {
        router.back() // Close dialog.
        deleteButtonEnabled.set(false)
        viewModelScope.launch {
            if (recordTagId != 0L) {
                removeRecordTagMediator.remove(recordTagId, fromArchive = false)
                showMessage(R.string.archive_tag_deleted)
                keyboardVisibility.set(false)
                router.back()
            }
        }
    }

    private suspend fun saveTypes(tagId: Long) {
        val addedTypes = newTypeIds.filterNot { it in initialTypeIds }
        val removedTypes = initialTypeIds.filterNot { it in newTypeIds }

        recordTypeToTagInteractor.addTypes(tagId, addedTypes)
        recordTypeToTagInteractor.removeTypes(tagId, removedTypes)
    }

    private suspend fun saveDefaultTypes(tagId: Long) {
        val addedTypes = newDefaultTypeIds.filterNot { it in initialDefaultTypeIds }
        val removedTypes = initialDefaultTypeIds.filterNot { it in newDefaultTypeIds }

        recordTypeToDefaultTagInteractor.addTypes(tagId, addedTypes)
        recordTypeToDefaultTagInteractor.removeTypes(tagId, removedTypes)
    }

    private fun onNewChooserState(
        newState: ChangeRecordTagChooserState,
    ) {
        val currentState = chooserState.value ?: return
        val current = currentState.chooserState.current

        val newChooserState = if (current == newState) {
            ViewChooserStateDelegate.States(
                current = ChangeRecordTagChooserState.Closed,
                previous = current,
            )
        } else {
            ViewChooserStateDelegate.States(
                current = newState,
                previous = current,
            )
        }

        keyboardVisibility.set(false)
        chooserState.set(currentState.copy(chooserState = newChooserState))
    }

    private suspend fun initializeChooserState() {
        val current = chooserState.value ?: return
        val newState = current.copy(
            additionalFieldsVisible = prefsInteractor.getTagAdditionalFieldsShown(),
        )
        chooserState.set(newState)
    }

    private suspend fun initializeTypes() {
        when (val extra = extra) {
            is ChangeTagData.Change -> {
                val assignedTypes = recordTypeToTagInteractor.getTypes(extra.id)
                newTypeIds = assignedTypes
                initialTypeIds = assignedTypes
            }
            is ChangeTagData.New -> {
                newTypeIds = setOfNotNull(extra.preselectedTypeId)
                initialTypeIds = emptySet()
            }
        }
    }

    private suspend fun initializeDefaultTypes() {
        when (val extra = extra) {
            is ChangeTagData.Change -> {
                val assignedDefaultTypes = recordTypeToDefaultTagInteractor.getTypes(extra.id)
                newDefaultTypeIds = assignedDefaultTypes
                initialDefaultTypeIds = assignedDefaultTypes
            }
            is ChangeTagData.New -> {
                newDefaultTypeIds = emptySet()
                initialDefaultTypeIds = emptySet()
            }
        }
    }

    private suspend fun initializePreviewViewData() {
        when (val extra = extra) {
            is ChangeTagData.Change -> {
                recordTagInteractor.get(extra.id)?.let {
                    newName = it.name
                    goalsViewModelDelegate.initialize(RecordTypeGoal.IdData.Tag(it.id))
                    iconSelectionViewModelDelegateImpl.newIcon = it.icon
                    colorSelectionViewModelDelegateImpl.newColor = it.color
                    newIconColorSource = it.iconColorSource
                    newNote = it.note
                    newValueType = it.valueType
                    newValueSuffix = it.valueSuffix
                    iconSelectionViewModelDelegateImpl.update()
                    colorSelectionViewModelDelegateImpl.update()
                    updateIconColorSourceSelected()
                    updateNoteState()
                    updateValueState()
                }
            }
            is ChangeTagData.New -> {
                recordTypeInteractor.get(extra.preselectedTypeId.orZero())?.let { type ->
                    iconSelectionViewModelDelegateImpl.newIcon = type.icon
                    colorSelectionViewModelDelegateImpl.newColor = type.color
                    newIconColorSource = type.id
                    iconSelectionViewModelDelegateImpl.update()
                    colorSelectionViewModelDelegateImpl.update()
                    updateIconColorSourceSelected()
                }
            }
        }
    }

    private fun getColorSelectionDelegateParent(): ColorSelectionViewModelDelegate.Parent {
        return object : ColorSelectionViewModelDelegate.Parent {
            override suspend fun update() {
                updatePreview()
                updateIconColorSourceSelected()
                iconSelectionViewModelDelegateImpl.update()
            }

            override fun onColorSelected() {
                viewModelScope.launch {
                    if (newIconColorSource == 0L) return@launch
                    val type = recordTypeInteractor.get(newIconColorSource)
                        ?: return@launch
                    iconSelectionViewModelDelegateImpl.newIcon = type.icon
                }
                newIconColorSource = 0
            }

            override suspend fun isColorSelectedCheck(): Boolean {
                return newIconColorSource == 0L
            }
        }
    }

    private fun getIconSelectionDelegateParent(): IconSelectionViewModelDelegate.Parent {
        return object : IconSelectionViewModelDelegate.Parent {
            override fun keyboardVisibility(isVisible: Boolean) {
                keyboardVisibility.set(isVisible)
            }

            override suspend fun update() {
                updatePreview()
                updateIconColorSourceSelected()
            }

            override fun onIconSelected() {
                viewModelScope.launch {
                    if (newIconColorSource == 0L) return@launch
                    val type = recordTypeInteractor.get(newIconColorSource)
                        ?: return@launch
                    colorSelectionViewModelDelegateImpl.newColor = type.color
                }
                newIconColorSource = 0
            }

            override fun getColor(): AppColor {
                return colorSelectionViewModelDelegateImpl.newColor
            }
        }
    }

    private fun updateIconColorSourceSelected() {
        iconColorSourceSelected.set(newIconColorSource != 0L)
    }

    private fun updatePreview() = viewModelScope.launch {
        preview.set(loadPreviewViewData())
    }

    private suspend fun loadPreviewViewData(): CategoryViewData.Record {
        val tag = RecordTag(
            name = newName,
            icon = iconSelectionViewModelDelegateImpl.newIcon,
            color = colorSelectionViewModelDelegateImpl.newColor,
            iconColorSource = newIconColorSource,
            note = newNote,
            valueType = newValueType,
            valueSuffix = newValueSuffix,
        )
        val isDarkTheme = prefsInteractor.getDarkMode()
        val types = recordTypeInteractor.getAll().associateBy { it.id }

        return categoryViewDataMapper.mapRecordTag(
            tag = tag,
            types = types,
            isDarkTheme = isDarkTheme,
        )
    }

    private fun updateTypesViewData() = viewModelScope.launch {
        val data = loadTypesViewData()
        types.set(data)
    }

    private suspend fun loadTypesViewData(): ChangeRecordTagTypesViewData {
        return changeRecordTagViewDataInteractor.getTypesViewData(
            selectedTypes = newTypeIds,
            initialTypeIds = initialTypeIds,
        )
    }

    private fun updateDefaultTypesViewData() = viewModelScope.launch {
        val data = loadDefaultTypesViewData()
        defaultTypes.set(data)
    }

    private suspend fun loadDefaultTypesViewData(): ChangeRecordTagTypesViewData {
        return changeRecordTagViewDataInteractor.getDefaultTypesViewData(newDefaultTypeIds)
    }

    private fun updateNoteState() {
        val data = loadNoteState()
        noteState.set(data)
    }

    private fun loadNoteState(): String {
        return newNote
    }

    private fun updateValueState(
        fromValueChange: Boolean = false,
    ) {
        val data = loadValueState(fromValueChange)
        valueState.set(data)
    }

    private fun loadValueState(
        fromValueChange: Boolean,
    ): ChangeRecordTagValueViewData {
        return changeRecordTagViewDataInteractor.getTagValueState(
            valueType = newValueType,
            valueSuffix = newValueSuffix,
            fromValueChange = fromValueChange,
        )
    }

    private fun showMessage(stringResId: Int) {
        snackBarMessageNavigationInteractor.showMessage(stringResId)
    }

    private fun showArchivedMessage(stringResId: Int) {
        snackBarMessageNavigationInteractor.showArchiveMessage(stringResId)
    }

    companion object {
        private const val TYPE_SELECTION_TAG = "types_selection_tag"
        private const val DELETE_ALERT_DIALOG_TAG = "delete_alert_dialog_tag"
    }
}
