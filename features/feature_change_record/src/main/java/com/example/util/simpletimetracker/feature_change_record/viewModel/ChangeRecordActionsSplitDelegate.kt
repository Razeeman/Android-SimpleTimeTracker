package com.example.util.simpletimetracker.feature_change_record.viewModel

import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.interactor.AddRecordMediator
import com.example.util.simpletimetracker.domain.model.Record
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.hint.HintViewData
import com.example.util.simpletimetracker.feature_change_record.R
import com.example.util.simpletimetracker.feature_change_record.adapter.ChangeRecordButtonViewData
import com.example.util.simpletimetracker.feature_change_record.adapter.ChangeRecordChangePreviewViewData
import com.example.util.simpletimetracker.feature_change_record.adapter.ChangeRecordTimeAdjustmentViewData
import com.example.util.simpletimetracker.feature_change_record.adapter.ChangeRecordTimePreviewViewData
import com.example.util.simpletimetracker.feature_change_record.interactor.ChangeRecordViewDataInteractor
import com.example.util.simpletimetracker.feature_change_record.mapper.ChangeRecordViewDataMapper
import com.example.util.simpletimetracker.feature_change_record.model.ChangeRecordActionsBlock
import com.example.util.simpletimetracker.feature_change_record.viewData.ChangeRecordPreview
import javax.inject.Inject

class ChangeRecordActionsSplitDelegate @Inject constructor(
    private val resourceRepo: ResourceRepo,
    private val changeRecordViewDataInteractor: ChangeRecordViewDataInteractor,
    private val addRecordMediator: AddRecordMediator,
    private val changeRecordViewDataMapper: ChangeRecordViewDataMapper,
) {

    private var parent: Parent? = null

    fun attach(parent: Parent) {
        this.parent = parent
    }

    suspend fun getViewData(): List<ViewHolderType> {
        val params = parent?.getViewDataParams()
            ?: return emptyList()
        val newTimeSplit = params.newTimeSplit
        val newTypeId = params.newTypeId
        val newTimeStarted = params.newTimeStarted
        val newTimeEnded = params.newTimeEnded
        val showTimeEnded = params.showTimeEnded
        val isButtonEnabled = params.isButtonEnabled

        val result = mutableListOf<ViewHolderType>()
        result += HintViewData(resourceRepo.getString(R.string.change_record_split_hint))
        result += ChangeRecordTimePreviewViewData(
            block = ChangeRecordActionsBlock.SplitTimePreview,
            text = loadTimeSplitValue(newTimeSplit),
        )
        result += ChangeRecordTimeAdjustmentViewData(
            block = ChangeRecordActionsBlock.SplitTimeAdjustment,
            items = loadTimeSplitAdjustmentItems(),
        )
        val previewData = loadSplitPreviewViewData(
            newTypeId = newTypeId,
            newTimeStarted = newTimeStarted,
            newTimeSplit = newTimeSplit,
            newTimeEnded = newTimeEnded,
            showTimeEnded = showTimeEnded,
        )
        result += ChangeRecordChangePreviewViewData(
            id = previewData.id,
            before = previewData.before,
            after = previewData.after,
            marginTopDp = 2,
            isChecked = false,
            isRemoveVisible = false,
            isCheckVisible = false,
            isCompareVisible = false,
        )
        result += ChangeRecordButtonViewData(
            block = ChangeRecordActionsBlock.SplitButton,
            text = resourceRepo.getString(R.string.change_record_split),
            icon = R.drawable.action_divide,
            iconSizeDp = 18,
            isEnabled = isButtonEnabled,
        )
        return result
    }

    suspend fun onSplitClickDelegate() {
        val params = parent?.getViewDataParams() ?: return

        Record(
            id = 0L, // Zero id creates new record
            typeId = params.newTypeId,
            timeStarted = params.newTimeStarted,
            timeEnded = params.newTimeSplit,
            comment = params.newComment,
            tagIds = params.newCategoryIds,
        ).let {
            addRecordMediator.add(it)
        }
        parent?.onSplitComplete()
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

        return ChangeRecordPreview(
            id = 0,
            before = changeRecordViewDataMapper.mapSimple(
                preview = firstRecord,
                showTimeEnded = true,
                timeStartedChanged = false,
                timeEndedChanged = true,
            ),
            after = changeRecordViewDataMapper.mapSimple(
                preview = secondRecord,
                showTimeEnded = showTimeEnded,
                timeStartedChanged = true,
                timeEndedChanged = false,
            ),
        )
    }

    interface Parent {

        fun getViewDataParams(): ViewDataParams?
        suspend fun onSplitComplete()

        data class ViewDataParams(
            val newTimeSplit: Long,
            val newTypeId: Long,
            val newTimeStarted: Long,
            val newTimeEnded: Long,
            val newComment: String,
            val newCategoryIds: List<Long>,
            val showTimeEnded: Boolean,
            val isButtonEnabled: Boolean,
        )
    }
}