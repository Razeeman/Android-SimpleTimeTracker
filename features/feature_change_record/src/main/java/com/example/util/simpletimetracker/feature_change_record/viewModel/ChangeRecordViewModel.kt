package com.example.util.simpletimetracker.feature_change_record.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.util.simpletimetracker.core.extension.addOrRemove
import com.example.util.simpletimetracker.core.extension.set
import com.example.util.simpletimetracker.core.interactor.RecordTagViewDataInteractor
import com.example.util.simpletimetracker.core.interactor.RecordTypesViewDataInteractor
import com.example.util.simpletimetracker.core.mapper.TimeMapper
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.extension.flip
import com.example.util.simpletimetracker.domain.extension.orTrue
import com.example.util.simpletimetracker.domain.extension.orZero
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordToRecordTagInteractor
import com.example.util.simpletimetracker.domain.model.RangeLength
import com.example.util.simpletimetracker.domain.model.Record
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.category.CategoryViewData
import com.example.util.simpletimetracker.feature_base_adapter.recordType.RecordTypeViewData
import com.example.util.simpletimetracker.feature_change_record.R
import com.example.util.simpletimetracker.feature_change_record.interactor.ChangeRecordViewDataInteractor
import com.example.util.simpletimetracker.feature_change_record.viewData.ChangeRecordViewData
import com.example.util.simpletimetracker.navigation.Router
import com.example.util.simpletimetracker.navigation.params.notification.ToastParams
import com.example.util.simpletimetracker.navigation.params.screen.ChangeRecordParams
import com.example.util.simpletimetracker.navigation.params.screen.DateTimeDialogParams
import com.example.util.simpletimetracker.navigation.params.screen.DateTimeDialogType
import kotlinx.coroutines.launch
import javax.inject.Inject

class ChangeRecordViewModel @Inject constructor(
    private val router: Router,
    private val recordInteractor: RecordInteractor,
    private val recordToRecordTagInteractor: RecordToRecordTagInteractor,
    private val changeRecordViewDataInteractor: ChangeRecordViewDataInteractor,
    private val recordTypesViewDataInteractor: RecordTypesViewDataInteractor,
    private val recordTagViewDataInteractor: RecordTagViewDataInteractor,
    private val timeMapper: TimeMapper,
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
    val categories: LiveData<List<ViewHolderType>> by lazy {
        return@lazy MutableLiveData<List<ViewHolderType>>().let { initial ->
            viewModelScope.launch {
                initializePreviewViewData()
                initial.value = loadCategoriesViewData()
            }
            initial
        }
    }
    val flipTypesChooser: LiveData<Boolean> = MutableLiveData()
    val flipCategoryChooser: LiveData<Boolean> = MutableLiveData()
    val saveButtonEnabled: LiveData<Boolean> = MutableLiveData(true)
    val keyboardVisibility: LiveData<Boolean> = MutableLiveData(false)

    private var newTypeId: Long = 0
    private var newTimeEnded: Long = 0
    private var newTimeStarted: Long = 0
    private var newComment: String = ""
    private var newCategoryId: Long = 0
    private var newGeneralCategories: MutableList<Long> = mutableListOf()

    fun onTypeChooserClick() {
        (keyboardVisibility as MutableLiveData).value = false
        (flipTypesChooser as MutableLiveData).value = flipTypesChooser.value
            ?.flip().orTrue()

        if (flipCategoryChooser.value == true) {
            (flipCategoryChooser as MutableLiveData).value = false
        }
    }

    fun onCategoryChooserClick() {
        (keyboardVisibility as MutableLiveData).value = false
        (flipCategoryChooser as MutableLiveData).value = flipCategoryChooser.value
            ?.flip().orTrue()

        if (flipTypesChooser.value == true) {
            (flipTypesChooser as MutableLiveData).value = false
        }
    }

    fun onTimeStartedClick() {
        viewModelScope.launch {
            val useMilitaryTime = prefsInteractor.getUseMilitaryTimeFormat()
            val firstDayOfWeek = prefsInteractor.getFirstDayOfWeek()

            router.navigate(
                DateTimeDialogParams(
                    tag = TIME_STARTED_TAG,
                    timestamp = newTimeStarted,
                    type = DateTimeDialogType.DATETIME(),
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
                DateTimeDialogParams(
                    tag = TIME_ENDED_TAG,
                    timestamp = newTimeEnded,
                    type = DateTimeDialogType.DATETIME(),
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
            showMessage(R.string.change_record_message_choose_type)
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
                comment = newComment,
                tagId = newCategoryId
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
                newCategoryId = 0L
                updatePreview()
                updateCategoriesViewData()
            }
        }
    }

    fun onCategoryClick(item: CategoryViewData) {
        viewModelScope.launch {
            when (item) {
                is CategoryViewData.Record.Tagged -> {
                    if (item.id != newCategoryId) {
                        newCategoryId = item.id
                    }
                }
                is CategoryViewData.Record.General -> {
                    newGeneralCategories.addOrRemove(item.id)
                }
                is CategoryViewData.Record.Untagged -> {
                    newCategoryId = 0
                    newGeneralCategories.clear()
                }
                else -> return@launch
            }
            updatePreview()
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
                    newCategoryId = record.tagId
                    newGeneralCategories = recordToRecordTagInteractor.getTagIdsByRecordId(record.id)
                        .toMutableList()
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
            comment = newComment,
            tagId = newCategoryId
        )

        return changeRecordViewDataInteractor.getPreviewViewData(
            record = record,
            generalTagIds = newGeneralCategories
        )
    }

    private suspend fun loadTypesViewData(): List<ViewHolderType> {
        return recordTypesViewDataInteractor.getTypesViewData()
    }

    private fun updateCategoriesViewData() = viewModelScope.launch {
        val data = loadCategoriesViewData()
        categories.set(data)
    }

    private suspend fun loadCategoriesViewData(): List<ViewHolderType> {
        return recordTagViewDataInteractor.getViewData(newTypeId)
    }

    private fun showMessage(stringResId: Int) {
        val params = ToastParams(message = resourceRepo.getString(stringResId))
        router.show(params)
    }

    companion object {
        private const val TIME_STARTED_TAG = "time_started_tag"
        private const val TIME_ENDED_TAG = "time_ended_tag"

        private const val ONE_HOUR: Int = 60 * 60 * 1000
    }
}
