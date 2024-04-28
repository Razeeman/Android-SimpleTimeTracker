package com.example.util.simpletimetracker.feature_change_record.view

import android.content.res.ColorStateList
import android.view.View
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewbinding.ViewBinding
import com.example.util.simpletimetracker.core.base.BaseFragment
import com.example.util.simpletimetracker.core.extension.addOnBackPressedListener
import com.example.util.simpletimetracker.core.extension.hideKeyboard
import com.example.util.simpletimetracker.core.extension.observeOnce
import com.example.util.simpletimetracker.core.extension.showKeyboard
import com.example.util.simpletimetracker.core.utils.setChooserColor
import com.example.util.simpletimetracker.domain.extension.orFalse
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
import com.example.util.simpletimetracker.feature_change_record.adapter.createChangeRecordChangePreviewAdapterDelegate
import com.example.util.simpletimetracker.feature_change_record.adapter.createChangeRecordCommentAdapterDelegate
import com.example.util.simpletimetracker.feature_change_record.adapter.createChangeRecordCommentFieldAdapterDelegate
import com.example.util.simpletimetracker.feature_change_record.databinding.ChangeRecordCoreLayoutBinding
import com.example.util.simpletimetracker.feature_change_record.databinding.ChangeRecordPreviewLayoutBinding
import com.example.util.simpletimetracker.feature_change_record.model.TimeAdjustmentState
import com.example.util.simpletimetracker.feature_change_record.utils.setData
import com.example.util.simpletimetracker.feature_change_record.viewData.ChangeRecordAdjustState
import com.example.util.simpletimetracker.feature_change_record.viewData.ChangeRecordChooserState
import com.example.util.simpletimetracker.feature_change_record.viewData.ChangeRecordChooserState.State.Action
import com.example.util.simpletimetracker.feature_change_record.viewData.ChangeRecordChooserState.State.Activity
import com.example.util.simpletimetracker.feature_change_record.viewData.ChangeRecordChooserState.State.Closed
import com.example.util.simpletimetracker.feature_change_record.viewData.ChangeRecordChooserState.State.Comment
import com.example.util.simpletimetracker.feature_change_record.viewData.ChangeRecordChooserState.State.Tag
import com.example.util.simpletimetracker.feature_change_record.viewData.ChangeRecordFavCommentState
import com.example.util.simpletimetracker.feature_change_record.viewData.ChangeRecordPreview
import com.example.util.simpletimetracker.feature_change_record.viewData.ChangeRecordSearchCommentState
import com.example.util.simpletimetracker.feature_change_record.viewData.ChangeRecordSimpleViewData
import com.example.util.simpletimetracker.feature_change_record.viewData.ChangeRecordTagsViewData
import com.example.util.simpletimetracker.feature_change_record.viewModel.ChangeRecordBaseViewModel
import com.example.util.simpletimetracker.feature_views.RecordSimpleView
import com.example.util.simpletimetracker.feature_views.extension.rotateDown
import com.example.util.simpletimetracker.feature_views.extension.rotateUp
import com.example.util.simpletimetracker.feature_views.extension.setOnClick
import com.example.util.simpletimetracker.feature_views.extension.visible
import com.example.util.simpletimetracker.feature_views.viewData.RecordTypeIcon
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
    private val changesPreviewAdapter: BaseRecyclerAdapter by lazy {
        BaseRecyclerAdapter(
            createHintAdapterDelegate(),
            createChangeRecordChangePreviewAdapterDelegate(),
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
        rvChangeRecordChangePreviews.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = changesPreviewAdapter
        }
    }

    fun <T : ViewBinding> initUx(
        fragment: BaseFragment<T>,
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
        containerChangeRecordTimeStartedAdjust.listener = viewModel::onAdjustTimeStartedItemClick
        containerChangeRecordTimeEndedAdjust.listener = viewModel::onAdjustTimeEndedItemClick
        containerChangeRecordTimeSplitAdjust.listener = viewModel::onAdjustTimeSplitItemClick
        fieldChangeRecordChangeCurrentPreviewTimeStarted.setOnClick(viewModel::onTimeStartedClick)
        fieldChangeRecordChangeCurrentPreviewTimeEnded.setOnClick(viewModel::onTimeEndedClick)
        btnChangeRecordChangeCurrentPreviewTimeStartedAdjust.setOnClick(viewModel::onAdjustTimeStartedClick)
        btnChangeRecordChangeCurrentPreviewTimeEndedAdjust.setOnClick(viewModel::onAdjustTimeEndedClick)
        containerChangeRecordChangeTimeCurrentAdjust.listener = viewModel::onAdjustTimeChangeClick
        btnChangeRecordSave.setOnClick(viewModel::onSaveClick)
        btnChangeRecordSplit.setOnClick(viewModel::onSplitClick)
        btnChangeRecordChange.setOnClick(viewModel::onChangeClick)
        btnChangeRecordContinue.setOnClick(viewModel::onContinueClick)
        btnChangeRecordRepeat.setOnClick(viewModel::onRepeatClick)
        btnChangeRecordDuplicate.setOnClick(viewModel::onDuplicateClick)
        btnChangeRecordMerge.setOnClick(viewModel::onMergeClick)
        fragment.addOnBackPressedListener(action = viewModel::onBackPressed)
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
            statsIconVisibility.observeOnce(
                owner = viewLifecycleOwner,
                observer = binding.btnChangeRecordStatistics::isVisible::set,
            )
            deleteIconVisibility.observeOnce(
                owner = viewLifecycleOwner,
                observer = binding.btnChangeRecordDelete::visible::set,
            )
            timeEndedVisibility.observeOnce(
                owner = viewLifecycleOwner,
                observer = { setTimeEndedVisibility(it, binding) },
            )
            types.observe(typesAdapter::replace)
            categories.observe { updateCategories(it, binding) }
            saveButtonEnabled.observe { enableModifyingButtons(it, binding) }
            timeStartedAdjustmentItems.observe(containerChangeRecordTimeStartedAdjust.adapter::replace)
            timeEndedAdjustmentItems.observe(containerChangeRecordTimeEndedAdjust.adapter::replace)
            timeSplitAdjustmentItems.observe(containerChangeRecordTimeSplitAdjust.adapter::replace)
            timeChangeAdjustmentItems.observe(containerChangeRecordChangeTimeCurrentAdjust.adapter::replace)
            chooserState.observe { updateChooserState(it, binding) }
            keyboardVisibility.observe { visible ->
                if (visible) {
                    showKeyboard(etChangeRecordComment)
                } else {
                    hideKeyboard()
                }
            }
            timeChangeAdjustmentState.observe { state ->
                containerChangeRecordChangeTimeCurrentAdjust.visible = state != TimeAdjustmentState.HIDDEN
                btnChangeRecordChangeCurrentPreviewTimeStartedAdjust
                    .setChooserColor(state == TimeAdjustmentState.TIME_STARTED)
                btnChangeRecordChangeCurrentPreviewTimeEndedAdjust
                    .setChooserColor(state == TimeAdjustmentState.TIME_ENDED)
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

    fun onSetPreview(
        binding: ChangeRecordCoreLayoutBinding,
        color: Int,
        iconId: RecordTypeIcon,
    ) {
        with(binding) {
            layoutChangeRecordTypePreview.setCardBackgroundColor(color)
            iconChangeRecordTypePreview.itemIcon = iconId
            layoutChangeRecordTagsPreview.setCardBackgroundColor(color)
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
        containerChangeRecordTime.isVisible = isClosed
        btnChangeRecordStatistics.isVisible =
            viewModel.statsIconVisibility.value.orFalse() && isClosed
        btnChangeRecordDelete.isVisible =
            viewModel.deleteIconVisibility.value.orFalse() && isClosed
        dividerChangeRecordBottom.isVisible = !isClosed

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
        btnChangeRecordChange.isEnabled = isEnabled
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
        data: ChangeRecordAdjustState,
        binding: ChangeRecordCoreLayoutBinding,
    ) = with(binding) {
        val current = data.currentData

        containerChangeRecordChange.isVisible = data.changesPreview.isNotEmpty()
        containerChangeRecordChangeCurrentPreview.setData(current)
        changesPreviewAdapter.replaceAsNew(data.changesPreview)
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

    private fun setTimeEndedVisibility(
        isVisible: Boolean,
        binding: ChangeRecordCoreLayoutBinding,
    ) {
        binding.fieldChangeRecordTimeEnded.isVisible = isVisible
        binding.containerChangeRecordTimeEndedAdjust.isVisible = isVisible
        binding.fieldChangeRecordChangeCurrentPreviewTimeEnded.isVisible = isVisible
        binding.btnChangeRecordChangeCurrentPreviewTimeEndedAdjust.isVisible = isVisible
    }

    private fun updateCategories(
        data: ChangeRecordTagsViewData,
        binding: ChangeRecordCoreLayoutBinding,
    ) = with(binding) {
        categoriesAdapter.replace(data.viewData)
        layoutChangeRecordTagsPreview.isVisible = data.selectedCount > 0
        tvChangeRecordTagPreview.text = data.selectedCount.toString()
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