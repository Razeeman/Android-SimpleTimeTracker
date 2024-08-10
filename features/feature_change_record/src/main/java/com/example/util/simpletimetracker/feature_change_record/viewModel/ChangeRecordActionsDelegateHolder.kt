package com.example.util.simpletimetracker.feature_change_record.viewModel

import com.example.util.simpletimetracker.feature_change_record.mapper.ChangeRecordActionsDelegateMapper
import javax.inject.Inject

class ChangeRecordActionsDelegateHolder @Inject constructor(
    val mergeDelegate: ChangeRecordActionsMergeDelegate,
    val splitDelegate: ChangeRecordActionsSplitDelegate,
    val adjustDelegate: ChangeRecordActionsAdjustDelegate,
    val continueDelegate: ChangeRecordActionsContinueDelegate,
    val repeatDelegate: ChangeRecordActionsRepeatDelegate,
    val duplicateDelegate: ChangeRecordActionsDuplicateDelegate,
    private val changeRecordActionsDelegateMapper: ChangeRecordActionsDelegateMapper,
) {

    val delegatesList = listOf(
        splitDelegate,
        adjustDelegate,
        continueDelegate,
        repeatDelegate,
        duplicateDelegate,
        mergeDelegate,
    )

    fun attach(
        parent: ChangeRecordActionsDelegate.Parent,
    ) {
        splitDelegate.attach(getSplitDelegateParent(parent))
        adjustDelegate.attach(getAdjustDelegateParent(parent))
        continueDelegate.attach(getContinueActionsDelegateParent(parent))
        repeatDelegate.attach(getRepeatActionsDelegateParent(parent))
        duplicateDelegate.attach(getDuplicateActionsDelegateParent(parent))
        mergeDelegate.attach(getMergeDelegateParent(parent))
    }

    private fun getSplitDelegateParent(
        parent: ChangeRecordActionsDelegate.Parent,
    ): ChangeRecordActionsSplitDelegate.Parent {
        return changeRecordActionsDelegateMapper.getSplitDelegateParent(
            parent = parent,
            updateViewData = parent::updateViewData,
        )
    }

    private fun getAdjustDelegateParent(
        parent: ChangeRecordActionsDelegate.Parent,
    ): ChangeRecordActionsAdjustDelegate.Parent {
        return changeRecordActionsDelegateMapper.getAdjustDelegateParent(
            parent = parent,
            updateViewData = parent::updateViewData,
        )
    }

    private fun getContinueActionsDelegateParent(
        parent: ChangeRecordActionsDelegate.Parent,
    ): ChangeRecordActionsContinueDelegate.Parent {
        return changeRecordActionsDelegateMapper.getContinueActionsDelegateParent(
            parent = parent,
            updateViewData = parent::updateViewData,
        )
    }

    private fun getRepeatActionsDelegateParent(
        parent: ChangeRecordActionsDelegate.Parent,
    ): ChangeRecordActionsRepeatDelegate.Parent {
        return changeRecordActionsDelegateMapper.getRepeatActionsDelegateParent(
            parent = parent,
            updateViewData = parent::updateViewData,
        )
    }

    private fun getDuplicateActionsDelegateParent(
        parent: ChangeRecordActionsDelegate.Parent,
    ): ChangeRecordActionsDuplicateDelegate.Parent {
        return changeRecordActionsDelegateMapper.getDuplicateActionsDelegateParent(
            parent = parent,
            updateViewData = parent::updateViewData,
        )
    }

    private fun getMergeDelegateParent(
        parent: ChangeRecordActionsDelegate.Parent,
    ): ChangeRecordActionsMergeDelegate.Parent {
        return changeRecordActionsDelegateMapper.getMergeDelegateParent(
            parent = parent,
            updateViewData = parent::updateViewData,
        )
    }
}