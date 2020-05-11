package com.example.util.simpletimetracker.feature_change_record_type.viewModel

import androidx.lifecycle.*
import com.example.util.simpletimetracker.core.adapter.ViewHolderType
import com.example.util.simpletimetracker.core.mapper.ColorMapper
import com.example.util.simpletimetracker.core.mapper.IconMapper
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.domain.model.RecordType
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
    val deleteIconVisibility: LiveData<Boolean> = MutableLiveData(id != 0L)

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

    fun onColorClick(item: ChangeRecordTypeColorViewData) {
        viewModelScope.launch {
            if (item.colorId != newColorId) {
                newColorId = item.colorId
                updateRecordType()
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
        viewModelScope.launch {
            if (id != 0L) recordTypeInteractor.remove(id)
            router.back()
        }
    }

    fun onSaveClick() {
        viewModelScope.launch {
            RecordType(
                id = id,
                name = newName,
                icon = newIconId,
                color = newColorId
            ).let {
                recordTypeInteractor.add(it)
                router.back()
            }
        }
    }

    private fun updateRecordType() {
        (recordType as MutableLiveData).value = loadRecordPreviewViewData()
    }

    private suspend fun loadRecordTypeViewData(): ChangeRecordTypeViewData {
        recordTypeInteractor
            .getAll()
            .firstOrNull { it.id == id }
            ?.let {
                newName = it.name
                newIconId = it.icon
                newColorId = it.color
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

    private fun loadIconsViewData(): List<ViewHolderType> {
        return IconMapper.availableIcons
            .mapIndexed { iconId, iconResId ->
                ChangeRecordTypeIconViewData(
                    iconId = iconId,
                    iconResId = iconResId
                )
            }
    }
}
