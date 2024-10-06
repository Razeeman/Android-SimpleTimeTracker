package com.example.util.simpletimetracker.feature_data_edit.view

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import com.example.util.simpletimetracker.core.base.BaseFragment
import com.example.util.simpletimetracker.core.dialog.RecordsFilterListener
import com.example.util.simpletimetracker.core.dialog.StandardDialogListener
import com.example.util.simpletimetracker.core.extension.hideKeyboard
import com.example.util.simpletimetracker.core.extension.showKeyboard
import com.example.util.simpletimetracker.core.utils.InsetConfiguration
import com.example.util.simpletimetracker.feature_base_adapter.BaseRecyclerAdapter
import com.example.util.simpletimetracker.feature_base_adapter.category.createCategoryAdapterDelegate
import com.example.util.simpletimetracker.feature_data_edit.dialog.DataEditTagSelectionDialogListener
import com.example.util.simpletimetracker.feature_data_edit.dialog.DataEditTypeSelectionDialogListener
import com.example.util.simpletimetracker.feature_data_edit.model.DataEditAddTagsState
import com.example.util.simpletimetracker.feature_data_edit.model.DataEditChangeActivityState
import com.example.util.simpletimetracker.feature_data_edit.model.DataEditChangeButtonState
import com.example.util.simpletimetracker.feature_data_edit.model.DataEditChangeCommentState
import com.example.util.simpletimetracker.feature_data_edit.model.DataEditDeleteRecordsState
import com.example.util.simpletimetracker.feature_data_edit.model.DataEditRecordsCountState
import com.example.util.simpletimetracker.feature_data_edit.model.DataEditRemoveTagsState
import com.example.util.simpletimetracker.feature_data_edit.viewModel.DataEditViewModel
import com.example.util.simpletimetracker.feature_views.extension.setOnClick
import com.example.util.simpletimetracker.navigation.params.screen.RecordsFilterResultParams
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import dagger.hilt.android.AndroidEntryPoint
import com.example.util.simpletimetracker.feature_data_edit.databinding.DataEditFragmentBinding as Binding

@AndroidEntryPoint
class DataEditFragment :
    BaseFragment<Binding>(),
    RecordsFilterListener,
    StandardDialogListener,
    DataEditTypeSelectionDialogListener,
    DataEditTagSelectionDialogListener {

    override val inflater: (LayoutInflater, ViewGroup?, Boolean) -> Binding =
        Binding::inflate

    override var insetConfiguration: InsetConfiguration =
        InsetConfiguration.ApplyToView { binding.containerDataEdit }

    private val viewModel: DataEditViewModel by viewModels()

    private val addTagsPreviewAdapter: BaseRecyclerAdapter by lazy {
        BaseRecyclerAdapter(createCategoryAdapterDelegate())
    }
    private val removeTagsPreviewAdapter: BaseRecyclerAdapter by lazy {
        BaseRecyclerAdapter(createCategoryAdapterDelegate())
    }

    override fun initUi(): Unit = with(binding) {
        rvDataEditAddTagsPreview.apply {
            layoutManager = FlexboxLayoutManager(requireContext()).apply {
                flexDirection = FlexDirection.ROW
                justifyContent = JustifyContent.FLEX_START
                flexWrap = FlexWrap.WRAP
            }
            adapter = addTagsPreviewAdapter
        }
        rvDataEditRemoveTagsPreview.apply {
            layoutManager = FlexboxLayoutManager(requireContext()).apply {
                flexDirection = FlexDirection.ROW
                justifyContent = JustifyContent.FLEX_START
                flexWrap = FlexWrap.WRAP
            }
            adapter = removeTagsPreviewAdapter
        }
    }

    override fun initUx() = with(binding) {
        btnDataEditSelectRecords.setOnClick(throttle(viewModel::onSelectRecordsClick))
        checkboxDataEditChangeActivity.setOnClick(viewModel::onChangeActivityClick)
        checkboxDataEditChangeComment.setOnClick(viewModel::onChangeCommentClick)
        checkboxDataEditAddTag.setOnClick(viewModel::onAddTagsClick)
        checkboxDataEditRemoveTag.setOnClick(viewModel::onRemoveTagsClick)
        checkboxDataEditDeleteRecords.setOnClick(viewModel::onDeleteRecordsClick)
        etDataEditChangeComment.doAfterTextChanged { viewModel.onCommentChange(it.toString()) }
        btnDataEditChange.setOnClick(throttle(viewModel::onChangeClick))
        btnDataEditDeleteRecords.setOnClick(throttle(viewModel::onDeleteAllRecordsClick))
        btnDataEditDeleteData.setOnClick(throttle(viewModel::onDeleteDataClick))
    }

    override fun initViewModel(): Unit = with(viewModel) {
        with(binding) {
            selectedRecordsCountViewData.observe(::setRecordsCountState)
            changeActivityState.observe(::setChangeActivityState)
            changeCommentState.observe(::setChangeCommentState)
            addTagsState.observe(::setAddTagState)
            removeTagsState.observe(::setRemoveTagState)
            deleteRecordsState.observe(::setDeleteRecordsState)
            changeButtonState.observe(::setChangeButtonState)
            disableButtons.observe { disableButtons() }
            keyboardVisibility.observe { visible ->
                if (visible) {
                    showKeyboard(etDataEditChangeComment)
                } else {
                    hideKeyboard()
                }
            }
        }
    }

    override fun onResume() = with(binding) {
        super.onResume()
        checkboxDataEditChangeActivity.jumpDrawablesToCurrentState()
        checkboxDataEditChangeComment.jumpDrawablesToCurrentState()
        checkboxDataEditAddTag.jumpDrawablesToCurrentState()
        checkboxDataEditRemoveTag.jumpDrawablesToCurrentState()
        checkboxDataEditDeleteRecords.jumpDrawablesToCurrentState()
    }

    override fun onFilterChanged(result: RecordsFilterResultParams) {
        viewModel.onFilterSelected(result)
    }

    override fun onFilterDismissed(tag: String) {
        viewModel.onFilterDismissed(tag)
    }

    override fun onPositiveClick(tag: String?, data: Any?) {
        viewModel.onPositiveDialogClick(tag)
    }

    override fun onTypeSelected(typeId: Long) {
        viewModel.onTypeSelected(typeId)
    }

    override fun onTypeDismissed() {
        viewModel.onTypeDismissed()
    }

    override fun onTagsSelected(tag: String, tagIds: List<Long>) {
        viewModel.onTagsSelected(tag, tagIds)
    }

    override fun onTagsDismissed() {
        viewModel.onTagsDismissed()
    }

    private fun setRecordsCountState(
        state: DataEditRecordsCountState,
    ) = with(binding) {
        tvDataEditSelectedRecords.text = state.countText
    }

    private fun setChangeActivityState(
        state: DataEditChangeActivityState,
    ) = with(binding) {
        when (state) {
            is DataEditChangeActivityState.Disabled -> {
                checkboxDataEditChangeActivity.isChecked = false
                viewDataEditChangeActivityPreview.isVisible = false
            }
            is DataEditChangeActivityState.Enabled -> {
                checkboxDataEditChangeActivity.isChecked = true
                viewDataEditChangeActivityPreview.isVisible = true
                viewDataEditChangeActivityPreview.apply {
                    itemColor = state.viewData.color
                    itemIcon = state.viewData.iconId
                    itemIconColor = state.viewData.iconColor
                    itemIconAlpha = state.viewData.iconAlpha
                    itemName = state.viewData.name
                }
            }
        }
    }

    private fun setChangeCommentState(
        state: DataEditChangeCommentState,
    ) = with(binding) {
        when (state) {
            is DataEditChangeCommentState.Disabled -> {
                checkboxDataEditChangeComment.isChecked = false
                inputDataEditChangeComment.isVisible = false
            }
            is DataEditChangeCommentState.Enabled -> {
                checkboxDataEditChangeComment.isChecked = true
                inputDataEditChangeComment.isVisible = true
                if (etDataEditChangeComment.text.toString() != state.viewData) {
                    etDataEditChangeComment.setText(state.viewData)
                }
            }
        }
    }

    private fun setAddTagState(
        state: DataEditAddTagsState,
    ) = with(binding) {
        when (state) {
            is DataEditAddTagsState.Disabled -> {
                checkboxDataEditAddTag.isChecked = false
                rvDataEditAddTagsPreview.isVisible = false
            }
            is DataEditAddTagsState.Enabled -> {
                checkboxDataEditAddTag.isChecked = true
                rvDataEditAddTagsPreview.isVisible = true
                addTagsPreviewAdapter.replace(state.viewData)
            }
        }
    }

    private fun setRemoveTagState(
        state: DataEditRemoveTagsState,
    ) = with(binding) {
        when (state) {
            is DataEditRemoveTagsState.Disabled -> {
                checkboxDataEditRemoveTag.isChecked = false
                rvDataEditRemoveTagsPreview.isVisible = false
            }
            is DataEditRemoveTagsState.Enabled -> {
                checkboxDataEditRemoveTag.isChecked = true
                rvDataEditRemoveTagsPreview.isVisible = true
                removeTagsPreviewAdapter.replace(state.viewData)
            }
        }
    }

    private fun setDeleteRecordsState(
        state: DataEditDeleteRecordsState,
    ) = with(binding) {
        when (state) {
            is DataEditDeleteRecordsState.Disabled -> {
                checkboxDataEditDeleteRecords.isChecked = false

                checkboxDataEditChangeActivity.isEnabled = true
                checkboxDataEditChangeComment.isEnabled = true
                checkboxDataEditAddTag.isEnabled = true
                checkboxDataEditRemoveTag.isEnabled = true
            }
            is DataEditDeleteRecordsState.Enabled -> {
                checkboxDataEditDeleteRecords.isChecked = true

                checkboxDataEditChangeActivity.isEnabled = false
                checkboxDataEditChangeComment.isEnabled = false
                checkboxDataEditAddTag.isEnabled = false
                checkboxDataEditRemoveTag.isEnabled = false
            }
        }
    }

    private fun setChangeButtonState(
        state: DataEditChangeButtonState,
    ) = with(binding) {
        btnDataEditChange.isEnabled = state.enabled
        btnDataEditChange.backgroundTintList = ColorStateList.valueOf(state.backgroundTint)
    }

    private fun disableButtons() = with(binding) {
        btnDataEditChange.isEnabled = false
        btnDataEditDeleteRecords.isEnabled = false
        btnDataEditDeleteData.isEnabled = false
    }
}
