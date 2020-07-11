package com.example.util.simpletimetracker.feature_change_record_type.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.util.simpletimetracker.core.adapter.ViewHolderType
import com.example.util.simpletimetracker.core.mapper.ColorMapper
import com.example.util.simpletimetracker.core.mapper.IconMapper
import com.example.util.simpletimetracker.core.mapper.RecordTypeViewDataMapper
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.core.viewData.RecordTypeViewData
import com.example.util.simpletimetracker.domain.extension.flip
import com.example.util.simpletimetracker.domain.extension.orTrue
import com.example.util.simpletimetracker.domain.interactor.RecordInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.domain.interactor.RunningRecordInteractor
import com.example.util.simpletimetracker.domain.interactor.WidgetInteractor
import com.example.util.simpletimetracker.domain.model.RecordType
import com.example.util.simpletimetracker.feature_change_record_type.R
import com.example.util.simpletimetracker.feature_change_record_type.extra.ChangeRecordTypeExtra
import com.example.util.simpletimetracker.feature_change_record_type.viewData.ChangeRecordTypeColorViewData
import com.example.util.simpletimetracker.feature_change_record_type.viewData.ChangeRecordTypeIconViewData
import com.example.util.simpletimetracker.navigation.Router
import kotlinx.coroutines.launch
import javax.inject.Inject

class ChangeRecordTypeViewModel @Inject constructor(
    private val router: Router,
    private val recordTypeInteractor: RecordTypeInteractor,
    private val recordInteractor: RecordInteractor,
    private val runningRecordInteractor: RunningRecordInteractor,
    private val widgetInteractor: WidgetInteractor,
    private val recordTypeViewDataMapper: RecordTypeViewDataMapper,
    private val resourceRepo: ResourceRepo,
    private val colorMapper: ColorMapper,
    private val iconMapper: IconMapper
) : ViewModel() {

    lateinit var extra: ChangeRecordTypeExtra

    val recordType: LiveData<RecordTypeViewData> by lazy {
        return@lazy MutableLiveData<RecordTypeViewData>().let { initial ->
            viewModelScope.launch { initial.value = loadRecordTypeViewData() }
            initial
        }
    }
    val colors: LiveData<List<ViewHolderType>> by lazy {
        return@lazy MutableLiveData<List<ViewHolderType>>().let { initial ->
            viewModelScope.launch { initial.value = loadColorsViewData() }
            initial
        }
    }
    val icons: LiveData<List<ViewHolderType>> by lazy {
        return@lazy MutableLiveData<List<ViewHolderType>>().let { initial ->
            viewModelScope.launch { initial.value = loadIconsViewData() }
            initial
        }
    }
    val flipColorChooser: LiveData<Boolean> = MutableLiveData()
    val flipIconChooser: LiveData<Boolean> = MutableLiveData()
    val deleteButtonEnabled: LiveData<Boolean> = MutableLiveData(true)
    val saveButtonEnabled: LiveData<Boolean> = MutableLiveData(true)
    val deleteIconVisibility: LiveData<Boolean> get() = MutableLiveData(extra.id != 0L)
    val keyboardVisibility: LiveData<Boolean> get() = MutableLiveData(extra.id == 0L)

    private var newName: String = ""
    private var newIconName: String = ""
    private var newColorId: Int = (0..ColorMapper.colorsNumber).random()

    fun onNameChange(name: String) {
        viewModelScope.launch {
            if (name != newName) {
                newName = name
                updateRecordType()
            }
        }
    }

    fun onColorChooserClick() {
        (keyboardVisibility as MutableLiveData).value = false
        (flipColorChooser as MutableLiveData).value = flipColorChooser.value
            ?.flip().orTrue()

        if (flipIconChooser.value == true) {
            (flipIconChooser as MutableLiveData).value = false
        }
    }

    fun onIconChooserClick() {
        (keyboardVisibility as MutableLiveData).value = false
        (flipIconChooser as MutableLiveData).value = flipIconChooser.value
            ?.flip().orTrue()

        if (flipColorChooser.value == true) {
            (flipColorChooser as MutableLiveData).value = false
        }
    }

    fun onColorClick(item: ChangeRecordTypeColorViewData) {
        viewModelScope.launch {
            if (item.colorId != newColorId) {
                newColorId = item.colorId
                updateRecordType()
                updateIcons()
            }
        }
    }

    fun onIconClick(item: ChangeRecordTypeIconViewData) {
        viewModelScope.launch {
            if (item.iconName != newIconName) {
                newIconName = item.iconName
                updateRecordType()
            }
        }
    }

    fun onDeleteClick() {
        (deleteButtonEnabled as MutableLiveData).value = false
        viewModelScope.launch {
            if (extra.id != 0L) {
                recordTypeInteractor.remove(extra.id)
                runningRecordInteractor.get(extra.id)?.let { runningRecord ->
                    recordInteractor.add(
                        typeId = runningRecord.id,
                        timeStarted = runningRecord.timeStarted
                    )
                    runningRecordInteractor.remove(extra.id)
                }
                widgetInteractor.updateWidgets()
                resourceRepo.getString(R.string.change_record_type_removed)
                    .let(router::showSystemMessage)
                (keyboardVisibility as MutableLiveData).value = false
                router.back()
            }
        }
    }

    fun onSaveClick() {
        if (newName.isEmpty()) {
            resourceRepo.getString(R.string.change_record_message_choose_name)
                .let(router::showSystemMessage)
            return
        }
        (saveButtonEnabled as MutableLiveData).value = false
        viewModelScope.launch {
            RecordType(
                id = extra.id,
                name = newName,
                icon = newIconName,
                color = newColorId
            ).let {
                recordTypeInteractor.add(it)
                widgetInteractor.updateWidgets()
                (keyboardVisibility as MutableLiveData).value = false
                router.back()
            }
        }
    }

    private fun updateRecordType() {
        (recordType as MutableLiveData).value = loadRecordPreviewViewData()
    }

    private suspend fun loadRecordTypeViewData(): RecordTypeViewData {
        recordTypeInteractor.get(extra.id)
            ?.let {
                newName = it.name
                newIconName = it.icon
                newColorId = it.color
                updateIcons()
            }
        return RecordType(
            name = newName,
            icon = newIconName,
            color = newColorId
        ).let(recordTypeViewDataMapper::map)
    }

    private fun loadRecordPreviewViewData(): RecordTypeViewData {
        return RecordType(
            name = newName,
            icon = newIconName,
            color = newColorId
        ).let(recordTypeViewDataMapper::map)
    }

    private fun loadColorsViewData(): List<ViewHolderType> {
        return ColorMapper.availableColors
            .mapIndexed { colorId, colorResId ->
                colorId to resourceRepo.getColor(colorResId)
            }
            .map { (colorId, colorInt) ->
                ChangeRecordTypeColorViewData(
                    colorId = colorId,
                    colorInt = colorInt
                )
            }
    }

    private fun updateIcons() {
        (icons as MutableLiveData).value = loadIconsViewData()
    }

    private fun loadIconsViewData(): List<ViewHolderType> {
        return iconMapper.availableIconsNames
            .map { (iconName, iconResId) ->
                ChangeRecordTypeIconViewData(
                    iconName = iconName,
                    iconResId = iconResId,
                    colorInt = newColorId
                        .let(colorMapper::mapToColorResId)
                        .let(resourceRepo::getColor)
                )
            }
    }
}
