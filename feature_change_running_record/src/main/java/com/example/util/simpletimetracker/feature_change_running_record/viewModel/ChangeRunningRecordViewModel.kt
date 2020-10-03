package com.example.util.simpletimetracker.feature_change_running_record.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.util.simpletimetracker.core.adapter.ViewHolderType
import com.example.util.simpletimetracker.core.interactor.WidgetInteractor
import com.example.util.simpletimetracker.core.mapper.RecordTypeViewDataMapper
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.core.viewData.RecordTypeViewData
import com.example.util.simpletimetracker.domain.extension.flip
import com.example.util.simpletimetracker.domain.extension.orTrue
import com.example.util.simpletimetracker.domain.extension.orZero
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.domain.interactor.RunningRecordInteractor
import com.example.util.simpletimetracker.domain.model.RunningRecord
import com.example.util.simpletimetracker.feature_change_running_record.R
import com.example.util.simpletimetracker.feature_change_running_record.extra.ChangeRunningRecordExtra
import com.example.util.simpletimetracker.feature_change_running_record.mapper.ChangeRunningRecordViewDataMapper
import com.example.util.simpletimetracker.feature_change_running_record.viewData.ChangeRunningRecordViewData
import com.example.util.simpletimetracker.navigation.Router
import com.example.util.simpletimetracker.navigation.Screen
import com.example.util.simpletimetracker.navigation.params.DateTimeDialogParams
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

class ChangeRunningRecordViewModel @Inject constructor(
    private val router: Router,
    private val runningRecordInteractor: RunningRecordInteractor,
    private val recordTypeInteractor: RecordTypeInteractor,
    private val widgetInteractor: WidgetInteractor,
    private val changeRunningRecordViewDataMapper: ChangeRunningRecordViewDataMapper,
    private val recordTypeViewDataMapper: RecordTypeViewDataMapper,
    private val resourceRepo: ResourceRepo,
    private val prefsInteractor: PrefsInteractor
) : ViewModel() {

    lateinit var extra: ChangeRunningRecordExtra

    val record: LiveData<ChangeRunningRecordViewData> by lazy {
        return@lazy MutableLiveData<ChangeRunningRecordViewData>().let { initial ->
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
    val deleteButtonEnabled: LiveData<Boolean> = MutableLiveData(true)
    val saveButtonEnabled: LiveData<Boolean> = MutableLiveData(true)

    private var newTypeId: Long = 0
    private var newTimeStarted: Long = 0
    private var timerJob: Job? = null

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

    fun onDeleteClick() {
        (deleteButtonEnabled as MutableLiveData).value = false
        viewModelScope.launch {
            runningRecordInteractor.remove(extra.id)
            widgetInteractor.updateWidgets()
            resourceRepo.getString(R.string.change_running_record_removed)
                .let(router::showSystemMessage)
            router.back()
        }
    }

    fun onSaveClick() {
        if (newTypeId == 0L) {
            resourceRepo.getString(R.string.change_running_record_message_choose_type)
                .let(router::showSystemMessage)
            return
        }
        (saveButtonEnabled as MutableLiveData).value = false
        viewModelScope.launch {
            runningRecordInteractor.remove(extra.id)
            runningRecordInteractor.add(newTypeId, newTimeStarted)
            widgetInteractor.updateWidgets()
            router.back()
        }
    }

    fun onTypeClick(item: RecordTypeViewData) {
        viewModelScope.launch {
            if (item.id != newTypeId) {
                newTypeId = item.id
                updatePreview()
            }
        }
    }

    fun onDateTimeSet(timestamp: Long, tag: String?) {
        // TODO block selection on chooser or show message if timestamp in the future?
        val currentTime = System.currentTimeMillis()
        viewModelScope.launch {
            when (tag) {
                TIME_STARTED_TAG -> {
                    if (timestamp != newTimeStarted) {
                        newTimeStarted = timestamp.takeIf { it < currentTime } ?: currentTime
                        updatePreview()
                    }
                }
            }
        }
    }

    fun onVisible() {
        startUpdate()
    }

    fun onHidden() {
        stopUpdate()
    }

    private suspend fun updatePreview() {
        (record as MutableLiveData).value = loadPreviewViewData()
    }

    private suspend fun initializePreviewViewData() {
        if (extra.id != 0L) {
            runningRecordInteractor.get(extra.id)?.let { record ->
                newTypeId = record.id.orZero()
                newTimeStarted = record.timeStarted
            }
        }
    }

    private suspend fun loadPreviewViewData(): ChangeRunningRecordViewData {
        val record = RunningRecord(
            id = newTypeId,
            timeStarted = newTimeStarted
        )
        // TODO add record type cache
        val type = recordTypeInteractor.get(newTypeId)
        return changeRunningRecordViewDataMapper.map(record, type)
    }

    private suspend fun loadTypesViewData(): List<ViewHolderType> {
        val numberOfCards = prefsInteractor.getNumberOfCards()
        return recordTypeInteractor.getAll()
            .filter { !it.hidden }
            .map { recordTypeViewDataMapper.map(it, numberOfCards) }
    }

    private fun startUpdate() {
        timerJob = viewModelScope.launch {
            timerJob?.cancelAndJoin()
            while (isActive) {
                updatePreview()
                delay(TIMER_UPDATE)
            }
        }
    }

    private fun stopUpdate() {
        viewModelScope.launch {
            timerJob?.cancelAndJoin()
        }
    }

    companion object {
        private const val TIMER_UPDATE = 1000L
        private const val TIME_STARTED_TAG = "time_started_tag"
    }
}