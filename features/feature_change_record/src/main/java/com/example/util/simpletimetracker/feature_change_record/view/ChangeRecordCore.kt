package com.example.util.simpletimetracker.feature_change_record.view

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
        fieldChangeRecordTimeStarted.setOnClick(viewModel::onTimeStartedClick)
        fieldChangeRecordTimeEnded.setOnClick(viewModel::onTimeEndedClick)
        fieldChangeRecordLastComments.setOnClick(viewModel::onLastCommentsChooserClick)
        btnChangeRecordTimeStartedAdjust.setOnClick(viewModel::onAdjustTimeStartedClick)
        btnChangeRecordTimeEndedAdjust.setOnClick(viewModel::onAdjustTimeEndedClick)
        containerChangeRecordTimeAdjust.listener = viewModel::onAdjustTimeItemClick
        btnChangeRecordSave.setOnClick(viewModel::onSaveClick)
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
            saveButtonEnabled.observe(btnChangeRecordSave::setEnabled)
            timeAdjustmentItems.observe(containerChangeRecordTimeAdjust.adapter::replace)
            flipTypesChooser.observe { opened ->
                rvChangeRecordType.visible = opened
                fieldChangeRecordType.setChooserColor(opened)
                arrowChangeRecordType.apply {
                    if (opened) rotateDown() else rotateUp()
                }
            }
            flipCategoryChooser.observe { opened ->
                rvChangeRecordCategories.visible = opened
                fieldChangeRecordCategory.setChooserColor(opened)
                arrowChangeRecordCategory.apply {
                    if (opened) rotateDown() else rotateUp()
                }
            }
            flipLastCommentsChooser.observe { opened ->
                rvChangeRecordLastComments.visible = opened
                fieldChangeRecordLastComments.setChooserColor(opened)
                arrowChangeRecordLastComment.apply {
                    if (opened) rotateDown() else rotateUp()
                }
            }
            keyboardVisibility.observe { visible ->
                if (visible) showKeyboard(etChangeRecordComment)
                else hideKeyboard()
            }
            timeAdjustmentState.observe { state ->
                containerChangeRecordTimeAdjust.visible = state != TimeAdjustmentState.HIDDEN
                btnChangeRecordTimeStartedAdjust.setChooserColor(state == TimeAdjustmentState.TIME_STARTED)
                btnChangeRecordTimeEndedAdjust.setChooserColor(state == TimeAdjustmentState.TIME_ENDED)
            }
            lastComments.observe { data ->
                fieldChangeRecordLastComments.visible = data.isNotEmpty()
                commentsAdapter.replace(data)
            }
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
}