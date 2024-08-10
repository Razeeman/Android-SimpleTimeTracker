package com.example.util.simpletimetracker.feature_change_record.viewModel

import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.interactor.RecordActionDuplicateMediator
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.hint.HintViewData
import com.example.util.simpletimetracker.feature_change_record.R
import com.example.util.simpletimetracker.feature_change_record.adapter.ChangeRecordButtonViewData
import com.example.util.simpletimetracker.feature_change_record.model.ChangeRecordActionsBlock
import javax.inject.Inject

class ChangeRecordActionsDuplicateDelegate @Inject constructor(
    private val resourceRepo: ResourceRepo,
    private val recordActionDuplicateMediator: RecordActionDuplicateMediator,
): ChangeRecordActionsSubDelegate<ChangeRecordActionsDuplicateDelegate.Parent> {

    private var parent: Parent? = null
    private var viewData: List<ViewHolderType> = emptyList()

    override fun attach(parent: Parent) {
        this.parent = parent
    }

    override fun getViewData(): List<ViewHolderType> {
        return viewData
    }

    override suspend fun updateViewData() {
        viewData = loadDuplicateViewData()
        parent?.update()
    }

    suspend fun onDuplicateClickDelegate() {
        val params = parent?.getViewDataParams() ?: return
        recordActionDuplicateMediator.execute(
            typeId = params.newTypeId,
            timeStarted = params.newTimeStarted,
            timeEnded = params.newTimeEnded,
            comment = params.newComment,
            tagIds = params.newCategoryIds,
        )
        parent?.onSaveClickDelegate()
    }

    private fun loadDuplicateViewData(): List<ViewHolderType> {
        val params = parent?.getViewDataParams()
            ?: return emptyList()

        val result = mutableListOf<ViewHolderType>()
        result += HintViewData(
            text = resourceRepo.getString(R.string.change_record_duplicate_hint),
        )
        result += ChangeRecordButtonViewData(
            block = ChangeRecordActionsBlock.DuplicateButton,
            text = resourceRepo.getString(R.string.change_record_duplicate),
            icon = R.drawable.action_copy,
            iconSizeDp = 20,
            isEnabled = params.isButtonEnabled,
        )
        return result
    }

    interface Parent {

        fun getViewDataParams(): ViewDataParams?
        fun update()
        suspend fun onSaveClickDelegate()

        data class ViewDataParams(
            val newTypeId: Long,
            val newTimeStarted: Long,
            val newTimeEnded: Long,
            val newComment: String,
            val newCategoryIds: List<Long>,
            val isButtonEnabled: Boolean,
        )
    }
}