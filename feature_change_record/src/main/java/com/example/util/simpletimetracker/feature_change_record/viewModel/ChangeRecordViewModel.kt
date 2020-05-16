package com.example.util.simpletimetracker.feature_change_record.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.util.simpletimetracker.core.adapter.ViewHolderType
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.extension.flip
import com.example.util.simpletimetracker.domain.extension.orTrue
import com.example.util.simpletimetracker.domain.extension.orZero
import com.example.util.simpletimetracker.domain.interactor.RecordInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.domain.model.Record
import com.example.util.simpletimetracker.feature_change_record.R
import com.example.util.simpletimetracker.feature_change_record.mapper.ChangeRecordTypeViewDataMapper
import com.example.util.simpletimetracker.feature_change_record.mapper.ChangeRecordViewDataMapper
import com.example.util.simpletimetracker.feature_change_record.viewData.ChangeRecordTypeViewData
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
    lateinit var changeRecordTypeViewDataMapper: ChangeRecordTypeViewDataMapper
    @Inject
    lateinit var resourceRepo: ResourceRepo

    val record: LiveData<ChangeRecordViewData> by lazy {
        return@lazy MutableLiveData<ChangeRecordViewData>().let { initial ->
            viewModelScope.launch {
                initializePreviewViewData()
                initial.value = loadPreviewViewData()
            }
            initial
        }
    }
    val types: LiveData<List<ViewHolderType>> by lazy {
        return@lazy MutableLiveData<List<ViewHolderType>>().let { initial ->
            viewModelScope.launch { initial.value = loadTypesViewData() }
            initial
        }
    }
    val flipTypesChooser: LiveData<Boolean> = MutableLiveData()
    val deleteIconVisibility: LiveData<Boolean> = MutableLiveData(id != 0L)
    val saveButtonEnabled: LiveData<Boolean> = MutableLiveData(true)

    private var newTypeId: Long = 0
    private var newTimeStarted: Long = System.currentTimeMillis() - 1000 * 60 * 60
    private var newTimeEnded: Long = System.currentTimeMillis()

    fun onTypeChooserClick() {
        (flipTypesChooser as MutableLiveData).value = flipTypesChooser.value
            ?.flip().orTrue()
    }

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
        if (newTypeId == 0L) {
            resourceRepo.getString(R.string.message_choose_type)
                .let(router::showSystemMessage)
            return
        }
        (saveButtonEnabled as MutableLiveData).value = false
        viewModelScope.launch {
            Record(
                id = id,
                typeId = newTypeId,
                timeStarted = newTimeStarted,
                timeEnded = newTimeEnded
            ).let {
                recordInteractor.add(it)
                router.back()
            }
        }
    }

    fun onTypeClick(item: ChangeRecordTypeViewData) {
        viewModelScope.launch {
            if (item.id != newTypeId) {
                newTypeId = item.id
                updatePreview()
            }
        }
    }

    private suspend fun updatePreview() {
        (record as MutableLiveData).value = loadPreviewViewData()
    }

    private suspend fun initializePreviewViewData() {
        if (id != 0L) {
            recordInteractor.get(id)?.let { record ->
                newTypeId = record.typeId.orZero()
                newTimeStarted = record.timeStarted
                newTimeEnded = record.timeEnded
            }
        }
    }

    private suspend fun loadPreviewViewData(): ChangeRecordViewData {
        val record = Record(
            typeId = newTypeId,
            timeStarted = newTimeStarted,
            timeEnded = newTimeEnded
        )
        val type = recordTypeInteractor.get(newTypeId)
        return changeRecordViewDataMapper.map(record, type)
    }

    private suspend fun loadTypesViewData(): List<ViewHolderType> {
        return recordTypeInteractor.getAll()
            .filter { !it.hidden }
            .map(changeRecordTypeViewDataMapper::map)
    }
}
