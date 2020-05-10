package com.example.util.simpletimetracker.feature_change_record_type.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.util.simpletimetracker.core.mapper.ColorMapper
import com.example.util.simpletimetracker.domain.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.domain.model.RecordType
import com.example.util.simpletimetracker.feature_change_record_type.mapper.ChangeRecordTypeViewDataMapper
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

    private var newName: String = "Name"
    private var newIconId: Int = 0
    private var newColorId: Int = (0..ColorMapper.colorsNumber).random()

    private val recordTypeLiveData: MutableLiveData<ChangeRecordTypeViewData> by lazy {
        return@lazy MutableLiveData<ChangeRecordTypeViewData>().let { initial ->
            viewModelScope.launch { initial.value = loadRecordTypeViewData() }
            initial
        }
    }

    val recordType: LiveData<ChangeRecordTypeViewData>
        get() = recordTypeLiveData

    fun onNameChange(name: String) {
        newName = name
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

    private suspend fun updateRecordType() {
        recordTypeLiveData.value = loadRecordTypeViewData()
    }

    private suspend fun loadRecordTypeViewData(): ChangeRecordTypeViewData {
        return (recordTypeInteractor
            .getAll()
            .firstOrNull { it.id == id }
            ?: RecordType(
                name = newName,
                icon = newIconId,
                color = newColorId
            ))
            .let(changeRecordTypeViewDataMapper::map)
    }
}
