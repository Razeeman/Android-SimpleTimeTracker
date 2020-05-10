package com.example.util.simpletimetracker.feature_change_record_type.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

    private var newName: String = "Name"
    private var newIconId: Int = 0
    private var newColorId: Int = (0..ColorMapper.colorsNumber).random()

    private val recordTypeLiveData: MutableLiveData<ChangeRecordTypeViewData> by lazy {
        return@lazy MutableLiveData<ChangeRecordTypeViewData>().let { initial ->
            viewModelScope.launch { initial.value = loadRecordTypeViewData() }
            initial
        }
    }
    private val colorsLiveData: MutableLiveData<List<ViewHolderType>> by lazy {
        return@lazy MutableLiveData<List<ViewHolderType>>().let { initial ->
            viewModelScope.launch { initial.value = loadColorsViewData() }
            initial
        }
    }
    private val iconsLiveData: MutableLiveData<List<ViewHolderType>> by lazy {
        return@lazy MutableLiveData<List<ViewHolderType>>().let { initial ->
            viewModelScope.launch { initial.value = loadIconsViewData() }
            initial
        }
    }

    val recordType: LiveData<ChangeRecordTypeViewData>
        get() = recordTypeLiveData
    val colors: LiveData<List<ViewHolderType>>
        get() = colorsLiveData
    val icons: LiveData<List<ViewHolderType>>
        get() = iconsLiveData

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
        recordTypeLiveData.value = loadRecordPreviewViewData()
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
