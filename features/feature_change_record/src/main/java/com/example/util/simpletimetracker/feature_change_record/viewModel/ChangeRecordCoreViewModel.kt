package com.example.util.simpletimetracker.feature_change_record.viewModel

import androidx.lifecycle.LiveData
import com.example.util.simpletimetracker.core.view.timeAdjustment.TimeAdjustmentView
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.category.CategoryViewData
import com.example.util.simpletimetracker.feature_base_adapter.recordType.RecordTypeViewData
import com.example.util.simpletimetracker.feature_change_record.viewData.ChangeRecordCommentViewData
import com.example.util.simpletimetracker.feature_change_record.viewData.TimeAdjustmentState

interface ChangeRecordCoreViewModel {
    val types: LiveData<List<ViewHolderType>>
    val categories: LiveData<List<ViewHolderType>>
    val saveButtonEnabled: LiveData<Boolean>
    val flipTypesChooser: LiveData<Boolean>
    val flipCategoryChooser: LiveData<Boolean>
    val keyboardVisibility: LiveData<Boolean>
    val timeAdjustmentItems: LiveData<List<ViewHolderType>>
    val flipLastCommentsChooser: LiveData<Boolean>
    val timeAdjustmentState: LiveData<TimeAdjustmentState>
    val comment: LiveData<String>
    val lastComments: LiveData<List<ViewHolderType>>

    fun onTypeClick(item: RecordTypeViewData)
    fun onCategoryClick(item: CategoryViewData)
    fun onCategoryLongClick(item: CategoryViewData, sharedElements: Pair<Any, String>)
    fun onAddCategoryClick()
    fun onCommentClick(item: ChangeRecordCommentViewData) {}
    fun onCommentChange(comment: String)
    fun onTypeChooserClick()
    fun onCategoryChooserClick()
    fun onTimeStartedClick()
    fun onTimeEndedClick() {}
    fun onLastCommentsChooserClick() {}
    fun onSaveClick()
    fun onAdjustTimeStartedClick()
    fun onAdjustTimeEndedClick() {}
    fun onAdjustTimeItemClick(viewData: TimeAdjustmentView.ViewData)
}