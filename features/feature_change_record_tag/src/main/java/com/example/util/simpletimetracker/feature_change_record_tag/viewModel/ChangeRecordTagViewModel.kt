package com.example.util.simpletimetracker.feature_change_record_tag.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.util.simpletimetracker.core.extension.set
import com.example.util.simpletimetracker.core.interactor.ColorViewDataInteractor
import com.example.util.simpletimetracker.core.interactor.SnackBarMessageNavigationInteractor
import com.example.util.simpletimetracker.core.mapper.CategoryViewDataMapper
import com.example.util.simpletimetracker.core.mapper.ColorMapper
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
import com.example.util.simpletimetracker.feature_base_adapter.color.ColorViewData
import com.example.util.simpletimetracker.feature_base_adapter.recordType.RecordTypeViewData
import com.example.util.simpletimetracker.feature_change_record_tag.R
import com.example.util.simpletimetracker.feature_change_record_tag.interactor.ChangeRecordTagViewDataInteractor
import com.example.util.simpletimetracker.feature_change_record_tag.viewData.ChangeRecordTagTypeChooserState
import com.example.util.simpletimetracker.navigation.Router
import com.example.util.simpletimetracker.navigation.params.screen.ChangeTagData
import com.example.util.simpletimetracker.navigation.params.screen.ColorSelectionDialogParams
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChangeRecordTagViewModel @Inject constructor(
    private val router: Router,
    private val changeRecordTagViewDataInteractor: ChangeRecordTagViewDataInteractor,
    private val recordTagInteractor: RecordTagInteractor,
    private val recordTypeInteractor: RecordTypeInteractor,
    private val recordTypeToTagInteractor: RecordTypeToTagInteractor,
    private val colorViewDataInteractor: ColorViewDataInteractor,
    private val prefsInteractor: PrefsInteractor,
    private val notificationTypeInteractor: NotificationTypeInteractor,
    private val wearInteractor: WearInteractor,
    private val categoryViewDataMapper: CategoryViewDataMapper,
    private val snackBarMessageNavigationInteractor: SnackBarMessageNavigationInteractor,
    private val colorMapper: ColorMapper,
) : ViewModel() {

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
    val colors: LiveData<List<ViewHolderType>> by lazy {
        return@lazy MutableLiveData<List<ViewHolderType>>().let { initial ->
            viewModelScope.launch { initial.value = loadColorsViewData() }
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
    val deleteIconVisibility: LiveData<Boolean> by lazy { MutableLiveData(recordTagId != 0L) }
    val keyboardVisibility: LiveData<Boolean> by lazy { MutableLiveData(recordTagId == 0L) }

    private val recordTagId: Long get() = (extra as? ChangeTagData.Change)?.id.orZero()
    private var newName: String = ""
    private var newIcon: String = ""
    private var newColor: AppColor = AppColor(colorId = (0..ColorMapper.colorsNumber).random(), colorInt = "")
    private var newIconColorSource: Long = 0L // TODO TAGS add type selection to take color and icon from.
    private var newTypeIds: Set<Long> = emptySet()
    private var initialTypeIds: Set<Long> = emptySet()

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

    fun onTypeChooserClick() {
        onNewChooserState(ChangeRecordTagTypeChooserState.State.Type)
    }

    fun onColorClick(item: ColorViewData) {
        viewModelScope.launch {
            if (item.colorId != newColor.colorId || newColor.colorInt.isNotEmpty()) {
                newColor = AppColor(colorId = item.colorId, colorInt = "")
                updatePreview()
                updateColors()
            }
        }
    }

    fun onColorPaletteClick() {
        ColorSelectionDialogParams(
            preselectedColor = colorMapper.mapToColorInt(
                color = newColor,
                isDarkTheme = false, // Pass original, not darkened color.
            ),
        ).let(router::navigate)
    }

    fun onCustomColorSelected(colorInt: Int) {
        viewModelScope.launch {
            if (colorInt.toString() != newColor.colorInt) {
                newColor = AppColor(colorId = 0, colorInt = colorInt.toString())
                updatePreview()
                updateColors()
            }
        }
    }

    fun onTypeClick(item: RecordTypeViewData) {
        viewModelScope.launch {
            newTypeIds = newTypeIds.toMutableSet().apply {
                if (item.id in this) remove(item.id) else add(item.id)
            }
            updateTypesViewData()
        }
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
                icon = newIcon,
                color = newColor,
                iconColorSource = newIconColorSource, // TODO TAGS save icon and color if != 0
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
                newIcon = it.icon
                newColor = it.color
                newIconColorSource = it.iconColorSource
                updateColors()
            }
        }
    }

    private fun updatePreview() = viewModelScope.launch {
        preview.set(loadPreviewViewData())
    }

    private suspend fun loadPreviewViewData(): CategoryViewData.Record {
        val tag = RecordTag(
            name = newName,
            icon = newIcon,
            color = newColor,
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

    private fun updateColors() = viewModelScope.launch {
        val data = loadColorsViewData()
        colors.set(data)
    }

    private suspend fun loadColorsViewData(): List<ViewHolderType> {
        return colorViewDataInteractor.getColorsViewData(newColor)
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
}
