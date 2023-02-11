package com.example.util.simpletimetracker.feature_change_record.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.util.simpletimetracker.core.extension.set
import com.example.util.simpletimetracker.domain.extension.flip
import com.example.util.simpletimetracker.domain.extension.orFalse
import com.example.util.simpletimetracker.domain.interactor.AddRecordMediator
import com.example.util.simpletimetracker.domain.model.Record
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_change_record.interactor.ChangeRecordViewDataInteractor
import com.example.util.simpletimetracker.feature_change_record.viewData.ChangeRecordPreview
import com.example.util.simpletimetracker.feature_change_record.viewData.ChangeRecordSimpleViewData
import com.example.util.simpletimetracker.feature_change_record.viewData.ChangeRecordViewData
import javax.inject.Inject

interface ChangeRecordSplitDelegate {
    val timeSplitAdjustmentState: LiveData<Boolean>
    val timeSplitText: LiveData<String>
    val splitPreview: LiveData<ChangeRecordPreview>
    val timeSplitAdjustmentItems: LiveData<List<ViewHolderType>>

    fun onAdjustTimeSplitClick()
}

class ChangeRecordSplitDelegateImpl @Inject constructor(
    private val changeRecordViewDataInteractor: ChangeRecordViewDataInteractor,
    private val addRecordMediator: AddRecordMediator,
) : ChangeRecordSplitDelegate {

    override val timeSplitAdjustmentItems: LiveData<List<ViewHolderType>> by lazy {
        MutableLiveData(loadTimeSplitAdjustmentItems())
    }
    override val timeSplitAdjustmentState: LiveData<Boolean> = MutableLiveData(true)
    override val timeSplitText: LiveData<String> = MutableLiveData()
    override val splitPreview: LiveData<ChangeRecordPreview> =
        MutableLiveData(ChangeRecordPreview.NotAvailable)

    override fun onAdjustTimeSplitClick() {
        val newValue = timeSplitAdjustmentState.value?.flip().orFalse()
        timeSplitAdjustmentState.set(newValue)
    }

    suspend fun onSplitClickDelegate(
        newTypeId: Long,
        newTimeStarted: Long,
        newTimeSplit: Long,
        newComment: String,
        newCategoryIds: List<Long>,
        onSplitComplete: () -> Unit,
    ) {
        Record(
            id = 0L, // Zero id creates new record
            typeId = newTypeId,
            timeStarted = newTimeStarted,
            timeEnded = newTimeSplit,
            comment = newComment,
            tagIds = newCategoryIds
        ).let {
            addRecordMediator.add(it)
        }
        onSplitComplete()
    }

    suspend fun updateTimeSplitValue(
        newTimeSplit: Long,
    ) {
        val data = loadTimeSplitValue(newTimeSplit)
        timeSplitText.set(data)
    }

    suspend fun updateSplitPreviewViewData(
        newTypeId: Long,
        newTimeStarted: Long,
        newTimeSplit: Long,
        newTimeEnded: Long,
        showTimeEnded: Boolean,
    ) {
        val data = loadSplitPreviewViewData(
            newTypeId = newTypeId,
            newTimeStarted = newTimeStarted,
            newTimeSplit = newTimeSplit,
            newTimeEnded = newTimeEnded,
            showTimeEnded = showTimeEnded,
        )
        splitPreview.set(data)
    }

    private fun loadTimeSplitAdjustmentItems(): List<ViewHolderType> {
        return changeRecordViewDataInteractor.getTimeAdjustmentItems()
    }

    private suspend fun loadTimeSplitValue(
        newTimeSplit: Long,
    ): String {
        return changeRecordViewDataInteractor.mapTime(newTimeSplit)
    }

    private suspend fun loadSplitPreviewViewData(
        newTypeId: Long,
        newTimeStarted: Long,
        newTimeSplit: Long,
        newTimeEnded: Long,
        showTimeEnded: Boolean,
    ): ChangeRecordPreview {
        val firstRecord = Record(
            typeId = newTypeId,
            timeStarted = newTimeStarted,
            timeEnded = newTimeSplit,
            comment = "",
        ).let {
            changeRecordViewDataInteractor.getPreviewViewData(it)
        }
        val secondRecord = Record(
            typeId = newTypeId,
            timeStarted = newTimeSplit,
            timeEnded = newTimeEnded,
            comment = "",
        ).let {
            changeRecordViewDataInteractor.getPreviewViewData(it)
        }

        return ChangeRecordPreview.Available(
            before = map(firstRecord, showTimeEnded = true),
            after = map(secondRecord, showTimeEnded = showTimeEnded),
        )
    }

    private fun map(
        preview: ChangeRecordViewData,
        showTimeEnded: Boolean,
    ): ChangeRecordSimpleViewData {
        return ChangeRecordSimpleViewData(
            name = preview.name,
            time = preview.timeStarted.let {
                if (showTimeEnded) "$it - ${preview.timeFinished}" else it
            },
            duration = preview.duration,
            iconId = preview.iconId,
            color = preview.color,
        )
    }
}