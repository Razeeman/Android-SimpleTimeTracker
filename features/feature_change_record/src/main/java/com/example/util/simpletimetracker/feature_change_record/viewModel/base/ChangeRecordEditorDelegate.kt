package com.example.util.simpletimetracker.feature_change_record.viewModel.base

import androidx.lifecycle.LiveData
import com.example.util.simpletimetracker.core.base.ScopeHolder
import com.example.util.simpletimetracker.core.view.timeAdjustment.TimeAdjustmentView
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.button.ButtonViewData
import com.example.util.simpletimetracker.feature_base_adapter.category.CategoryAddViewData
import com.example.util.simpletimetracker.feature_base_adapter.category.CategoryViewData
import com.example.util.simpletimetracker.feature_base_adapter.recordType.RecordTypeViewData
import com.example.util.simpletimetracker.feature_change_record.adapter.ChangeRecordChangePreviewViewData
import com.example.util.simpletimetracker.feature_change_record.adapter.ChangeRecordSliderViewData
import com.example.util.simpletimetracker.feature_change_record.adapter.ChangeRecordTimeAdjustmentViewData
import com.example.util.simpletimetracker.feature_change_record.adapter.ChangeRecordTimeDoublePreviewViewData
import com.example.util.simpletimetracker.feature_change_record.adapter.ChangeRecordTimePreviewViewData
import com.example.util.simpletimetracker.feature_change_record.model.ChangeRecordDateTimeFieldsState
import com.example.util.simpletimetracker.feature_change_record.viewData.ChangeRecordChooserState
import com.example.util.simpletimetracker.feature_change_record.viewData.ChangeRecordTagsViewData
import com.example.util.simpletimetracker.feature_comment_selection.api.CommentSelectionViewModelDelegate
import com.example.util.simpletimetracker.navigation.params.screen.RecordTagValueSelectionParams

interface ChangeRecordEditorDelegate : ScopeHolder, CommentSelectionViewModelDelegate {
    val commentSelectionViewModelDelegate: CommentSelectionViewModelDelegate
    val types: LiveData<List<ViewHolderType>>
    val categories: LiveData<ChangeRecordTagsViewData>
    val timeStartedAdjustmentItems: LiveData<List<ViewHolderType>>
    val timeEndedAdjustmentItems: LiveData<List<ViewHolderType>>
    val chooserState: LiveData<ChangeRecordChooserState>
    val actionsViewData: LiveData<List<ViewHolderType>>
    val saveButtonEnabled: LiveData<Boolean>
    val keyboardVisibility: LiveData<Boolean>
    val timeEndedVisibility: LiveData<Boolean>
    val deleteIconVisibility: LiveData<Boolean>
    val statsIconVisibility: LiveData<Boolean>
    val recordState: ChangeRecordEditorState
    var dateTimeState: ChangeRecordDateTimeFieldsState

    fun attach(mode: ChangeRecordEditorMode)
    fun clear()
    fun onDeleteClick()
    fun onStatisticsClick()
    fun afterInitializePreviewViewData()
    suspend fun afterTimeStartedChanged()
    suspend fun afterTimeEndedChanged()
    fun updateActionsData()
    fun onVisible()
    fun onTypeChooserClick()
    fun onCategoryChooserClick()
    fun onCommentChooserClick()
    fun onActionChooserClick()
    fun onTimeStartedClick()
    fun onTimeEndedClick()
    fun onTimeStartedStateClick()
    fun onTimeEndedStateClick()
    fun onItemTimePreviewClick(data: ChangeRecordTimePreviewViewData)
    fun onItemTimeStartedClick(data: ChangeRecordTimeDoublePreviewViewData)
    fun onItemTimeEndedClick(data: ChangeRecordTimeDoublePreviewViewData)
    fun onItemAdjustTimeStartedClick(data: ChangeRecordTimeDoublePreviewViewData)
    fun onItemAdjustTimeEndedClick(data: ChangeRecordTimeDoublePreviewViewData)
    fun onChangePreviewCheckClick(item: ChangeRecordChangePreviewViewData)
    fun onChangePreviewBeforeActionClick()
    fun onChangePreviewAfterActionClick()
    fun onSaveClick()
    fun onItemButtonClick(viewData: ButtonViewData)
    fun onTypeClick(item: RecordTypeViewData)
    fun onCategoryClick(item: CategoryViewData)
    fun onCategoryValueSelected(params: RecordTagValueSelectionParams, value: Double)
    fun onDataSelected(tag: String?, dataIds: List<Long>)
    fun onCategoryLongClick(item: CategoryViewData, sharedElements: Pair<Any, String>)
    fun onCategorySpecialClick(viewData: CategoryAddViewData)
    fun onSearchTextChange(text: String)
    fun onDateTimeSet(timestamp: Long, tag: String?)
    fun onDurationSet(durationSeconds: Long, tag: String?)
    fun onTimeAdjustmentClick(data: ChangeRecordTimeAdjustmentViewData, viewData: TimeAdjustmentView.ViewData)
    fun onSliderValueChanged(viewData: ChangeRecordSliderViewData, value: Float)
    fun onAdjustTimeStartedItemClick(viewData: TimeAdjustmentView.ViewData)
    fun onAdjustTimeEndedItemClick(viewData: TimeAdjustmentView.ViewData)
    fun onBackPressed()
    fun showMessage(stringResId: Int)
}