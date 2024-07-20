package com.example.util.simpletimetracker.feature_change_record.viewModel

import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.interactor.RecordActionContinueMediator
import com.example.util.simpletimetracker.domain.interactor.RecordActionDuplicateMediator
import com.example.util.simpletimetracker.domain.interactor.RecordActionRepeatMediator
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.hint.HintViewData
import com.example.util.simpletimetracker.feature_change_record.R
import com.example.util.simpletimetracker.feature_change_record.adapter.ChangeRecordButtonViewData
import com.example.util.simpletimetracker.feature_change_record.model.ChangeRecordActionsBlock
import com.example.util.simpletimetracker.navigation.Router
import javax.inject.Inject

class ChangeRecordActionsAdditionalDelegate @Inject constructor(
    private val router: Router,
    private val resourceRepo: ResourceRepo,
    private val recordActionDuplicateMediator: RecordActionDuplicateMediator,
    private val recordActionRepeatMediator: RecordActionRepeatMediator,
    private val recordActionContinueMediator: RecordActionContinueMediator,
) {

    private var parent: Parent? = null

    fun attach(parent: Parent) {
        this.parent = parent
    }

    fun getContinueViewData(): List<ViewHolderType> {
        val params = parent?.getViewDataParams()
            ?: return emptyList()

        val result = mutableListOf<ViewHolderType>()
        result += HintViewData(
            text = resourceRepo.getString(R.string.change_record_continue_hint),
        )
        result += ChangeRecordButtonViewData(
            block = ChangeRecordActionsBlock.ContinueButton,
            text = resourceRepo.getString(R.string.change_record_continue),
            icon = R.drawable.action_continue,
            iconSizeDp = 24,
            isEnabled = params.isButtonEnabled,
        )
        return result
    }

    suspend fun onContinueClickDelegate() {
        val params = parent?.getViewDataParams() ?: return
        recordActionContinueMediator.execute(
            recordId = params.recordId,
            typeId = params.newTypeId,
            timeStarted = params.newTimeStarted,
            comment = params.newComment,
            tagIds = params.newCategoryIds,
        )
        // Exit.
        router.back()
    }

    fun canContinue(): Boolean {
        val params = parent?.getViewDataParams() ?: return false

        // Can't continue future record
        return if (params.newTimeStarted > System.currentTimeMillis()) {
            parent?.showMessage(R.string.cannot_be_in_the_future)
            false
        } else {
            true
        }
    }

    fun getRepeatViewData(): List<ViewHolderType> {
        val params = parent?.getViewDataParams()
            ?: return emptyList()

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

    fun getDuplicateViewData(): List<ViewHolderType> {
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

    interface Parent {

        fun getViewDataParams(): ViewDataParams?
        suspend fun onSaveClickDelegate()
        fun showMessage(stringResId: Int)

        data class ViewDataParams(
            val recordId: Long,
            val newTypeId: Long,
            val newTimeStarted: Long,
            val newTimeEnded: Long,
            val newComment: String,
            val newCategoryIds: List<Long>,
            val isButtonEnabled: Boolean,
        )
    }
}