package com.example.util.simpletimetracker.feature_change_record_type.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.util.simpletimetracker.domain.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.feature_change_record_type.mapper.ChangeRecordTypeViewDataMapper
import com.example.util.simpletimetracker.feature_change_record_type.viewData.ChangeRecordTypeViewData
import com.example.util.simpletimetracker.navigation.Router
import kotlinx.coroutines.launch
import javax.inject.Inject

class ChangeRecordTypeViewModel(private val name: String) : ViewModel() {

    @Inject
    lateinit var router: Router
    @Inject
    lateinit var recordTypeInteractor: RecordTypeInteractor
    @Inject
    lateinit var changeRecordTypeViewDataMapper: ChangeRecordTypeViewDataMapper

    private var newName: String = ""

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
            recordTypeInteractor.getAll()
                .firstOrNull { it.name == name }
                ?.copy(name = newName)
                ?.let {
                    recordTypeInteractor.add(it)
                    router.back()
                }
        }
    }

    private suspend fun updateRecordType() {
        recordTypeLiveData.value = loadRecordTypeViewData()
    }

    private suspend fun loadRecordTypeViewData(): ChangeRecordTypeViewData {
        return recordTypeInteractor
            .getAll()
            .firstOrNull {
                it.name == name
            }
            ?.let(changeRecordTypeViewDataMapper::map)
            ?: ChangeRecordTypeViewData(
                name = ""
            )
    }
}
