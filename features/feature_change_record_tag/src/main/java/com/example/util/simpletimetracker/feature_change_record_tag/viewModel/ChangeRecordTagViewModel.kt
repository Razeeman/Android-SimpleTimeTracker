package com.example.util.simpletimetracker.feature_change_record_tag.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.util.simpletimetracker.core.extension.set
import com.example.util.simpletimetracker.core.interactor.ColorViewDataInteractor
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.category.CategoryViewData
import com.example.util.simpletimetracker.core.interactor.NotificationTypeInteractor
import com.example.util.simpletimetracker.core.interactor.RecordTypesViewDataInteractor
import com.example.util.simpletimetracker.core.mapper.CategoryViewDataMapper
import com.example.util.simpletimetracker.core.mapper.ColorMapper
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.core.view.buttonsRowView.ButtonsRowViewData
import com.example.util.simpletimetracker.feature_base_adapter.recordType.RecordTypeViewData
import com.example.util.simpletimetracker.domain.extension.flip
import com.example.util.simpletimetracker.domain.extension.orTrue
import com.example.util.simpletimetracker.domain.extension.orZero
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTagInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.domain.model.AppColor
import com.example.util.simpletimetracker.domain.model.RecordTag
import com.example.util.simpletimetracker.feature_base_adapter.color.ColorViewData
import com.example.util.simpletimetracker.feature_change_record_tag.R
import com.example.util.simpletimetracker.feature_change_record_tag.mapper.ChangeRecordTagMapper
import com.example.util.simpletimetracker.feature_change_record_tag.viewData.ChangeRecordTagTypeSetupViewData
import com.example.util.simpletimetracker.feature_change_record_tag.viewData.ChangeRecordTagTypeSwitchViewData
import com.example.util.simpletimetracker.feature_change_record_tag.viewData.RecordTagType
import com.example.util.simpletimetracker.navigation.Router
import com.example.util.simpletimetracker.navigation.params.screen.ChangeTagData
import com.example.util.simpletimetracker.navigation.params.notification.ToastParams
import com.example.util.simpletimetracker.navigation.params.screen.ColorSelectionDialogParams
import kotlinx.coroutines.launch
import javax.inject.Inject

class ChangeRecordTagViewModel @Inject constructor(
    private val router: Router,
    private val recordTypesViewDataInteractor: RecordTypesViewDataInteractor,
    private val recordTagInteractor: RecordTagInteractor,
    private val recordTypeInteractor: RecordTypeInteractor,
    private val colorViewDataInteractor: ColorViewDataInteractor,
    private val prefsInteractor: PrefsInteractor,
    private val notificationTypeInteractor: NotificationTypeInteractor,
    private val categoryViewDataMapper: CategoryViewDataMapper,
    private val changeRecordTagMapper: ChangeRecordTagMapper,
    private val resourceRepo: ResourceRepo,
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
    val tagTypeViewData: LiveData<List<ViewHolderType>> by lazy {
        return@lazy MutableLiveData(loadTagTypeViewData())
    }
    val tagTypeSetupViewData: LiveData<ChangeRecordTagTypeSetupViewData> by lazy {
        return@lazy MutableLiveData()
    }
    val colors: LiveData<List<ViewHolderType>> by lazy {
        return@lazy MutableLiveData<List<ViewHolderType>>().let { initial ->
            viewModelScope.launch { initial.value = loadColorsViewData() }
            initial
        }
    }
    val types: LiveData<List<ViewHolderType>> by lazy {
        return@lazy MutableLiveData<List<ViewHolderType>>().let { initial ->
            viewModelScope.launch { initial.value = loadTypesViewData() }
            initial
        }
    }
    val flipColorChooser: LiveData<Boolean> = MutableLiveData()
    val flipTypesChooser: LiveData<Boolean> = MutableLiveData()
    val deleteButtonEnabled: LiveData<Boolean> = MutableLiveData(true)
    val saveButtonEnabled: LiveData<Boolean> = MutableLiveData(true)
    val deleteIconVisibility: LiveData<Boolean> by lazy { MutableLiveData(recordTagId != 0L) }
    val keyboardVisibility: LiveData<Boolean> by lazy { MutableLiveData(recordTagId == 0L) }
    val typeSelectionVisibility: LiveData<Boolean> by lazy { MutableLiveData(recordTagId == 0L) }

    private val recordTagId: Long get() = (extra as? ChangeTagData.Change)?.id.orZero()
    private var tagType: RecordTagType = RecordTagType.GENERAL
    private var newName: String = ""
    private var newColor: AppColor = AppColor(colorId = (0..ColorMapper.colorsNumber).random(), colorInt = "")
    private var newTypeId: Long = 0L

    fun onNameChange(name: String) {
        viewModelScope.launch {
            if (name != newName) {
                newName = name
                updatePreview()
            }
        }
    }

    fun onTagTypeClick(viewData: ButtonsRowViewData) {
        if (viewData !is ChangeRecordTagTypeSwitchViewData) return
        viewModelScope.launch {
            tagType = viewData.tagType
            updateTagTypeViewData()
            updateTagTypeSetupViewData()
            // TODO
            if (flipTypesChooser.value == true) {
                (flipTypesChooser as MutableLiveData).value = false
            }
            if (flipColorChooser.value == true) {
                (flipColorChooser as MutableLiveData).value = false
            }
        }
    }

    fun onColorChooserClick() {
        (keyboardVisibility as MutableLiveData).value = false
        (flipColorChooser as MutableLiveData).value = flipColorChooser.value
            ?.flip().orTrue()
    }

    fun onTypeChooserClick() {
        (keyboardVisibility as MutableLiveData).value = false
        (flipTypesChooser as MutableLiveData).value = flipTypesChooser.value
            ?.flip().orTrue()
    }

    fun onColorClick(item: ColorViewData) {
        viewModelScope.launch {
            if (newTypeId != 0L || item.colorId != newColor.colorId || newColor.colorInt.isNotEmpty()) {
                newColor = AppColor(colorId = item.colorId, colorInt = "")
                newTypeId = 0
                updatePreview()
                updateColors()
            }
        }
    }

    fun onColorPaletteClick() {
        ColorSelectionDialogParams(
            preselectedColor = colorMapper.mapToColorInt(
                color = newColor,
                isDarkTheme = false // Pass original, not darkened color.
            )
        ).let(router::navigate)
    }

    fun onCustomColorSelected(colorInt: Int) {
        viewModelScope.launch {
            if (newTypeId != 0L || colorInt.toString() != newColor.colorInt) {
                newColor = AppColor(colorId = 0, colorInt = colorInt.toString())
                newTypeId = 0
                updatePreview()
                updateColors()
            }
        }
    }

    fun onTypeClick(item: RecordTypeViewData) {
        viewModelScope.launch {
            if (item.id != newTypeId) {
                newTypeId = item.id
                updatePreview()
                updateColors()
            }
        }
    }

    fun onDeleteClick() {
        (deleteButtonEnabled as MutableLiveData).value = false
        viewModelScope.launch {
            if (recordTagId != 0L) {
                recordTagInteractor.archive(recordTagId)
                showMessage(R.string.change_record_tag_archived)
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
                typeId = newTypeId,
                name = newName,
                color = newColor,
            ).let {
                recordTagInteractor.add(it)
                notificationTypeInteractor.checkAndShow(newTypeId)
                (keyboardVisibility as MutableLiveData).value = false
                router.back()
            }
        }
    }

    private suspend fun initializePreviewViewData() {
        when (extra) {
            is ChangeTagData.Change -> {
                recordTagInteractor.get(recordTagId)?.let {
                    newTypeId = it.typeId
                    newName = it.name
                    newColor = it.color
                    updateColors()
                }
            }
            is ChangeTagData.New -> {
                val preselectedTypeId: Long? = (extra as? ChangeTagData.New)?.preselectedTypeId
                newTypeId = preselectedTypeId.orZero()
                if (preselectedTypeId != null) tagType = RecordTagType.TYPED
            }
        }
        updateTagTypeSetupViewData()
    }

    private fun updatePreview() = viewModelScope.launch {
        (preview as MutableLiveData).value = loadPreviewViewData()
    }

    private suspend fun loadPreviewViewData(): CategoryViewData.Record {
        val tag = RecordTag(
            name = newName,
            color = newColor,
            typeId = newTypeId
        )
        val type = recordTypeInteractor.get(newTypeId)
        val isDarkTheme = prefsInteractor.getDarkMode()

        return categoryViewDataMapper.mapRecordTag(
            tag = tag,
            type = type,
            isDarkTheme = isDarkTheme
        )
    }

    private fun updateTagTypeViewData() {
        tagTypeViewData.set(loadTagTypeViewData())
    }

    private fun loadTagTypeViewData(): List<ViewHolderType> {
        return changeRecordTagMapper.mapToTagTypeSwitchViewData(tagType)
    }

    private fun updateTagTypeSetupViewData() {
        tagTypeSetupViewData.set(loadTagTypeSetupViewData())
    }

    private fun loadTagTypeSetupViewData(): ChangeRecordTagTypeSetupViewData {
        return changeRecordTagMapper.mapToTagTypeSetupViewData(
            recordTagId = recordTagId,
            typeId = newTypeId,
            tagType = tagType
        )
    }

    private fun updateColors() = viewModelScope.launch {
        val data = loadColorsViewData()
        colors.set(data)
    }

    private suspend fun loadColorsViewData(): List<ViewHolderType> {
        val selectedColor = if (newTypeId != 0L) {
            AppColor(colorId = -1, colorInt = "") // Nonexistent color.
        } else {
            newColor
        }

        return colorViewDataInteractor.getColorsViewData(selectedColor)
    }

    private suspend fun loadTypesViewData(): List<ViewHolderType> {
        return recordTypesViewDataInteractor.getTypesViewData()
    }

    private fun showMessage(stringResId: Int) {
        val params = ToastParams(message = resourceRepo.getString(stringResId))
        router.show(params)
    }
}
