package com.example.util.simpletimetracker.feature_change_record.view

import android.view.View
import androidx.cardview.widget.CardView
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.viewbinding.ViewBinding
import com.example.util.simpletimetracker.core.base.BaseFragment
import com.example.util.simpletimetracker.core.extension.hideKeyboard
import com.example.util.simpletimetracker.core.extension.showKeyboard
import com.example.util.simpletimetracker.core.utils.setChooserColor
import com.example.util.simpletimetracker.feature_base_adapter.BaseRecyclerAdapter
import com.example.util.simpletimetracker.feature_base_adapter.category.createCategoryAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.category.createCategoryAddAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.divider.createDividerAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.empty.createEmptyAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.hint.createHintAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.info.createInfoAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.recordType.createRecordTypeAdapterDelegate
import com.example.util.simpletimetracker.feature_change_record.adapter.createChangeRecordCommentAdapterDelegate
import com.example.util.simpletimetracker.feature_change_record.databinding.ChangeRecordCoreLayoutBinding
import com.example.util.simpletimetracker.feature_change_record.viewData.ChangeRecordChooserState
import com.example.util.simpletimetracker.feature_change_record.viewData.ChangeRecordChooserState.State.Action
import com.example.util.simpletimetracker.feature_change_record.viewData.ChangeRecordChooserState.State.Activity
import com.example.util.simpletimetracker.feature_change_record.viewData.ChangeRecordChooserState.State.Closed
import com.example.util.simpletimetracker.feature_change_record.viewData.ChangeRecordChooserState.State.Comment
import com.example.util.simpletimetracker.feature_change_record.viewData.ChangeRecordChooserState.State.Tag
import com.example.util.simpletimetracker.feature_change_record.viewData.TimeAdjustmentState
import com.example.util.simpletimetracker.feature_change_record.viewModel.ChangeRecordBaseViewModel
import com.example.util.simpletimetracker.feature_views.extension.rotateDown
import com.example.util.simpletimetracker.feature_views.extension.rotateUp
import com.example.util.simpletimetracker.feature_views.extension.setOnClick
import com.example.util.simpletimetracker.feature_views.extension.visible
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent

class ChangeRecordCore(
    private val viewModel: ChangeRecordBaseViewModel,
) {

    private val typesAdapter: BaseRecyclerAdapter by lazy {
        BaseRecyclerAdapter(
            createEmptyAdapterDelegate(),
            createRecordTypeAdapterDelegate(viewModel::onTypeClick)
        )
    }
    private val categoriesAdapter: BaseRecyclerAdapter by lazy {
        BaseRecyclerAdapter(
            createCategoryAdapterDelegate(
                onClick = viewModel::onCategoryClick,
                onLongClickWithTransition = viewModel::onCategoryLongClick,
            ),
            createCategoryAddAdapterDelegate { viewModel.onAddCategoryClick() },
            createDividerAdapterDelegate(),
            createInfoAdapterDelegate(),
            createHintAdapterDelegate(),
            createEmptyAdapterDelegate()
        )
    }
    private val commentsAdapter: BaseRecyclerAdapter by lazy {
        BaseRecyclerAdapter(
            createHintAdapterDelegate(),
            createChangeRecordCommentAdapterDelegate(viewModel::onCommentClick),
        )
    }

    fun initUi(
        binding: ChangeRecordCoreLayoutBinding,
    ) = with(binding) {
        val context = binding.root.context

        rvChangeRecordType.apply {
            layoutManager = FlexboxLayoutManager(context).apply {
                flexDirection = FlexDirection.ROW
                justifyContent = JustifyContent.CENTER
                flexWrap = FlexWrap.WRAP
            }
            adapter = typesAdapter
        }
        rvChangeRecordCategories.apply {
            layoutManager = FlexboxLayoutManager(context).apply {
                flexDirection = FlexDirection.ROW
                justifyContent = JustifyContent.CENTER
                flexWrap = FlexWrap.WRAP
            }
            adapter = categoriesAdapter
        }
        rvChangeRecordLastComments.apply {
            layoutManager = FlexboxLayoutManager(context).apply {
                flexDirection = FlexDirection.ROW
                justifyContent = JustifyContent.CENTER
                flexWrap = FlexWrap.WRAP
            }
            adapter = commentsAdapter
        }
    }

    fun initUx(
        binding: ChangeRecordCoreLayoutBinding,
    ) = with(binding) {
        etChangeRecordComment.doAfterTextChanged { viewModel.onCommentChange(it.toString()) }
        fieldChangeRecordType.setOnClick(viewModel::onTypeChooserClick)
        fieldChangeRecordCategory.setOnClick(viewModel::onCategoryChooserClick)
        fieldChangeRecordComment.setOnClick(viewModel::onCommentChooserClick)
        fieldChangeRecordAction.setOnClick(viewModel::onActionChooserClick)
        fieldChangeRecordTimeStarted.setOnClick(viewModel::onTimeStartedClick)
        fieldChangeRecordTimeEnded.setOnClick(viewModel::onTimeEndedClick)
        fieldChangeRecordTimeSplit.setOnClick(viewModel::onTimeSplitClick)
        btnChangeRecordTimeStartedAdjust.setOnClick(viewModel::onAdjustTimeStartedClick)
        btnChangeRecordTimeEndedAdjust.setOnClick(viewModel::onAdjustTimeEndedClick)
        btnChangeRecordTimeSplitAdjust.setOnClick(viewModel::onAdjustTimeSplitClick)
        containerChangeRecordTimeAdjust.listener = viewModel::onAdjustTimeItemClick
        containerChangeRecordTimeSplitAdjust.listener = viewModel::onAdjustTimeSplitItemClick
        btnChangeRecordSave.setOnClick(viewModel::onSaveClick)
        btnChangeRecordSplit.setOnClick(viewModel::onSplitClick)
        btnChangeRecordAdjust.setOnClick(viewModel::onAdjustClick)
        btnChangeRecordContinue.setOnClick(viewModel::onContinueClick)
        btnChangeRecordMerge.setOnClick(viewModel::onMergeClick)
    }

    fun <T : ViewBinding> initViewModel(
        fragment: BaseFragment<T>,
        binding: ChangeRecordCoreLayoutBinding,
    ) {
        fragment.initCoreViewModel(binding)
    }

    private fun <T : ViewBinding> BaseFragment<T>.initCoreViewModel(
        binding: ChangeRecordCoreLayoutBinding,
    ) = with(binding) {
        with(viewModel) {
            types.observe(typesAdapter::replace)
            categories.observe(categoriesAdapter::replace)
            saveButtonEnabled.observe { enableModifyingButtons(it, binding) }
            timeAdjustmentItems.observe(containerChangeRecordTimeAdjust.adapter::replace)
            timeSplitAdjustmentItems.observe(containerChangeRecordTimeSplitAdjust.adapter::replace)
            chooserState.observe { updateChooserState(it, binding) }
            keyboardVisibility.observe { visible ->
                if (visible) showKeyboard(etChangeRecordComment)
                else hideKeyboard()
            }
            timeAdjustmentState.observe { state ->
                containerChangeRecordTimeAdjust.visible = state != TimeAdjustmentState.HIDDEN
                btnChangeRecordTimeStartedAdjust.setChooserColor(state == TimeAdjustmentState.TIME_STARTED)
                btnChangeRecordTimeEndedAdjust.setChooserColor(state == TimeAdjustmentState.TIME_ENDED)
            }
            timeSplitAdjustmentState.observe { opened ->
                containerChangeRecordTimeSplitAdjust.isVisible = opened
                btnChangeRecordTimeSplitAdjust.setChooserColor(opened)
            }
            timeSplitText.observe(tvChangeRecordTimeSplit::setText)
            lastComments.observe(commentsAdapter::replace)
            comment.observe { updateUi(binding, it) }
        }
    }

    fun updateUi(
        binding: ChangeRecordCoreLayoutBinding,
        comment: String,
    ) = with(binding) {
        etChangeRecordComment.setText(comment)
        etChangeRecordComment.setSelection(comment.length)
    }

    private fun updateChooserState(
        state: ChangeRecordChooserState,
        binding: ChangeRecordCoreLayoutBinding,
    ) = with(binding) {
        updateChooser<Activity>(
            state = state,
            chooserData = rvChangeRecordType,
            chooserView = fieldChangeRecordType,
            chooserArrow = arrowChangeRecordType,
        )
        updateChooser<Tag>(
            state = state,
            chooserData = rvChangeRecordCategories,
            chooserView = fieldChangeRecordCategory,
            chooserArrow = arrowChangeRecordCategory,
        )
        updateChooser<Comment>(
            state = state,
            chooserData = containerChangeRecordComment,
            chooserView = fieldChangeRecordComment,
            chooserArrow = arrowChangeRecordComment,
        )
        updateChooser<Action>(
            state = state,
            chooserData = containerChangeRecordAction,
            chooserView = fieldChangeRecordAction,
            chooserArrow = arrowChangeRecordAction,
        )

        val isClosed = state.current is Closed

        // Chooser fields
        fieldChangeRecordType.isVisible = isClosed || state.current is Activity
        fieldChangeRecordCategory.isVisible = isClosed || state.current is Tag
        fieldChangeRecordComment.isVisible = isClosed || state.current is Comment
        fieldChangeRecordAction.isVisible = isClosed || state.current is Action
    }

    private fun enableModifyingButtons(
        isEnabled: Boolean,
        binding: ChangeRecordCoreLayoutBinding,
    ) = with(binding) {
        btnChangeRecordSave.isEnabled = isEnabled
        btnChangeRecordSplit.isEnabled = isEnabled
        btnChangeRecordAdjust.isEnabled = isEnabled
        btnChangeRecordContinue.isEnabled = isEnabled
        btnChangeRecordMerge.isEnabled = isEnabled
    }

    private inline fun <reified T : ChangeRecordChooserState.State> updateChooser(
        state: ChangeRecordChooserState,
        chooserData: View,
        chooserView: CardView,
        chooserArrow: View,
    ) {
        val opened = state.current is T
        val opening = state.previous !is T && state.current is T
        val closing = state.previous is T && state.current !is T

        chooserData.isVisible = opened
        chooserView.setChooserColor(opened)
        chooserArrow.apply {
            if (opening) rotateDown()
            if (closing) rotateUp()
        }
    }
}