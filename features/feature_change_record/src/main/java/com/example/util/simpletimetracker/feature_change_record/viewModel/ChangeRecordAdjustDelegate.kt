package com.example.util.simpletimetracker.feature_change_record.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.util.simpletimetracker.core.base.ViewModelDelegate
import com.example.util.simpletimetracker.core.extension.addOrRemove
import com.example.util.simpletimetracker.core.extension.set
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.interactor.AddRecordMediator
import com.example.util.simpletimetracker.domain.interactor.RecordInteractor
import com.example.util.simpletimetracker.domain.interactor.RemoveRecordMediator
import com.example.util.simpletimetracker.domain.model.Range
import com.example.util.simpletimetracker.domain.model.Record
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.hint.HintAccentViewData
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

    fun attach(parent: Parent)
    fun onAdjustTimeStartedClick()
    fun onAdjustTimeEndedClick()
    fun onChangePreviewCheckClick(item: ChangeRecordChangePreviewViewData)

    interface Parent {
        suspend fun update()
    }
}

class ChangeRecordAdjustDelegateImpl @Inject constructor(
    private val changeRecordViewDataInteractor: ChangeRecordViewDataInteractor,
    private val addRecordMediator: AddRecordMediator,
    private val removeRecordMediator: RemoveRecordMediator,
    private val changeRecordViewDataMapper: ChangeRecordViewDataMapper,
    private val resourceRepo: ResourceRepo,
    private val recordInteractor: RecordInteractor,
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

    private var parent: ChangeRecordAdjustDelegate.Parent? = null
    private var recordsUnmarkedFromAdjustment: List<Long> = emptyList()

    override fun attach(parent: ChangeRecordAdjustDelegate.Parent) {
        this.parent = parent
    }

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

    override fun onChangePreviewCheckClick(item: ChangeRecordChangePreviewViewData) {
        delegateScope.launch {
            recordsUnmarkedFromAdjustment = recordsUnmarkedFromAdjustment
                .toMutableList()
                .apply { addOrRemove(item.id) }
            parent?.update()
        }
    }

    suspend fun onAdjustClickDelegate(
        recordId: Long,
        adjustNextRecordAvailable: Boolean,
        newTimeStarted: Long,
        newTimeEnded: Long,
        onAdjustComplete: () -> Unit,
    ) {
        val adjacentRecords = getAdjacentRecords(
            recordId = recordId,
            newTimeStarted = newTimeStarted,
            newTimeEnded = newTimeEnded,
            adjustNextRecordAvailable = adjustNextRecordAvailable,
        )

        adjacentRecords.previous
            .filter { it.id !in recordsUnmarkedFromAdjustment }
            .forEach { prevRecord ->
                getChangedPrevRecord(
                    record = prevRecord,
                    newTimeStarted = newTimeStarted,
                ).let { addRecordMediator.add(it) }
            }

        adjacentRecords.overlapped
            .filter { it.id !in recordsUnmarkedFromAdjustment }
            .forEach { overlappedRecord ->
                removeRecordMediator.remove(
                    recordId = overlappedRecord.id,
                    typeId = overlappedRecord.typeId,
                )
            }

        adjacentRecords.next
            .filter { it.id !in recordsUnmarkedFromAdjustment }
            .forEach { nextRecord ->
                getChangedNextRecord(
                    record = nextRecord,
                    newTimeEnded = newTimeEnded,
                ).let { addRecordMediator.add(it) }
            }

        onAdjustComplete()
    }

    suspend fun updateAdjustPreviewViewData(
        recordId: Long,
        adjustNextRecordAvailable: Boolean,
        newTypeId: Long,
        newTimeStarted: Long,
        newTimeEnded: Long,
        originalTypeId: Long,
        originalTimeStarted: Long,
        originalTimeEnded: Long,
        showTimeEnded: Boolean,
    ) {
        val adjacentRecords = getAdjacentRecords(
            recordId = recordId,
            newTimeStarted = newTimeStarted,
            newTimeEnded = newTimeEnded,
            adjustNextRecordAvailable = adjustNextRecordAvailable,
        )

        val currentData = loadAdjustPreviewViewData(
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

        val prevData = adjacentRecords.previous.map { prevRecord ->
            loadAdjustPreviewViewData(
                record = prevRecord,
                changedRecord = getChangedPrevRecord(
                    record = prevRecord,
                    newTimeStarted = newTimeStarted,
                ),
                showTimeEnded = true,
            )
        }

        val overlappedData = adjacentRecords.overlapped.map { overlappedRecord ->
            loadAdjustPreviewViewData(
                record = overlappedRecord,
                changedRecord = overlappedRecord,
                showTimeEnded = true,
            )
        }

        val nextData = adjacentRecords.next.map { nextRecord ->
            loadAdjustPreviewViewData(
                record = nextRecord,
                changedRecord = getChangedNextRecord(
                    record = nextRecord,
                    newTimeEnded = newTimeEnded,
                ),
                showTimeEnded = true,
            )
        }

        val viewData = mutableListOf<ViewHolderType>()

        fun mapItem(
            data: ChangeRecordPreview.Available,
        ): ViewHolderType {
            return ChangeRecordChangePreviewViewData(
                id = data.id,
                before = data.before,
                after = data.after,
                isChecked = data.id !in recordsUnmarkedFromAdjustment,
            )
        }

        if (nextData.isNotEmpty()) {
            viewData += HintViewData(
                resourceRepo.getString(R.string.change_record_change_next),
            )
            viewData += nextData.map(::mapItem)
        }

        if (overlappedData.isNotEmpty()) {
            viewData += HintViewData(
                resourceRepo.getString(R.string.change_record_change_overlapped),
                paddingBottom = 0,
            )
            viewData += HintAccentViewData(
                resourceRepo.getString(R.string.change_record_change_overlapped_hint),
                paddingTop = 0,
            )
            viewData += overlappedData.map(::mapItem)
        }

        if (prevData.isNotEmpty()) {
            viewData += HintViewData(
                resourceRepo.getString(R.string.change_record_change_prev),
            )
            viewData += prevData.map(::mapItem)
        }

        if (viewData.isEmpty()) {
            viewData += HintViewData(
                resourceRepo.getString(R.string.no_records_exist),
            )
        }

        val data = ChangeRecordAdjustState(
            currentData = currentData,
            changesPreview = viewData,
        )

        adjustPreview.set(data)
    }

    private suspend fun getAdjacentRecords(
        recordId: Long,
        newTimeStarted: Long,
        newTimeEnded: Long,
        adjustNextRecordAvailable: Boolean,
    ): AdjacentRecords {
        suspend fun getNext(): Record? {
            return recordInteractor.getNext(newTimeEnded)
        }

        val recordRange = Range(timeStarted = newTimeStarted, timeEnded = newTimeEnded)
        val adjacentRecords = recordInteractor.getFromRange(recordRange)
            .sortedByDescending { it.timeStarted }

        val previousRecords = adjacentRecords
            .filter { it.timeStarted < newTimeStarted && it.timeEnded <= newTimeEnded }
            .ifEmpty { recordInteractor.getPrev(newTimeStarted, limit = 1) }
            .filter { it.id != recordId }
        val overlappedRecords = adjacentRecords
            .filter { it.timeStarted >= newTimeStarted && it.timeEnded <= newTimeEnded }
            .filter { it.id != recordId }
        val nextRecords = adjacentRecords
            .filter { it.timeStarted >= newTimeStarted && it.timeEnded > newTimeEnded }
            .ifEmpty { listOfNotNull(if (adjustNextRecordAvailable) getNext() else null) }
            .takeIf { adjustNextRecordAvailable }
            .orEmpty()
            .filter { it.id != recordId }

        return AdjacentRecords(
            previous = previousRecords,
            overlapped = overlappedRecords,
            next = nextRecords,
        )
    }

    private suspend fun loadAdjustPreviewViewData(
        record: Record,
        changedRecord: Record,
        showTimeEnded: Boolean,
    ): ChangeRecordPreview.Available {
        val previousRecordPreview = changeRecordViewDataInteractor
            .getPreviewViewData(record)
        val changedRecordPreview = changeRecordViewDataInteractor
            .getPreviewViewData(changedRecord)

        return ChangeRecordPreview.Available(
            id = record.id,
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
        record: Record,
        newTimeStarted: Long,
    ): Record {
        return record.let {
            it.copy(
                timeStarted = it.timeStarted.coerceAtMost(newTimeStarted),
                timeEnded = newTimeStarted,
            )
        }
    }

    private fun getChangedNextRecord(
        record: Record,
        newTimeEnded: Long,
    ): Record {
        return record.let {
            it.copy(
                timeStarted = newTimeEnded,
                timeEnded = it.timeEnded.coerceAtLeast(newTimeEnded),
            )
        }
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

    private fun loadTimeAdjustmentItems(): List<ViewHolderType> {
        return changeRecordViewDataInteractor.getTimeAdjustmentItems()
    }

    private data class AdjacentRecords(
        val previous: List<Record>,
        val overlapped: List<Record>,
        val next: List<Record>,
    )
}