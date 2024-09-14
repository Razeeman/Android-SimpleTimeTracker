package com.example.util.simpletimetracker.feature_change_record_tag.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.util.simpletimetracker.core.delegates.colorSelection.ColorSelectionViewModelDelegate
import com.example.util.simpletimetracker.core.delegates.colorSelection.ColorSelectionViewModelDelegateImpl
import com.example.util.simpletimetracker.core.delegates.iconSelection.viewModelDelegate.IconSelectionViewModelDelegate
import com.example.util.simpletimetracker.core.delegates.iconSelection.viewModelDelegate.IconSelectionViewModelDelegateImpl
import com.example.util.simpletimetracker.core.extension.set
import com.example.util.simpletimetracker.core.interactor.SnackBarMessageNavigationInteractor
import com.example.util.simpletimetracker.core.interactor.StatisticsDetailNavigationInteractor
import com.example.util.simpletimetracker.core.mapper.CategoryViewDataMapper
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.core.view.ViewChooserStateDelegate
import com.example.util.simpletimetracker.domain.extension.orZero
import com.example.util.simpletimetracker.domain.interactor.NotificationTypeInteractor
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTagInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTypeToDefaultTagInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTypeToTagInteractor
import com.example.util.simpletimetracker.domain.interactor.RemoveRecordTagMediator
import com.example.util.simpletimetracker.domain.interactor.WearInteractor
import com.example.util.simpletimetracker.domain.model.AppColor
import com.example.util.simpletimetracker.domain.model.ChartFilterType
import com.example.util.simpletimetracker.domain.model.RecordTag
import com.example.util.simpletimetracker.feature_base_adapter.category.CategoryViewData
import com.example.util.simpletimetracker.feature_base_adapter.recordType.RecordTypeViewData
import com.example.util.simpletimetracker.feature_change_record_tag.R
import com.example.util.simpletimetracker.feature_change_record_tag.interactor.ChangeRecordTagViewDataInteractor
import com.example.util.simpletimetracker.feature_change_record_tag.viewData.ChangeRecordTagChooserState
import com.example.util.simpletimetracker.feature_change_record_tag.viewData.ChangeRecordTagTypesViewData
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
    private val notificationTypeInteractor: NotificationTypeInteractor,
    private val wearInteractor: WearInteractor,
    private val categoryViewDataMapper: CategoryViewDataMapper,
    private val snackBarMessageNavigationInteractor: SnackBarMessageNavigationInteractor,
    private val statisticsDetailNavigationInteractor: StatisticsDetailNavigationInteractor,
    private val removeRecordTagMediator: RemoveRecordTagMediator,
    private val colorSelectionViewModelDelegateImpl: ColorSelectionViewModelDelegateImpl,
    private val iconSelectionViewModelDelegateImpl: IconSelectionViewModelDelegateImpl,
) : ViewModel(),
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
    val chooserState: LiveData<ViewChooserStateDelegate.States> by lazy {
        return@lazy MutableLiveData(
            ViewChooserStateDelegate.States(
                current = ChangeRecordTagChooserState.Closed,
                previous = ChangeRecordTagChooserState.Closed,
            ),
        )
    }
    val noteState: LiveData<String> by lazy {
        return@lazy MutableLiveData<String>().let { initial ->
            viewModelScope.launch {
                initial.value = loadNoteState()
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

    init {
        colorSelectionViewModelDelegateImpl.attach(getColorSelectionDelegateParent())
        iconSelectionViewModelDelegateImpl.attach(getIconSelectionDelegateParent())
    }

    override fun onCleared() {
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
            val type = recordTagInteractor.get(name)
            val error = if (type != null && type.id != recordTagId) {
                resourceRepo.getString(R.string.change_record_message_name_exist)
            } else {
                ""
            }
            nameErrorMessage.set(error)
        }
    }

    fun onNoteChange(note: String) {
        viewModelScope.launch {
            if (note != newNote) {
                newNote = note
                updateNoteState()
            }
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

    fun onTypeClick(item: RecordTypeViewData) {
        viewModelScope.launch {
            newTypeIds = newTypeIds.toMutableSet().apply {
                if (item.id in this) remove(item.id) else add(item.id)
            }
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
            isMultiSelectAvailable = false,
            idsShouldBeVisible = listOf(newIconColorSource),
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

    fun onArchiveClick() {
        archiveButtonEnabled.set(false)
        viewModelScope.launch {
            if (recordTagId != 0L) {
                recordTagInteractor.archive(recordTagId)
                notificationTypeInteractor.updateNotifications()
                wearInteractor.update()
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
                name = newName,
                icon = iconSelectionViewModelDelegateImpl.newIcon,
                color = colorSelectionViewModelDelegateImpl.newColor,
                iconColorSource = newIconColorSource,
                note = newNote,
            ).let {
                val addedId = recordTagInteractor.add(it)
                saveTypes(addedId)
                saveDefaultTypes(addedId)
                notificationTypeInteractor.updateNotifications()
                wearInteractor.update()
                (keyboardVisibility as MutableLiveData).value = false
                router.back()
            }
        }
    }

    fun onBackPressed() {
        if (chooserState.value?.current !is ChangeRecordTagChooserState.Closed) {
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
                removeRecordTagMediator.remove(recordTagId)
                notificationTypeInteractor.updateNotifications()
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
        val current = chooserState.value?.current
            ?: ChangeRecordTagChooserState.Closed
        keyboardVisibility.set(false)
        if (current == newState) {
            chooserState.set(
                ViewChooserStateDelegate.States(
                    current = ChangeRecordTagChooserState.Closed,
                    previous = current,
                ),
            )
        } else {
            chooserState.set(
                ViewChooserStateDelegate.States(
                    current = newState,
                    previous = current,
                ),
            )
        }
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
                    iconSelectionViewModelDelegateImpl.newIcon = it.icon
                    colorSelectionViewModelDelegateImpl.newColor = it.color
                    newIconColorSource = it.iconColorSource
                    newNote = it.note
                    iconSelectionViewModelDelegateImpl.update()
                    colorSelectionViewModelDelegateImpl.update()
                    updateIconColorSourceSelected()
                    updateNoteState()
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
        )
        val isDarkTheme = prefsInteractor.getDarkMode()
        val type = recordTypeInteractor.get(newIconColorSource)

        return categoryViewDataMapper.mapRecordTag(
            tag = tag,
            type = type,
            isDarkTheme = isDarkTheme,
        )
    }

    private fun updateTypesViewData() = viewModelScope.launch {
        val data = loadTypesViewData()
        types.set(data)
    }

    private suspend fun loadTypesViewData(): ChangeRecordTagTypesViewData {
        return changeRecordTagViewDataInteractor.getTypesViewData(newTypeIds)
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
