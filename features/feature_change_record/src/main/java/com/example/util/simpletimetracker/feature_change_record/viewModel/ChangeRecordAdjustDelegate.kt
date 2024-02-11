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

interface ChangeRecordAdjustDelegate {
    val adjustPreview: LiveData<Pair<ChangeRecordPreview, ChangeRecordPreview>>
}

class ChangeRecordAdjustDelegateImpl @Inject constructor(
    private val changeRecordViewDataInteractor: ChangeRecordViewDataInteractor,
    private val addRecordMediator: AddRecordMediator,
) : ChangeRecordAdjustDelegate {

    override val adjustPreview: LiveData<Pair<ChangeRecordPreview, ChangeRecordPreview>> =
        MutableLiveData(ChangeRecordPreview.NotAvailable to ChangeRecordPreview.NotAvailable)

    suspend fun onAdjustClickDelegate(
        adjustNextRecordAvailable: Boolean,
        prevRecord: Record?,
        nextRecord: Record?,
        newTimeStarted: Long,
        newTimeEnded: Long,
        onAdjustComplete: () -> Unit,
    ) {
        getChangedPrevRecord(
            record = prevRecord,
            newTimeStarted = newTimeStarted,
        )?.let {
            addRecordMediator.add(it)
        }

        if (adjustNextRecordAvailable) {
            getChangedNextRecord(
                record = nextRecord,
                newTimeEnded = newTimeEnded,
            )?.let {
                addRecordMediator.add(it)
            }
        }

        onAdjustComplete()
    }

    suspend fun updateAdjustPreviewViewData(
        adjustNextRecordAvailable: Boolean,
        prevRecord: Record?,
        nextRecord: Record?,
        newTimeStarted: Long,
        newTimeEnded: Long,
    ) {
        val prevData = loadAdjustPreviewViewData(
            adjustAvailable = true,
            record = prevRecord,
            changedRecord = getChangedPrevRecord(
                record = prevRecord,
                newTimeStarted = newTimeStarted,
            ),
        )
        val nextData = loadAdjustPreviewViewData(
            adjustAvailable = adjustNextRecordAvailable,
            record = nextRecord,
            changedRecord = getChangedNextRecord(
                record = nextRecord,
                newTimeEnded = newTimeEnded,
            ),
        )
        adjustPreview.set(prevData to nextData)
    }

    private suspend fun loadAdjustPreviewViewData(
        adjustAvailable: Boolean,
        record: Record?,
        changedRecord: Record?,
    ): ChangeRecordPreview {
        if (!adjustAvailable || record == null || changedRecord == null) {
            return ChangeRecordPreview.NotAvailable
        }

        val previousRecordPreview = changeRecordViewDataInteractor
            .getPreviewViewData(record)
        val changedRecordPreview = changeRecordViewDataInteractor
            .getPreviewViewData(changedRecord)

        return ChangeRecordPreview.Available(
            before = map(previousRecordPreview),
            after = map(changedRecordPreview),
        )
    }

    private fun getChangedPrevRecord(
        record: Record?,
        newTimeStarted: Long,
    ): Record? {
        return record?.let {
            it.copy(
                timeStarted = it.timeStarted.coerceAtMost(newTimeStarted),
                timeEnded = newTimeStarted,
            )
        }
    }

    private fun getChangedNextRecord(
        record: Record?,
        newTimeEnded: Long,
    ): Record? {
        return record?.let {
            it.copy(
                timeStarted = newTimeEnded,
                timeEnded = it.timeEnded.coerceAtLeast(newTimeEnded),
            )
        }
    }

    private fun map(preview: ChangeRecordViewData): ChangeRecordSimpleViewData {
        return ChangeRecordSimpleViewData(
            name = preview.name,
            time = "${preview.timeStarted} - ${preview.timeFinished}",
            duration = preview.duration,
            iconId = preview.iconId,
            color = preview.color,
        )
    }
}