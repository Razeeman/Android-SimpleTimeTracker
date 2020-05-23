package com.example.util.simpletimetracker.feature_change_record_type.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.util.simpletimetracker.core.adapter.ViewHolderType
import com.example.util.simpletimetracker.core.mapper.ColorMapper
import com.example.util.simpletimetracker.core.mapper.IconMapper
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.extension.flip
import com.example.util.simpletimetracker.domain.extension.orTrue
import com.example.util.simpletimetracker.domain.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.domain.model.RecordType
import com.example.util.simpletimetracker.feature_change_record_type.R
import com.example.util.simpletimetracker.feature_change_record_type.mapper.ChangeRecordTypeViewDataMapper
import com.example.util.simpletimetracker.feature_change_record_type.viewData.ChangeRecordTypeColorViewData
import com.example.util.simpletimetracker.feature_change_record_type.viewData.ChangeRecordTypeIconViewData
import com.example.util.simpletimetracker.feature_change_record_type.viewData.ChangeRecordTypeViewData
import com.example.util.simpletimetracker.navigation.Router
import kotlinx.coroutines.launch
import javax.inject.Inject

class ChangeRecordTypeViewModel(
    private val id: Long
) : ViewModel() {

    @Inject
    lateinit var router: Router
    @Inject
    lateinit var recordTypeInteractor: RecordTypeInteractor
    @Inject
    lateinit var changeRecordTypeViewDataMapper: ChangeRecordTypeViewDataMapper
    @Inject
    lateinit var resourceRepo: ResourceRepo
    @Inject
    lateinit var colorMapper: ColorMapper

    val recordType: LiveData<ChangeRecordTypeViewData> by lazy {
        return@lazy MutableLiveData<ChangeRecordTypeViewData>().let { initial ->
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
    val deleteIconVisibility: LiveData<Boolean> = MutableLiveData(id != 0L)
    val deleteButtonEnabled: LiveData<Boolean> = MutableLiveData(true)
    val saveButtonEnabled: LiveData<Boolean> = MutableLiveData(true)
    val keyboardVisibility: LiveData<Boolean> = MutableLiveData(id == 0L)

    private var newName: String = ""
    private var newIconId: Int = 0
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
            if (item.iconId != newIconId) {
                newIconId = item.iconId
                updateRecordType()
            }
        }
    }

    fun onDeleteClick() {
        (deleteButtonEnabled as MutableLiveData).value = false
        viewModelScope.launch {
            if (id != 0L) {
                recordTypeInteractor.remove(id)
                resourceRepo.getString(R.string.record_type_removed)
                    .let(router::showSystemMessage)
                (keyboardVisibility as MutableLiveData).value = false
                router.back()
            }
        }
    }

    fun onSaveClick() {
        (saveButtonEnabled as MutableLiveData).value = false
        viewModelScope.launch {
            RecordType(
                id = id,
                name = newName,
                icon = newIconId,
                color = newColorId
            ).let {
                recordTypeInteractor.add(it)
                (keyboardVisibility as MutableLiveData).value = false
                router.back()
            }
        }
    }

    private fun updateRecordType() {
        (recordType as MutableLiveData).value = loadRecordPreviewViewData()
    }

    private suspend fun loadRecordTypeViewData(): ChangeRecordTypeViewData {
        recordTypeInteractor.get(id)
            ?.let {
                newName = it.name
                newIconId = it.icon
                newColorId = it.color
                updateIcons()
            }
        return RecordType(
            name = newName,
            icon = newIconId,
            color = newColorId
        ).let(changeRecordTypeViewDataMapper::map)
    }

    private fun loadRecordPreviewViewData(): ChangeRecordTypeViewData {
        return RecordType(
            name = newName,
            icon = newIconId,
            color = newColorId
        ).let(changeRecordTypeViewDataMapper::map)
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
        return IconMapper.availableIcons
            .mapIndexed { iconId, iconResId ->
                ChangeRecordTypeIconViewData(
                    iconId = iconId,
                    iconResId = iconResId,
                    colorInt = newColorId
                        .let(colorMapper::mapToColorResId)
                        .let(resourceRepo::getColor)
                )
            }
    }
}
