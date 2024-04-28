package com.example.util.simpletimetracker.core.sharedViewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.util.simpletimetracker.core.R
import com.example.util.simpletimetracker.domain.interactor.AddRecordMediator
import com.example.util.simpletimetracker.domain.interactor.RemoveRecordMediator
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.extension.orZero
import com.example.util.simpletimetracker.domain.interactor.RecordInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.domain.model.Record
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

    private var recordId: Long = 0

    fun prepare(id: Long) {
        recordId = id
        (deleteButtonEnabled as MutableLiveData).value = true
    }

    fun onDeleteClick(from: ChangeRecordParams.From?) {
        (deleteButtonEnabled as MutableLiveData).value = false
        viewModelScope.launch {
            if (recordId != 0L) {
                val removedRecord = recordInteractor.get(recordId, adjusted = false)
                val typeId = removedRecord?.typeId
                val removedName = typeId
                    ?.let { recordTypeInteractor.get(it) }
                    ?.name
                    .orEmpty()
                val tag = when (from) {
                    is ChangeRecordParams.From.Records ->
                        SnackBarParams.TAG.RECORD_DELETE
                    is ChangeRecordParams.From.RecordsAll ->
                        SnackBarParams.TAG.RECORDS_ALL_DELETE
                    else -> null
                }

                removeRecordMediator.remove(recordId, typeId.orZero())

                (needUpdate as MutableLiveData).value = true

                (message as MutableLiveData).value = SnackBarParams(
                    tag = tag,
                    message = resourceRepo.getString(R.string.record_removed, removedName),
                    actionText = R.string.record_removed_undo.let(resourceRepo::getString),
                    actionListener = { removedRecord?.let(::onAction) },
                )
            }
        }
    }

    fun onMessageShown() {
        (message as MutableLiveData).value = null
    }

    fun onUpdated() {
        (needUpdate as MutableLiveData).value = false
    }

    private fun onAction(removedRecord: Record) {
        viewModelScope.launch {
            addRecordMediator.add(removedRecord)
            (needUpdate as MutableLiveData).value = true
        }
    }
}