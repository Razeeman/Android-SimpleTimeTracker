package com.example.util.simpletimetracker.feature_change_record.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.util.simpletimetracker.core.adapter.ViewHolderType
import com.example.util.simpletimetracker.core.mapper.RecordTypeViewDataMapper
import com.example.util.simpletimetracker.core.mapper.TimeMapper
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.core.viewData.RecordTypeViewData
import com.example.util.simpletimetracker.domain.extension.flip
import com.example.util.simpletimetracker.domain.extension.orTrue
import com.example.util.simpletimetracker.domain.extension.orZero
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.domain.model.RangeLength
import com.example.util.simpletimetracker.domain.model.Record
import com.example.util.simpletimetracker.feature_change_record.R
import com.example.util.simpletimetracker.feature_change_record.mapper.ChangeRecordViewDataMapper
import com.example.util.simpletimetracker.feature_change_record.viewData.ChangeRecordViewData
import com.example.util.simpletimetracker.navigation.Notification
import com.example.util.simpletimetracker.navigation.Router
import com.example.util.simpletimetracker.navigation.Screen
import com.example.util.simpletimetracker.navigation.params.ChangeRecordParams
import com.example.util.simpletimetracker.navigation.params.DateTimeDialogParams
import com.example.util.simpletimetracker.navigation.params.DateTimeDialogType
import com.example.util.simpletimetracker.navigation.params.ToastParams
import kotlinx.coroutines.launch
import javax.inject.Inject

class ChangeRecordViewModel @Inject constructor(
    private val router: Router,
    private val recordInteractor: RecordInteractor,
    private val recordTypeInteractor: RecordTypeInteractor,
    private val timeMapper: TimeMapper,
    private val changeRecordViewDataMapper: ChangeRecordViewDataMapper,
    private val recordTypeViewDataMapper: RecordTypeViewDataMapper,
    private val resourceRepo: ResourceRepo,
    private val prefsInteractor: PrefsInteractor
) : ViewModel() {

    lateinit var extra: ChangeRecordParams

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
    val recordTags: LiveData<List<ViewHolderType>> by lazy {
        return@lazy MutableLiveData<List<ViewHolderType>>().let { initial ->
            viewModelScope.launch { initial.value = loadRecordTagsViewData() }
            initial
        }
    }
    val flipTypesChooser: LiveData<Boolean> = MutableLiveData()
    val saveButtonEnabled: LiveData<Boolean> = MutableLiveData(true)
    val keyboardVisibility: LiveData<Boolean> = MutableLiveData(false)

    private var newTypeId: Long = 0
    private var newTimeEnded: Long = 0
    private var newTimeStarted: Long = 0
    private var newComment: String = ""
    private var newRecordTagId: Long = 0

    fun onTypeChooserClick() {
        (keyboardVisibility as MutableLiveData).value = false
        (flipTypesChooser as MutableLiveData).value = flipTypesChooser.value
            ?.flip().orTrue()
    }

    fun onTimeStartedClick() {
        viewModelScope.launch {
            val useMilitaryTime = prefsInteractor.getUseMilitaryTimeFormat()
            val firstDayOfWeek = prefsInteractor.getFirstDayOfWeek()

            router.navigate(
                Screen.DATE_TIME_DIALOG,
                DateTimeDialogParams(
                    tag = TIME_STARTED_TAG,
                    timestamp = newTimeStarted,
                    type = DateTimeDialogType.DATETIME,
                    useMilitaryTime = useMilitaryTime,
                    firstDayOfWeek = firstDayOfWeek
                )
            )
        }
    }

    fun onTimeEndedClick() {
        viewModelScope.launch {
            val useMilitaryTime = prefsInteractor.getUseMilitaryTimeFormat()
            val firstDayOfWeek = prefsInteractor.getFirstDayOfWeek()

            router.navigate(
                Screen.DATE_TIME_DIALOG,
                DateTimeDialogParams(
                    tag = TIME_ENDED_TAG,
                    timestamp = newTimeEnded,
                    type = DateTimeDialogType.DATETIME,
                    useMilitaryTime = useMilitaryTime,
                    firstDayOfWeek = firstDayOfWeek
                )
            )
        }
    }

    fun onDeleteClick() {
        (keyboardVisibility as MutableLiveData).value = false
        router.back()
    }

    fun onSaveClick() {
        if (newTypeId == 0L) {
            showMessage(R.string.message_choose_type)
            return
        }
        (saveButtonEnabled as MutableLiveData).value = false
        viewModelScope.launch {
            // Zero id creates new record
            val id = (extra as? ChangeRecordParams.Tracked)?.id.orZero()
            Record(
                id = id,
                typeId = newTypeId,
                timeStarted = newTimeStarted,
                timeEnded = newTimeEnded,
                comment = newComment
            ).let {
                recordInteractor.add(it)
                (keyboardVisibility as MutableLiveData).value = false
                router.back()
            }
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
        viewModelScope.launch {
            when (tag) {
                TIME_STARTED_TAG -> {
                    if (timestamp != newTimeStarted) {
                        newTimeStarted = timestamp
                        if (timestamp > newTimeEnded) newTimeEnded = timestamp
                        updatePreview()
                    }
                }
                TIME_ENDED_TAG -> {
                    if (timestamp != newTimeEnded) {
                        newTimeEnded = timestamp
                        if (timestamp < newTimeStarted) newTimeStarted = timestamp
                        updatePreview()
                    }
                }
            }
        }
    }

    fun onCommentChange(comment: String) {
        viewModelScope.launch {
            if (comment != newComment) {
                newComment = comment
                updatePreview()
            }
        }
    }

    private fun getInitialDate(daysFromToday: Int): Long {
        return timeMapper.toTimestampShifted(daysFromToday, RangeLength.DAY)
    }

    private suspend fun updatePreview() {
        (record as MutableLiveData).value = loadPreviewViewData()
    }

    private suspend fun initializePreviewViewData() {
        when (extra) {
            is ChangeRecordParams.Tracked -> {
                recordInteractor.get((extra as ChangeRecordParams.Tracked).id)?.let { record ->
                    newTypeId = record.typeId.orZero()
                    newTimeStarted = record.timeStarted
                    newTimeEnded = record.timeEnded
                    newComment = record.comment
                    newRecordTagId = record.tagId
                }
            }
            is ChangeRecordParams.Untracked -> {
                newTimeStarted = (extra as ChangeRecordParams.Untracked).timeStarted
                newTimeEnded = (extra as ChangeRecordParams.Untracked).timeEnded
            }
            is ChangeRecordParams.New -> {
                newTimeEnded = getInitialDate((extra as ChangeRecordParams.New).daysFromToday)
                newTimeStarted = newTimeEnded - ONE_HOUR
            }
        }
    }

    private suspend fun loadPreviewViewData(): ChangeRecordViewData {
        val record = Record(
            typeId = newTypeId,
            timeStarted = newTimeStarted,
            timeEnded = newTimeEnded,
            comment = newComment
        )
        val type = recordTypeInteractor.get(newTypeId)
        val isDarkTheme = prefsInteractor.getDarkMode()
        val useMilitaryTime = prefsInteractor.getUseMilitaryTimeFormat()

        return changeRecordViewDataMapper.map(
            record = record,
            recordType = type,
            isDarkTheme = isDarkTheme,
            useMilitaryTime = useMilitaryTime
        )
    }

    private suspend fun loadTypesViewData(): List<ViewHolderType> {
        val numberOfCards = prefsInteractor.getNumberOfCards()
        val isDarkTheme = prefsInteractor.getDarkMode()

        return recordTypeInteractor.getAll()
            .filter { !it.hidden }
            .takeUnless { it.isEmpty() }
            ?.map { recordTypeViewDataMapper.map(it, numberOfCards, isDarkTheme) }
            ?: recordTypeViewDataMapper.mapToEmpty()
    }

    private suspend fun loadRecordTagsViewData(): List<ViewHolderType> {
        val isDarkTheme = prefsInteractor.getDarkMode()

        return emptyList()
    }

    private fun showMessage(stringResId: Int) {
        stringResId
            .let(resourceRepo::getString)
            .let { router.show(Notification.TOAST, ToastParams(it)) }
    }

    companion object {
        private const val TIME_STARTED_TAG = "time_started_tag"
        private const val TIME_ENDED_TAG = "time_ended_tag"

        private const val ONE_HOUR: Int = 60 * 60 * 1000
    }
}
