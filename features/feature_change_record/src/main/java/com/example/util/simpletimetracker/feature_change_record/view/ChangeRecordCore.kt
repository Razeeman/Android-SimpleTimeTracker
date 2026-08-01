package com.example.util.simpletimetracker.feature_change_record.view

import android.annotation.SuppressLint
import android.view.View
import android.widget.EditText
import androidx.appcompat.widget.AppCompatTextView
import androidx.cardview.widget.CardView
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import com.example.util.simpletimetracker.core.base.BaseFragment
import com.example.util.simpletimetracker.core.extension.addOnBackPressedListener
import com.example.util.simpletimetracker.core.extension.hideKeyboard
import com.example.util.simpletimetracker.core.extension.showKeyboard
import com.example.util.simpletimetracker.core.utils.setChooserColor
import com.example.util.simpletimetracker.core.view.LinearLayoutManagerWithExtraLayoutSpace
import com.example.util.simpletimetracker.core.viewData.ChangeRecordDateTimeState
import com.example.util.simpletimetracker.domain.extension.orFalse
import com.example.util.simpletimetracker.feature_base_adapter.BaseRecyclerAdapter
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.button.createButtonAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.category.createCategoryAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.category.createCategoryAddAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.commentField.createCommentFieldAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.divider.createDividerAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.empty.createEmptyAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.emptySpace.createEmptySpaceAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.hint.createHintAccentAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.hint.createHintAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.hintBig.createHintBigAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.info.createInfoAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.recordType.createRecordTypeAdapterDelegate
import com.example.util.simpletimetracker.feature_change_record.R
import com.example.util.simpletimetracker.feature_change_record.adapter.createChangeRecordChangePreviewAdapterDelegate
import com.example.util.simpletimetracker.feature_change_record.adapter.createChangeRecordSliderAdapterDelegate
import com.example.util.simpletimetracker.feature_change_record.adapter.createChangeRecordTimeAdjustmentAdapterDelegate
import com.example.util.simpletimetracker.feature_change_record.adapter.createChangeRecordTimeDoublePreviewAdapterDelegate
import com.example.util.simpletimetracker.feature_change_record.adapter.createChangeRecordTimePreviewAdapterDelegate
import com.example.util.simpletimetracker.feature_change_record.databinding.ChangeRecordCoreLayoutBinding
import com.example.util.simpletimetracker.feature_change_record.viewData.ChangeRecordChooserState
import com.example.util.simpletimetracker.feature_change_record.viewData.ChangeRecordChooserState.State.Action
import com.example.util.simpletimetracker.feature_change_record.viewData.ChangeRecordChooserState.State.Activity
import com.example.util.simpletimetracker.feature_change_record.viewData.ChangeRecordChooserState.State.Closed
import com.example.util.simpletimetracker.feature_change_record.viewData.ChangeRecordChooserState.State.Comment
import com.example.util.simpletimetracker.feature_change_record.viewData.ChangeRecordChooserState.State.Tag
import com.example.util.simpletimetracker.feature_change_record.viewData.ChangeRecordTagsViewData
import com.example.util.simpletimetracker.feature_change_record.viewModel.base.ChangeRecordEditorDelegate
import com.example.util.simpletimetracker.feature_comment_selection.api.CommentSelectionViewDelegateProvider
import com.example.util.simpletimetracker.feature_views.extension.dpToPx
import com.example.util.simpletimetracker.feature_views.extension.postDelayed
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
    private val viewModel: ChangeRecordEditorDelegate,
    private val commentDelegateProvider: CommentSelectionViewDelegateProvider,
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
            createCategoryAddAdapterDelegate(
                onItemClick = viewModel::onCategorySpecialClick,
            ),
            createCommentFieldAdapterDelegate(
                afterTextChange = viewModel::onSearchTextChange,
            ),
            createDividerAdapterDelegate(),
            createInfoAdapterDelegate(),
            createHintAdapterDelegate(),
            createHintBigAdapterDelegate(),
            createEmptyAdapterDelegate(),
            createEmptySpaceAdapterDelegate(),
        )
    }
    private val actionsAdapter: BaseRecyclerAdapter by lazy {
        BaseRecyclerAdapter(
            createDividerAdapterDelegate(),
            createHintAdapterDelegate(),
            createHintAccentAdapterDelegate(),
            createChangeRecordChangePreviewAdapterDelegate(
                onCheckboxClicked = viewModel::onChangePreviewCheckClick,
                onBeforeActionClicked = viewModel::onChangePreviewBeforeActionClick,
                onAfterActionClicked = viewModel::onChangePreviewAfterActionClick,
            ),
            createChangeRecordTimePreviewAdapterDelegate(viewModel::onItemTimePreviewClick),
            createChangeRecordTimeDoublePreviewAdapterDelegate(
                onTimeStartedClick = viewModel::onItemTimeStartedClick,
                onTimeEndedClick = viewModel::onItemTimeEndedClick,
                onAdjustTimeStartedClick = viewModel::onItemAdjustTimeStartedClick,
                onAdjustTimeEndedClick = viewModel::onItemAdjustTimeEndedClick,
            ),
            createChangeRecordTimeAdjustmentAdapterDelegate(viewModel::onTimeAdjustmentClick),
            createButtonAdapterDelegate(viewModel::onItemButtonClick),
            createChangeRecordSliderAdapterDelegate(viewModel::onSliderValueChanged),
        )
    }

    private val commentsDelegate by lazy {
        commentDelegateProvider.provide(viewModel)
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
        commentsDelegate.initUi(rvChangeRecordComments)
        rvChangeRecordAction.apply {
            layoutManager = LinearLayoutManagerWithExtraLayoutSpace(context)
            adapter = actionsAdapter
        }
    }

    fun <T : ViewBinding> initUx(
        fragment: BaseFragment<T>,
        binding: ChangeRecordCoreLayoutBinding,
    ) = with(binding) {
        fieldChangeRecordType.setOnClick(viewModel::onTypeChooserClick)
        fieldChangeRecordCategory.setOnClick(viewModel::onCategoryChooserClick)
        fieldChangeRecordComment.setOnClick(viewModel::onCommentChooserClick)
        fieldChangeRecordAction.setOnClick(viewModel::onActionChooserClick)
        fieldChangeRecordTimeStarted.setOnClick(viewModel::onTimeStartedClick)
        fieldChangeRecordTimeEnded.setOnClick(viewModel::onTimeEndedClick)
        btnChangeRecordTimeStartedAdjust.setOnClick(viewModel::onTimeStartedStateClick)
        btnChangeRecordTimeEndedAdjust.setOnClick(viewModel::onTimeEndedStateClick)
        containerChangeRecordTimeStartedAdjust.listener = viewModel::onAdjustTimeStartedItemClick
        containerChangeRecordTimeEndedAdjust.listener = viewModel::onAdjustTimeEndedItemClick
        btnChangeRecordSave.setOnClick(viewModel::onSaveClick)
        binding.btnChangeRecordStatistics.setOnClick(viewModel::onStatisticsClick)
        binding.btnChangeRecordDelete.setOnClick(viewModel::onDeleteClick)
        fragment.addOnBackPressedListener(action = viewModel::onBackPressed)
    }

    fun <T : ViewBinding> initViewModel(
        fragment: BaseFragment<T>,
        binding: ChangeRecordCoreLayoutBinding,
    ) = with(viewModel) {
        with(fragment) {
            statsIconVisibility.observeOnce(binding.btnChangeRecordStatistics::isVisible::set)
            deleteIconVisibility.observeOnce(binding.btnChangeRecordDelete::visible::set)
            timeEndedVisibility.observeOnce { setTimeEndedVisibility(it, binding) }
            types.observe(typesAdapter::replace)
            categories.observe { updateCategories(it, binding) }
            saveButtonEnabled.observe { enableModifyingButtons(it, binding) }
            timeStartedAdjustmentItems.observe(binding.containerChangeRecordTimeStartedAdjust.adapter::replace)
            timeEndedAdjustmentItems.observe(binding.containerChangeRecordTimeEndedAdjust.adapter::replace)
            chooserState.observe { updateChooserState(it, binding) }
            keyboardVisibility.observe { onKeyboardVisibility(binding, it) }
            actionsViewData.observe(::setActionsViewData)
            commentsDelegate.initViewModel(this)
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

    fun setDateTime(
        state: ChangeRecordDateTimeState,
        dateView: AppCompatTextView,
        timeView: AppCompatTextView,
        hintView: AppCompatTextView,
    ) {
        hintView.text = state.hint
        when (val data = state.state) {
            is ChangeRecordDateTimeState.State.DateTime -> {
                dateView.isVisible = true
                timeView.updatePadding(left = 0)
                dateView.text = data.data.date
                timeView.text = data.data.time
            }
            is ChangeRecordDateTimeState.State.Duration -> {
                dateView.isVisible = false
                timeView.updatePadding(left = 10.dpToPx())
                timeView.text = data.data
            }
        }
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
            chooserData = rvChangeRecordComments,
            chooserView = fieldChangeRecordComment,
            chooserArrow = arrowChangeRecordComment,
        )
        updateChooser<Action>(
            state = state,
            chooserData = rvChangeRecordAction,
            chooserView = fieldChangeRecordAction,
            chooserArrow = arrowChangeRecordAction,
        )

        val isClosed = state.current is Closed
        spaceChangeRecordFieldsTop.isVisible = !isClosed
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
    }

    private fun setActionsViewData(data: List<ViewHolderType>) {
        actionsAdapter.replace(data)
    }

    private fun setTimeEndedVisibility(
        isVisible: Boolean,
        binding: ChangeRecordCoreLayoutBinding,
    ) {
        binding.fieldChangeRecordTimeEnded.isVisible = isVisible
        binding.containerChangeRecordTimeEndedAdjust.isVisible = isVisible
        binding.btnChangeRecordTimeEndedAdjust.isVisible = isVisible
    }

    @SuppressLint("SetTextI18n")
    private fun updateCategories(
        data: ChangeRecordTagsViewData,
        binding: ChangeRecordCoreLayoutBinding,
    ) = with(binding) {
        categoriesAdapter.replace(data.viewData)
        layoutChangeRecordTagsPreview.isVisible = data.selectedCount > 0
        tvChangeRecordTagPreview.text = data.selectedCount.toString()
    }

    private fun Fragment.onKeyboardVisibility(
        binding: ChangeRecordCoreLayoutBinding,
        visible: Boolean,
    ) {
        if (visible) {
            binding.rvChangeRecordComments.postDelayed(500) {
                findViewById<EditText>(R.id.etChangeRecordCommentField)
                    ?.let(::showKeyboard)
            }
        } else {
            hideKeyboard()
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