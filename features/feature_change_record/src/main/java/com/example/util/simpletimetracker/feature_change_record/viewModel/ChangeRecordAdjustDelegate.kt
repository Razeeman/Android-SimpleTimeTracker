package com.example.util.simpletimetracker.feature_change_record.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.util.simpletimetracker.core.base.ViewModelDelegate
import com.example.util.simpletimetracker.core.extension.set
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.interactor.AddRecordMediator
import com.example.util.simpletimetracker.domain.model.Record
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.hint.HintViewData
import com.example.util.simpletimetracker.feature_change_record.R
import com.example.util.simpletimetracker.feature_change_record.adapter.ChangeRecordChangePreviewViewData
import com.example.util.simpletimetracker.feature_change_record.interactor.ChangeRecordViewDataInteractor
import com.example.util.simpletimetracker.feature_change_record.mapper.ChangeRecordViewDataMapper
import com.example.util.simpletimetracker.feature_change_record.model.TimeAdjustmentState
import com.example.util.simpletimetracker.feature_change_record.viewData.ChangeRecordAdjustState
import com.example.util.simpletimetracker.feature_change_record.viewData.ChangeRecordPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

interface ChangeRecordAdjustDelegate {
    val adjustPreview: LiveData<ChangeRecordAdjustState>
    val timeChangeAdjustmentItems: LiveData<List<ViewHolderType>>
    val timeChangeAdjustmentState: LiveData<TimeAdjustmentState>

    fun onAdjustTimeStartedClick()
    fun onAdjustTimeEndedClick()
}

class ChangeRecordAdjustDelegateImpl @Inject constructor(
    private val changeRecordViewDataInteractor: ChangeRecordViewDataInteractor,
    private val addRecordMediator: AddRecordMediator,
    private val changeRecordViewDataMapper: ChangeRecordViewDataMapper,
    private val resourceRepo: ResourceRepo,
) : ChangeRecordAdjustDelegate, ViewModelDelegate() {

    override val adjustPreview: LiveData<ChangeRecordAdjustState> =
        MutableLiveData(
            ChangeRecordAdjustState(
                currentData = ChangeRecordPreview.NotAvailable,
                changesPreview = emptyList(),
            ),
        )
    override val timeChangeAdjustmentItems: LiveData<List<ViewHolderType>> by lazy {
        MutableLiveData(loadTimeAdjustmentItems())
    }
    override val timeChangeAdjustmentState: LiveData<TimeAdjustmentState> =
        MutableLiveData(TimeAdjustmentState.TIME_STARTED)

    override fun onAdjustTimeStartedClick() {
        updateAdjustTimeState(
            clicked = TimeAdjustmentState.TIME_STARTED,
            other = TimeAdjustmentState.TIME_ENDED,
        )
    }

    override fun onAdjustTimeEndedClick() {
        updateAdjustTimeState(
            clicked = TimeAdjustmentState.TIME_ENDED,
            other = TimeAdjustmentState.TIME_STARTED,
        )
    }

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
        newTypeId: Long,
        newTimeStarted: Long,
        newTimeEnded: Long,
        originalTypeId: Long,
        originalTimeStarted: Long,
        originalTimeEnded: Long,
        showTimeEnded: Boolean,
    ) {
        val currentData = loadAdjustPreviewViewData(
            adjustAvailable = true,
            record = Record(
                id = 0,
                typeId = originalTypeId,
                timeStarted = originalTimeStarted,
                timeEnded = originalTimeEnded,
                comment = "",
            ),
            changedRecord = Record(
                id = 0,
                typeId = newTypeId,
                timeStarted = newTimeStarted,
                timeEnded = newTimeEnded,
                comment = "",
            ),
            showTimeEnded = showTimeEnded,
        )

        val prevData = loadAdjustPreviewViewData(
            adjustAvailable = true,
            record = prevRecord,
            changedRecord = getChangedPrevRecord(
                record = prevRecord,
                newTimeStarted = newTimeStarted,
            ),
            showTimeEnded = true,
        ).let { listOf(it) }.filterIsInstance<ChangeRecordPreview.Available>()

        val nextData = loadAdjustPreviewViewData(
            adjustAvailable = adjustNextRecordAvailable,
            record = nextRecord,
            changedRecord = getChangedNextRecord(
                record = nextRecord,
                newTimeEnded = newTimeEnded,
            ),
            showTimeEnded = true,
        ).let { listOf(it) }.filterIsInstance<ChangeRecordPreview.Available>()

        val viewData = mutableListOf<ViewHolderType>()

        fun mapItem(
            data: ChangeRecordPreview.Available,
        ): ViewHolderType {
            return ChangeRecordChangePreviewViewData(
                id = 0,
                before = data.before,
                after = data.after,
            )
        }

        if (prevData.isNotEmpty()) {
            viewData += HintViewData(
                resourceRepo.getString(R.string.change_record_change_prev),
            )
            viewData += prevData.map(::mapItem)
        }

        if (nextData.isNotEmpty()) {
            viewData += HintViewData(
                resourceRepo.getString(R.string.change_record_change_next),
            )
            viewData += nextData.map(::mapItem)
        }

        val data = ChangeRecordAdjustState(
            currentData = currentData,
            changesPreview = viewData,
        )
        adjustPreview.set(data)
    }

    private fun updateAdjustTimeState(
        clicked: TimeAdjustmentState,
        other: TimeAdjustmentState,
    ) {
        when (timeChangeAdjustmentState.value) {
            TimeAdjustmentState.HIDDEN -> {
                timeChangeAdjustmentState.set(clicked)
            }
            clicked -> {
                timeChangeAdjustmentState.set(TimeAdjustmentState.HIDDEN)
            }
            other -> delegateScope.launch {
                timeChangeAdjustmentState.set(TimeAdjustmentState.HIDDEN)
                delay(300)
                timeChangeAdjustmentState.set(clicked)
            }
            else -> {
                // Do nothing
            }
        }
    }

    private suspend fun loadAdjustPreviewViewData(
        adjustAvailable: Boolean,
        record: Record?,
        changedRecord: Record?,
        showTimeEnded: Boolean,
    ): ChangeRecordPreview {
        if (!adjustAvailable || record == null || changedRecord == null) {
            return ChangeRecordPreview.NotAvailable
        }

        val previousRecordPreview = changeRecordViewDataInteractor
            .getPreviewViewData(record)
        val changedRecordPreview = changeRecordViewDataInteractor
            .getPreviewViewData(changedRecord)

        return ChangeRecordPreview.Available(
            before = changeRecordViewDataMapper.mapSimple(
                preview = previousRecordPreview,
                showTimeEnded = showTimeEnded,
                timeStartedChanged = false,
                timeEndedChanged = false,
            ),
            after = changeRecordViewDataMapper.mapSimple(
                preview = changedRecordPreview,
                showTimeEnded = showTimeEnded,
                timeStartedChanged = changedRecord.timeStarted != record.timeStarted,
                timeEndedChanged = changedRecord.timeEnded != record.timeEnded,
            ),
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

    private fun loadTimeAdjustmentItems(): List<ViewHolderType> {
        return changeRecordViewDataInteractor.getTimeAdjustmentItems()
    }
}