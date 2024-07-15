package com.example.util.simpletimetracker.feature_change_record.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.util.simpletimetracker.core.extension.set
import com.example.util.simpletimetracker.domain.interactor.RecordActionMergeMediator
import com.example.util.simpletimetracker.domain.model.Record
import com.example.util.simpletimetracker.feature_change_record.interactor.ChangeRecordViewDataInteractor
import com.example.util.simpletimetracker.feature_change_record.mapper.ChangeRecordViewDataMapper
import com.example.util.simpletimetracker.feature_change_record.viewData.ChangeRecordPreview
import javax.inject.Inject

interface ChangeRecordMergeDelegate {
    val mergePreview: LiveData<ChangeRecordPreview>
}

class ChangeRecordMergeDelegateImpl @Inject constructor(
    private val changeRecordViewDataInteractor: ChangeRecordViewDataInteractor,
    private val recordActionMergeMediator: RecordActionMergeMediator,
    private val changeRecordViewDataMapper: ChangeRecordViewDataMapper,
) : ChangeRecordMergeDelegate {

    override val mergePreview: LiveData<ChangeRecordPreview> =
        MutableLiveData(ChangeRecordPreview.NotAvailable)

    suspend fun onMergeClickDelegate(
        prevRecord: Record?,
        newTimeEnded: Long,
        onMergeComplete: () -> Unit,
    ) {
        recordActionMergeMediator.execute(
            prevRecord = prevRecord,
            newTimeEnded = newTimeEnded,
            onMergeComplete = onMergeComplete,
        )
    }

    private fun getChangedRecord(
        previousRecord: Record,
        newTimeEnded: Long,
    ): Record {
        return previousRecord.copy(
            timeEnded = newTimeEnded,
        )
    }

    suspend fun updateMergePreviewViewData(
        mergeAvailable: Boolean,
        prevRecord: Record?,
        newTimeEnded: Long,
    ) {
        prevRecord ?: return
        val data = loadMergePreviewViewData(
            mergeAvailable = mergeAvailable,
            prevRecord = prevRecord,
            newTimeEnded = newTimeEnded,
        )
        mergePreview.set(data)
    }

    private suspend fun loadMergePreviewViewData(
        mergeAvailable: Boolean,
        prevRecord: Record?,
        newTimeEnded: Long,
    ): ChangeRecordPreview {
        if (!mergeAvailable || prevRecord == null) {
            return ChangeRecordPreview.NotAvailable
        }

        val changedRecord = getChangedRecord(prevRecord, newTimeEnded)
        val previousRecordPreview = changeRecordViewDataInteractor
            .getPreviewViewData(prevRecord)
        val changedRecordPreview = changeRecordViewDataInteractor
            .getPreviewViewData(changedRecord)

        return ChangeRecordPreview.Available(
            id = 0,
            before = changeRecordViewDataMapper.mapSimple(
                preview = previousRecordPreview,
                showTimeEnded = true,
                timeStartedChanged = false,
                timeEndedChanged = false,
            ),
            after = changeRecordViewDataMapper.mapSimple(
                preview = changedRecordPreview,
                showTimeEnded = true,
                timeStartedChanged = changedRecord.timeStarted != prevRecord.timeStarted,
                timeEndedChanged = changedRecord.timeEnded != prevRecord.timeEnded,
            ),
        )
    }
}