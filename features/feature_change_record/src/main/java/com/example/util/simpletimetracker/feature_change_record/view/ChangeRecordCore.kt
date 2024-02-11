package com.example.util.simpletimetracker.feature_change_record.view

import android.content.res.ColorStateList
import android.view.View
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.isInvisible
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
import com.example.util.simpletimetracker.feature_base_adapter.hintBig.createHintBigAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.info.createInfoAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.loader.createLoaderAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.recordType.createRecordTypeAdapterDelegate
import com.example.util.simpletimetracker.feature_change_record.adapter.createChangeRecordCommentAdapterDelegate
import com.example.util.simpletimetracker.feature_change_record.adapter.createChangeRecordCommentFieldAdapterDelegate
import com.example.util.simpletimetracker.feature_change_record.databinding.ChangeRecordCoreLayoutBinding
import com.example.util.simpletimetracker.feature_change_record.databinding.ChangeRecordPreviewLayoutBinding
import com.example.util.simpletimetracker.feature_change_record.viewData.ChangeRecordChooserState
import com.example.util.simpletimetracker.feature_change_record.viewData.ChangeRecordChooserState.State.Action
import com.example.util.simpletimetracker.feature_change_record.viewData.ChangeRecordChooserState.State.Activity
import com.example.util.simpletimetracker.feature_change_record.viewData.ChangeRecordChooserState.State.Closed
import com.example.util.simpletimetracker.feature_change_record.viewData.ChangeRecordChooserState.State.Comment
import com.example.util.simpletimetracker.feature_change_record.viewData.ChangeRecordChooserState.State.Tag
import com.example.util.simpletimetracker.feature_change_record.viewData.ChangeRecordPreview
import com.example.util.simpletimetracker.feature_change_record.viewData.ChangeRecordSimpleViewData
import com.example.util.simpletimetracker.feature_change_record.model.TimeAdjustmentState
import com.example.util.simpletimetracker.feature_change_record.viewData.ChangeRecordFavCommentState
import com.example.util.simpletimetracker.feature_change_record.viewData.ChangeRecordSearchCommentState
import com.example.util.simpletimetracker.feature_change_record.viewModel.ChangeRecordBaseViewModel
import com.example.util.simpletimetracker.feature_views.RecordSimpleView
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
            createRecordTypeAdapterDelegate(viewModel::onTypeClick),
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
            createHintBigAdapterDelegate(),
            createEmptyAdapterDelegate(),
        )
    }
    private val commentsAdapter: BaseRecyclerAdapter by lazy {
        BaseRecyclerAdapter(
            createHintAdapterDelegate(),
            createChangeRecordCommentAdapterDelegate(viewModel::onCommentClick),
        )
    }
    private val searchCommentsAdapter: BaseRecyclerAdapter by lazy {
        BaseRecyclerAdapter(
            createLoaderAdapterDelegate(),
            createChangeRecordCommentFieldAdapterDelegate(
                afterTextChange = viewModel::onSearchCommentChange,
                onSearchClick = viewModel::onSearchCommentClick,
            ),
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
        rvChangeRecordSearchComments.apply {
            layoutManager = FlexboxLayoutManager(context).apply {
                flexDirection = FlexDirection.ROW
                justifyContent = JustifyContent.CENTER
                flexWrap = FlexWrap.WRAP
            }
            adapter = searchCommentsAdapter
        }
    }

    fun initUx(
        binding: ChangeRecordCoreLayoutBinding,
    ) = with(binding) {
        etChangeRecordComment.doAfterTextChanged { viewModel.onCommentChange(it.toString()) }
        btnChangeRecordFavouriteComment.setOnClick(viewModel::onFavouriteCommentClick)
        btnChangeRecordSearchComment.setOnClick(viewModel::onSearchCommentClick)
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
        btnChangeRecordRepeat.setOnClick(viewModel::onRepeatClick)
        btnChangeRecordDuplicate.setOnClick(viewModel::onDuplicateClick)
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
                if (visible) {
                    showKeyboard(etChangeRecordComment)
                } else {
                    hideKeyboard()
                }
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
            favCommentViewData.observe { setFavCommentState(it, binding) }
            searchCommentViewData.observe { setSearchCommentState(it, binding) }
            mergePreview.observe { setMergePreview(it, binding) }
            splitPreview.observe { setSplitPreview(it, binding) }
            adjustPreview.observe { setAdjustPreview(it, binding) }
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
        btnChangeRecordRepeat.isEnabled = isEnabled
        btnChangeRecordDuplicate.isEnabled = isEnabled
        btnChangeRecordMerge.isEnabled = isEnabled
    }

    private fun setMergePreview(
        data: ChangeRecordPreview,
        binding: ChangeRecordCoreLayoutBinding,
    ) = with(binding) {
        containerChangeRecordMerge.isVisible = data is ChangeRecordPreview.Available
        containerChangeRecordMergePreview.setData(data)
    }

    private fun setSplitPreview(
        data: ChangeRecordPreview,
        binding: ChangeRecordCoreLayoutBinding,
    ) = with(binding) {
        containerChangeRecordSplit.isVisible = data is ChangeRecordPreview.Available
        containerChangeRecordSplitPreview.setData(data)
        containerChangeRecordSplitPreview.ivChangeRecordPreviewCompare.isInvisible = true
    }

    private fun setAdjustPreview(
        data: Pair<ChangeRecordPreview, ChangeRecordPreview>,
        binding: ChangeRecordCoreLayoutBinding,
    ) = with(binding) {
        val prev = data.first
        val next = data.second

        containerChangeRecordAdjust.isVisible = prev is ChangeRecordPreview.Available ||
            next is ChangeRecordPreview.Available
        containerChangeRecordAdjustPrevPreview.setData(prev)
        containerChangeRecordAdjustNextPreview.setData(next)
    }

    private fun setFavCommentState(
        data: ChangeRecordFavCommentState,
        binding: ChangeRecordCoreLayoutBinding,
    ) {
        ViewCompat.setBackgroundTintList(
            binding.ivChangeRecordFavouriteComment,
            ColorStateList.valueOf(data.iconColor),
        )
        binding.btnChangeRecordFavouriteComment.visible = data.isVisible
    }

    private fun setSearchCommentState(
        data: ChangeRecordSearchCommentState,
        binding: ChangeRecordCoreLayoutBinding,
    ) {
        if (data.enabled) {
            binding.containerChangeRecordCommentField.visible = false
            binding.rvChangeRecordSearchComments.visible = true
        } else {
            binding.containerChangeRecordCommentField.visible = true
            binding.rvChangeRecordSearchComments.visible = false
        }
        searchCommentsAdapter.replaceAsNew(data.items)
    }

    private fun ChangeRecordPreviewLayoutBinding.setData(data: ChangeRecordPreview) {
        when (data) {
            is ChangeRecordPreview.NotAvailable -> {
                root.isVisible = false
            }
            is ChangeRecordPreview.Available -> {
                root.isVisible = true
                viewChangeRecordPreviewBefore.setData(data.before)
                viewChangeRecordPreviewAfter.setData(data.after)
            }
        }
    }

    private fun RecordSimpleView.setData(data: ChangeRecordSimpleViewData) {
        itemName = data.name
        itemTime = data.time
        itemDuration = data.duration
        itemIcon = data.iconId
        itemColor = data.color
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