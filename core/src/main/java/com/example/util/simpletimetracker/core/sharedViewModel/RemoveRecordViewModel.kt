package com.example.util.simpletimetracker.core.sharedViewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.util.simpletimetracker.core.R
import com.example.util.simpletimetracker.core.extension.set
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.record.interactor.AddRecordMediator
import com.example.util.simpletimetracker.domain.record.interactor.RecordInteractor
import com.example.util.simpletimetracker.domain.record.interactor.RemoveRecordMediator
import com.example.util.simpletimetracker.domain.record.model.Record
import com.example.util.simpletimetracker.domain.record.model.RecordBase
import com.example.util.simpletimetracker.domain.recordType.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.navigation.params.notification.SnackBarParams
import com.example.util.simpletimetracker.navigation.params.screen.ChangeRecordParams
import kotlinx.coroutines.launch
import javax.inject.Inject

class RemoveRecordViewModel @Inject constructor(
    private val resourceRepo: ResourceRepo,
    private val recordInteractor: RecordInteractor,
    private val addRecordMediator: AddRecordMediator,
    private val removeRecordMediator: RemoveRecordMediator,
    private val recordTypeInteractor: RecordTypeInteractor,
) : ViewModel() {

    val deleteButtonEnabled: LiveData<Boolean> = MutableLiveData()
    val message: LiveData<SnackBarParams?> = MutableLiveData()
    val needUpdate: LiveData<Boolean> = MutableLiveData()

    private var removedRecords: List<Record> = emptyList()

    fun prepare() {
        deleteButtonEnabled.set(true)
    }

    fun onDeleteClick(
        recordIds: Set<Long>,
        from: ChangeRecordParams.From?,
    ) = viewModelScope.launch {
        deleteButtonEnabled.set(false)

        removedRecords = recordIds.mapNotNull { recordInteractor.get(it) }

        val typeIds = removedRecords.map(Record::typeId).distinct()
        removeRecordMediator.remove(
            recordIds = removedRecords.map(Record::id),
            typeIds = typeIds,
            tagIds = removedRecords.map(Record::tags).flatten()
                .map(RecordBase.Tag::tagId).distinct(),
        )

        val removedRecordsCount = removedRecords.size
        val messageText = if (removedRecordsCount == 1) {
            val typeId = typeIds.firstOrNull()
            val removedName = typeId?.let { recordTypeInteractor.get(it) }
                ?.name.orEmpty().let { "($it)" }
            resourceRepo.getString(R.string.record_removed, removedName)
        } else {
            val removedCount = "($removedRecordsCount)"
            resourceRepo.getString(R.string.record_removed, removedCount)
        }
        val tag = when (from) {
            is ChangeRecordParams.From.Records ->
                SnackBarParams.TAG.RECORD_DELETE
            is ChangeRecordParams.From.RecordsAll ->
                SnackBarParams.TAG.RECORDS_ALL_DELETE
            else -> null
        }

        needUpdate.set(true)

        val messageParams = SnackBarParams(
            tag = tag,
            message = messageText,
            actionText = R.string.record_removed_undo.let(resourceRepo::getString),
            actionListener = { onAction() },
        )
        message.set(messageParams)
    }

    fun onMessageShown() {
        message.set(null)
    }

    fun onUpdated() {
        needUpdate.set(false)
    }

    private fun onAction() = viewModelScope.launch {
        addRecordMediator.add(removedRecords)
        needUpdate.set(true)
    }
}