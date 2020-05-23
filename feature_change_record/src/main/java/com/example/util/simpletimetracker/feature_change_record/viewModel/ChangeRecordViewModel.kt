package com.example.util.simpletimetracker.feature_change_record.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.util.simpletimetracker.core.adapter.ViewHolderType
import com.example.util.simpletimetracker.core.mapper.TimeMapper
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.extension.flip
import com.example.util.simpletimetracker.domain.extension.orTrue
import com.example.util.simpletimetracker.domain.extension.orZero
import com.example.util.simpletimetracker.domain.interactor.RecordInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.domain.model.Record
import com.example.util.simpletimetracker.feature_change_record.R
import com.example.util.simpletimetracker.feature_change_record.extra.ChangeRecordExtra
import com.example.util.simpletimetracker.feature_change_record.mapper.ChangeRecordTypeViewDataMapper
import com.example.util.simpletimetracker.feature_change_record.mapper.ChangeRecordViewDataMapper
import com.example.util.simpletimetracker.feature_change_record.viewData.ChangeRecordTypeViewData
import com.example.util.simpletimetracker.feature_change_record.viewData.ChangeRecordViewData
import com.example.util.simpletimetracker.navigation.Router
import com.example.util.simpletimetracker.navigation.Screen
import com.example.util.simpletimetracker.navigation.params.DateTimeDialogParams
import kotlinx.coroutines.launch
import javax.inject.Inject

class ChangeRecordViewModel @Inject constructor(
    private var router: Router,
    private var recordInteractor: RecordInteractor,
    private var recordTypeInteractor: RecordTypeInteractor,
    private var timeMapper: TimeMapper,
    private var changeRecordViewDataMapper: ChangeRecordViewDataMapper,
    private var changeRecordTypeViewDataMapper: ChangeRecordTypeViewDataMapper,
    private var resourceRepo: ResourceRepo
) : ViewModel() {

    lateinit var extra: ChangeRecordExtra

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
    val saveButtonEnabled: LiveData<Boolean> = MutableLiveData(true)
    val deleteButtonEnabled: LiveData<Boolean> = MutableLiveData(true)
    val deleteIconVisibility: LiveData<Boolean> get() = MutableLiveData(extra.id != 0L)

    private var newTypeId: Long = 0
    private var newTimeEnded: Long = 0
    private var newTimeStarted: Long = 0

    fun onTypeChooserClick() {
        (flipTypesChooser as MutableLiveData).value = flipTypesChooser.value
            ?.flip().orTrue()
    }

    fun onTimeStartedClick() {
        router.navigate(
            Screen.DATE_TIME_DIALOG,
            DateTimeDialogParams(
                tag = TIME_STARTED_TAG,
                timestamp = newTimeStarted
            )
        )
    }

    fun onTimeEndedClick() {
        router.navigate(
            Screen.DATE_TIME_DIALOG,
            DateTimeDialogParams(
                tag = TIME_ENDED_TAG,
                timestamp = newTimeEnded
            )
        )
    }

    fun onDeleteClick() {
        (deleteButtonEnabled as MutableLiveData).value = false
        viewModelScope.launch {
            if (extra.id != 0L) {
                recordInteractor.remove(extra.id)
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
                id = extra.id,
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

    fun onDateTimeSet(timestamp: Long, tag: String?) {
        viewModelScope.launch {
            when (tag) {
                TIME_STARTED_TAG -> {
                    if (timestamp != newTimeStarted) {
                        newTimeStarted = timestamp
                        updatePreview()
                    }
                }
                TIME_ENDED_TAG -> {
                    if (timestamp != newTimeEnded) {
                        newTimeEnded = timestamp
                        updatePreview()
                    }
                }
            }
        }
    }

    private fun getInitialDate(): Long {
        return timeMapper.toTimestampShifted(extra.daysFromToday.orZero())
    }

    private suspend fun updatePreview() {
        (record as MutableLiveData).value = loadPreviewViewData()
    }

    private suspend fun initializePreviewViewData() {
        if (extra.id != 0L) {
            recordInteractor.get(extra.id)?.let { record ->
                newTypeId = record.typeId.orZero()
                newTimeStarted = record.timeStarted
                newTimeEnded = record.timeEnded
            }
        } else {
            newTimeEnded = getInitialDate()
            newTimeStarted = newTimeEnded - ONE_HOUR
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

    companion object {
        private const val TIME_STARTED_TAG = "time_started_tag"
        private const val TIME_ENDED_TAG = "time_ended_tag"

        private const val ONE_HOUR: Int = 60 * 60 * 1000
    }
}
