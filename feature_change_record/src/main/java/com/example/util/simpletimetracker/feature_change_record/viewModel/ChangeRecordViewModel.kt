package com.example.util.simpletimetracker.feature_change_record.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.interactor.RecordInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.feature_change_record.R
import com.example.util.simpletimetracker.feature_change_record.mapper.ChangeRecordViewDataMapper
import com.example.util.simpletimetracker.feature_change_record.viewData.ChangeRecordViewData
import com.example.util.simpletimetracker.navigation.Router
import kotlinx.coroutines.launch
import javax.inject.Inject

class ChangeRecordViewModel(
    private val id: Long
) : ViewModel() {

    @Inject
    lateinit var router: Router
    @Inject
    lateinit var recordInteractor: RecordInteractor
    @Inject
    lateinit var recordTypeInteractor: RecordTypeInteractor
    @Inject
    lateinit var changeRecordViewDataMapper: ChangeRecordViewDataMapper
    @Inject
    lateinit var resourceRepo: ResourceRepo

    val record: LiveData<ChangeRecordViewData> by lazy {
        return@lazy MutableLiveData<ChangeRecordViewData>().let { initial ->
            viewModelScope.launch { initial.value = loadRecordViewData() }
            initial
        }
    }
    val deleteIconVisibility: LiveData<Boolean> = MutableLiveData(id != 0L)

    fun onDeleteClick() {
        viewModelScope.launch {
            if (id != 0L) {
                recordInteractor.remove(id)
                resourceRepo.getString(R.string.record_removed)
                    .let(router::showSystemMessage)
                router.back()
            }
        }
    }

    fun onSaveClick() {
        // TODO
        router.back()
    }

    private suspend fun loadRecordViewData(): ChangeRecordViewData {
        if (id != 0L) {
            recordInteractor.get(id)?.let { record ->
                recordTypeInteractor.get(record.typeId)?.let { recordType ->
                    return changeRecordViewDataMapper.map(record, recordType)
                }
            }
        }
        return changeRecordViewDataMapper.mapToEmpty()
    }
}
