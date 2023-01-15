package com.example.util.simpletimetracker.feature_change_record.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.util.simpletimetracker.core.extension.set
import com.example.util.simpletimetracker.domain.interactor.AddRecordMediator
import com.example.util.simpletimetracker.domain.model.Record
import com.example.util.simpletimetracker.feature_change_record.interactor.ChangeRecordViewDataInteractor
import com.example.util.simpletimetracker.feature_change_record.viewData.ChangeRecordPreview
import com.example.util.simpletimetracker.feature_change_record.viewData.ChangeRecordSimpleViewData
import com.example.util.simpletimetracker.feature_change_record.viewData.ChangeRecordViewData
import javax.inject.Inject

interface ChangeRecordMergeDelegate {
    val mergePreview: LiveData<ChangeRecordPreview>

    suspend fun onMergeClickDelegate(
        prevRecord: Record?,
        newTimeEnded: Long,
        onMergeComplete: () -> Unit,
    )
}

class ChangeRecordMergeDelegateImpl @Inject constructor(
    private val changeRecordViewDataInteractor: ChangeRecordViewDataInteractor,
    private val addRecordMediator: AddRecordMediator,
) : ChangeRecordMergeDelegate {
    override val mergePreview: LiveData<ChangeRecordPreview> = MutableLiveData()

    override suspend fun onMergeClickDelegate(
        prevRecord: Record?,
        newTimeEnded: Long,
        onMergeComplete: () -> Unit,
    ) {
        // If merge would be available bot only for untracked - add removal of current record
        prevRecord?.copy(
            timeEnded = newTimeEnded,
        )?.let {
            addRecordMediator.add(it)
            onMergeComplete()
        }
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
            newTimeEnded = newTimeEnded
        )
        mergePreview.set(data)
    }

    private fun getChangedRecord(
        previousRecord: Record,
        newTimeEnded: Long,
    ): Record {
        return previousRecord.copy(
            timeEnded = newTimeEnded,
        )
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
            before = map(previousRecordPreview),
            after = map(changedRecordPreview),
        )
    }

    fun map(preview: ChangeRecordViewData): ChangeRecordSimpleViewData {
        return ChangeRecordSimpleViewData(
            name = preview.name,
            time = "${preview.timeStarted} - ${preview.timeFinished}",
            duration = preview.duration,
            iconId = preview.iconId,
            color = preview.color,
        )
    }
}