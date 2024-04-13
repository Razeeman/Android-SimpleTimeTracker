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
import com.example.util.simpletimetracker.core.mapper.CategoryViewDataMapper
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.extension.orZero
import com.example.util.simpletimetracker.domain.interactor.NotificationTypeInteractor
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTagInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTypeToTagInteractor
import com.example.util.simpletimetracker.domain.interactor.WearInteractor
import com.example.util.simpletimetracker.domain.model.AppColor
import com.example.util.simpletimetracker.domain.model.RecordTag
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.category.CategoryViewData
import com.example.util.simpletimetracker.feature_base_adapter.recordType.RecordTypeViewData
import com.example.util.simpletimetracker.feature_change_record_tag.R
import com.example.util.simpletimetracker.feature_change_record_tag.interactor.ChangeRecordTagViewDataInteractor
import com.example.util.simpletimetracker.feature_change_record_tag.viewData.ChangeRecordTagTypeChooserState
import com.example.util.simpletimetracker.navigation.Router
import com.example.util.simpletimetracker.navigation.params.screen.ChangeTagData
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
    private val prefsInteractor: PrefsInteractor,
    private val notificationTypeInteractor: NotificationTypeInteractor,
    private val wearInteractor: WearInteractor,
    private val categoryViewDataMapper: CategoryViewDataMapper,
    private val snackBarMessageNavigationInteractor: SnackBarMessageNavigationInteractor,
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
    val types: LiveData<List<ViewHolderType>> by lazy {
        return@lazy MutableLiveData<List<ViewHolderType>>().let { initial ->
            viewModelScope.launch {
                initializeTypes()
                initial.value = loadTypesViewData()
            }
            initial
        }
    }
    val chooserState: LiveData<ChangeRecordTagTypeChooserState> = MutableLiveData(
        ChangeRecordTagTypeChooserState(
            current = ChangeRecordTagTypeChooserState.State.Closed,
            previous = ChangeRecordTagTypeChooserState.State.Closed,
        ),
    )
    val deleteButtonEnabled: LiveData<Boolean> = MutableLiveData(true)
    val saveButtonEnabled: LiveData<Boolean> = MutableLiveData(true)
    val iconColorSourceSelected: LiveData<Boolean> = MutableLiveData(false)
    val deleteIconVisibility: LiveData<Boolean> by lazy { MutableLiveData(recordTagId != 0L) }
    val keyboardVisibility: LiveData<Boolean> by lazy { MutableLiveData(recordTagId == 0L) }

    private val recordTagId: Long get() = (extra as? ChangeTagData.Change)?.id.orZero()
    private var newName: String = ""
    private var newIconColorSource: Long = 0L
    private var newTypeIds: Set<Long> = emptySet()
    private var initialTypeIds: Set<Long> = emptySet()

    init {
        colorSelectionViewModelDelegateImpl.attach(getColorSelectionDelegateParent())
        iconSelectionViewModelDelegateImpl.attach(getIconSelectionDelegateParent())
    }

    // TODO TAGS add "remove icon" button
    fun onNameChange(name: String) {
        viewModelScope.launch {
            if (name != newName) {
                newName = name
                updatePreview()
            }
        }
    }

    fun onColorChooserClick() {
        onNewChooserState(ChangeRecordTagTypeChooserState.State.Color)
    }

    fun onIconChooserClick() {
        onNewChooserState(ChangeRecordTagTypeChooserState.State.Icon)
    }

    fun onTypeChooserClick() {
        onNewChooserState(ChangeRecordTagTypeChooserState.State.Type)
    }

    fun onTypeClick(item: RecordTypeViewData) {
        viewModelScope.launch {
            newTypeIds = newTypeIds.toMutableSet().apply {
                if (item.id in this) remove(item.id) else add(item.id)
            }
            updateTypesViewData()
        }
    }

    fun onSelectActivityClick() {
        TypesSelectionDialogParams(
            tag = TYPE_SELECTION_TAG,
            title = resourceRepo.getString(R.string.change_record_message_choose_type),
            subtitle = "",
            selectedTypeIds = listOf(newIconColorSource),
            isMultiSelectAvailable = false,
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

    fun onDeleteClick() {
        (deleteButtonEnabled as MutableLiveData).value = false
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
            ).let {
                val addedId = recordTagInteractor.add(it)
                saveTypes(addedId)
                notificationTypeInteractor.updateNotifications()
                wearInteractor.update()
                (keyboardVisibility as MutableLiveData).value = false
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

    private fun onNewChooserState(
        newState: ChangeRecordTagTypeChooserState.State,
    ) {
        val current = chooserState.value?.current
            ?: ChangeRecordTagTypeChooserState.State.Closed
        keyboardVisibility.set(false)
        if (current == newState) {
            chooserState.set(
                ChangeRecordTagTypeChooserState(
                    current = ChangeRecordTagTypeChooserState.State.Closed,
                    previous = current,
                ),
            )
        } else {
            chooserState.set(
                ChangeRecordTagTypeChooserState(
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

    private suspend fun initializePreviewViewData() {
        val extra = extra
        if (extra is ChangeTagData.Change) {
            recordTagInteractor.get(extra.id)?.let {
                newName = it.name
                iconSelectionViewModelDelegateImpl.newIcon = it.icon
                colorSelectionViewModelDelegateImpl.newColor = it.color
                newIconColorSource = it.iconColorSource
                iconSelectionViewModelDelegateImpl.update()
                colorSelectionViewModelDelegateImpl.update()
                updateIconColorSourceSelected()
            }
        }
    }

    private fun getColorSelectionDelegateParent(): ColorSelectionViewModelDelegate.Parent {
        return object : ColorSelectionViewModelDelegate.Parent {
            override suspend fun update() {
                updatePreview()
                updateIconColorSourceSelected()
            }

            override fun onColorSelected() {
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

    private suspend fun loadTypesViewData(): List<ViewHolderType> {
        return changeRecordTagViewDataInteractor.getTypesViewData(newTypeIds)
    }

    private fun showMessage(stringResId: Int) {
        snackBarMessageNavigationInteractor.showMessage(stringResId)
    }

    private fun showArchivedMessage(stringResId: Int) {
        snackBarMessageNavigationInteractor.showArchiveMessage(stringResId)
    }

    companion object {
        private const val TYPE_SELECTION_TAG = "types_selection_tag"
    }
}
