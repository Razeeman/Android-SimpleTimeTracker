package com.example.util.simpletimetracker.feature_change_record.viewModel

import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.interactor.RecordActionRepeatMediator
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.hint.HintViewData
import com.example.util.simpletimetracker.feature_change_record.R
import com.example.util.simpletimetracker.feature_change_record.adapter.ChangeRecordButtonViewData
import com.example.util.simpletimetracker.feature_change_record.model.ChangeRecordActionsBlock
import javax.inject.Inject

class ChangeRecordActionsRepeatDelegate @Inject constructor(
    private val resourceRepo: ResourceRepo,
    private val recordActionRepeatMediator: RecordActionRepeatMediator,
): ChangeRecordActionsSubDelegate<ChangeRecordActionsRepeatDelegate.Parent> {

    private var parent: Parent? = null
    private var viewData: List<ViewHolderType> = emptyList()

    override fun attach(parent: Parent) {
        this.parent = parent
    }

    override fun getViewData(): List<ViewHolderType> {
        return viewData
    }

    override suspend fun updateViewData() {
        viewData = loadRepeatViewData()
        parent?.update()
    }

    suspend fun onRepeatClickDelegate() {
        val params = parent?.getViewDataParams() ?: return
        recordActionRepeatMediator.execute(
            typeId = params.newTypeId,
            comment = params.newComment,
            tagIds = params.newCategoryIds,
        )
        // Exit.
        parent?.onSaveClickDelegate()
    }

    private fun loadRepeatViewData(): List<ViewHolderType> {
        val params = parent?.getViewDataParams()
            ?: return emptyList()
        if (!params.isAdditionalActionsAvailable) return emptyList()

        val result = mutableListOf<ViewHolderType>()
        result += HintViewData(
            text = resourceRepo.getString(R.string.change_record_repeat_hint),
        )
        result += ChangeRecordButtonViewData(
            block = ChangeRecordActionsBlock.RepeatButton,
            text = resourceRepo.getString(R.string.change_record_repeat),
            icon = R.drawable.repeat,
            iconSizeDp = 24,
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
            val newComment: String,
            val newCategoryIds: List<Long>,
            val isAdditionalActionsAvailable: Boolean,
            val isButtonEnabled: Boolean,
        )
    }
}