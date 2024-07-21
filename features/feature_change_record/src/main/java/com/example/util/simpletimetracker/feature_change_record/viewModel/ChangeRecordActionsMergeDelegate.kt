package com.example.util.simpletimetracker.feature_change_record.viewModel

import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.interactor.RecordActionMergeMediator
import com.example.util.simpletimetracker.domain.model.Record
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.hint.HintViewData
import com.example.util.simpletimetracker.feature_change_record.R
import com.example.util.simpletimetracker.feature_change_record.adapter.ChangeRecordButtonViewData
import com.example.util.simpletimetracker.feature_change_record.adapter.ChangeRecordChangePreviewViewData
import com.example.util.simpletimetracker.feature_change_record.interactor.ChangeRecordViewDataInteractor
import com.example.util.simpletimetracker.feature_change_record.mapper.ChangeRecordViewDataMapper
import com.example.util.simpletimetracker.feature_change_record.model.ChangeRecordActionsBlock
import com.example.util.simpletimetracker.feature_change_record.viewData.ChangeRecordPreview
import com.example.util.simpletimetracker.navigation.Router
import javax.inject.Inject

class ChangeRecordActionsMergeDelegate @Inject constructor(
    private val router: Router,
    private val resourceRepo: ResourceRepo,
    private val changeRecordViewDataInteractor: ChangeRecordViewDataInteractor,
    private val recordActionMergeMediator: RecordActionMergeMediator,
    private val changeRecordViewDataMapper: ChangeRecordViewDataMapper,
) {

    private var parent: Parent? = null

    fun attach(parent: Parent) {
        this.parent = parent
    }

    suspend fun getViewData(): List<ViewHolderType> {
        val params = parent?.getViewDataParams()
            ?: return emptyList()

        val result = mutableListOf<ViewHolderType>()
        val previewData = loadMergePreviewViewData(
            mergeAvailable = params.mergeAvailable,
            prevRecord = params.prevRecord,
            newTimeEnded = params.newTimeEnded,
        )
        if (previewData != null) {
            result += HintViewData(
                text = resourceRepo.getString(R.string.change_record_merge_hint),
            )
            result += ChangeRecordChangePreviewViewData(
                id = previewData.id,
                before = previewData.before,
                after = previewData.after,
                isChecked = false,
                marginTopDp = 0,
                isRemoveVisible = false,
                isCheckVisible = false,
                isCompareVisible = true,
            )
            result += ChangeRecordButtonViewData(
                block = ChangeRecordActionsBlock.MergeButton,
                text = resourceRepo.getString(R.string.change_record_merge),
                icon = R.drawable.action_merge,
                iconSizeDp = 24,
                isEnabled = params.isButtonEnabled,
            )
        }
        return result
    }

    suspend fun onMergeClickDelegate() {
        val params = parent?.getViewDataParams() ?: return
        recordActionMergeMediator.execute(
            prevRecord = params.prevRecord,
            newTimeEnded = params.newTimeEnded,
            onMergeComplete = { router.back() },
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

    private suspend fun loadMergePreviewViewData(
        mergeAvailable: Boolean,
        prevRecord: Record?,
        newTimeEnded: Long,
    ): ChangeRecordPreview? {
        if (!mergeAvailable || prevRecord == null) {
            return null
        }

        val changedRecord = getChangedRecord(prevRecord, newTimeEnded)
        val previousRecordPreview = changeRecordViewDataInteractor
            .getPreviewViewData(prevRecord)
        val changedRecordPreview = changeRecordViewDataInteractor
            .getPreviewViewData(changedRecord)

        return ChangeRecordPreview(
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

    interface Parent {

        fun getViewDataParams(): ViewDataParams?

        data class ViewDataParams(
            val mergeAvailable: Boolean,
            val prevRecord: Record?,
            val newTimeEnded: Long,
            val isButtonEnabled: Boolean,
        )
    }
}