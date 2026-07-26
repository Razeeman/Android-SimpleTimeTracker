package com.example.util.simpletimetracker.feature_tag_selection.view

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.WindowManager
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.viewModels
import com.example.util.simpletimetracker.core.base.BaseFragment
import com.example.util.simpletimetracker.feature_dialogs.api.OnTagSelectedListener
import com.example.util.simpletimetracker.feature_dialogs.api.OnTagValueSelectedListener
import com.example.util.simpletimetracker.core.extension.findListeners
import com.example.util.simpletimetracker.core.manager.KeyboardVisibilityManager
import com.example.util.simpletimetracker.core.utils.InsetConfiguration
import com.example.util.simpletimetracker.core.utils.fragmentArgumentDelegate
import com.example.util.simpletimetracker.feature_base_adapter.BaseRecyclerAdapter
import com.example.util.simpletimetracker.feature_base_adapter.category.createCategoryAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.category.createCategoryAddAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.commentField.createCommentFieldAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.divider.createDividerAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.empty.createEmptyAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.emptySpace.createEmptySpaceAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.hint.createHintAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.info.createInfoAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.loader.createLoaderAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.recordComment.createRecordCommentAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.recordFilter.createFilterAdapterDelegate
import com.example.util.simpletimetracker.feature_tag_selection.adapter.createRecordTagSelectionTextAdapterDelegate
import com.example.util.simpletimetracker.feature_tag_selection.viewModel.RecordTagSelectionViewModel
import com.example.util.simpletimetracker.feature_views.extension.safeUpdateLayoutParams
import com.example.util.simpletimetracker.feature_views.extension.setOnClick
import com.example.util.simpletimetracker.feature_views.extension.visible
import com.example.util.simpletimetracker.navigation.params.screen.ARGS_PARAMS
import com.example.util.simpletimetracker.navigation.params.screen.RecordTagSelectionParams
import com.example.util.simpletimetracker.navigation.params.screen.RecordTagValueSelectionParams
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import dagger.hilt.android.AndroidEntryPoint
import com.example.util.simpletimetracker.feature_tag_selection.databinding.RecordTagSelectionFragmentBinding as Binding

@AndroidEntryPoint
class RecordTagSelectionFragment :
    BaseFragment<Binding>(),
    OnTagValueSelectedListener {

    override val inflater: (LayoutInflater, ViewGroup?, Boolean) -> Binding =
        Binding::inflate

    override var insetConfiguration: InsetConfiguration =
        InsetConfiguration.DoNotApply

    private val viewModel: RecordTagSelectionViewModel by viewModels()

    private val adapter: BaseRecyclerAdapter by lazy {
        BaseRecyclerAdapter(
            createLoaderAdapterDelegate(),
            createCategoryAdapterDelegate(viewModel::onCategoryClick),
            createCategoryAddAdapterDelegate(viewModel::onCategorySpecialClick),
            createDividerAdapterDelegate(),
            createInfoAdapterDelegate(),
            createHintAdapterDelegate(),
            createEmptyAdapterDelegate(),
            createEmptySpaceAdapterDelegate(),
            createCommentFieldAdapterDelegate(afterTextChangeWithViewData = viewModel::onCommentChange),
            createRecordTagSelectionTextAdapterDelegate(),
            createRecordCommentAdapterDelegate(viewModel::onCommentClick),
            createFilterAdapterDelegate(viewModel::onCommentFilterClick),
        )
    }
    private val params: RecordTagSelectionParams by fragmentArgumentDelegate(
        key = ARGS_PARAMS, default = RecordTagSelectionParams.Empty,
    )
    private var listeners: List<OnTagSelectedListener> = mutableListOf()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listeners = context.findListeners<OnTagSelectedListener>()
    }

    override fun initUi(): Unit = with(binding) {
        rvRecordTagSelectionList.apply {
            layoutManager = FlexboxLayoutManager(requireContext()).apply {
                flexDirection = FlexDirection.ROW
                justifyContent = JustifyContent.CENTER
                flexWrap = FlexWrap.WRAP
            }
            adapter = this@RecordTagSelectionFragment.adapter
        }
    }

    override fun initUx() = with(binding) {
        KeyboardVisibilityManager.addObserver(this@RecordTagSelectionFragment, ::onKeyboardVisible)
        btnRecordTagSelectionSave.setOnClick(viewModel::onSaveClick)
    }

    override fun initViewModel(): Unit = with(viewModel) {
        extra = params
        saveSoftInputMode()
        viewData.observe(adapter::replace)
        saveButtonVisibility.observe {
            binding.dividerTypesSelectionBottom.visible = it
            binding.btnRecordTagSelectionSave.visible = it
        }
        saveClicked.observe { onTagSelected() }
    }

    override fun onDestroy() {
        restoreSoftInputMode()
        KeyboardVisibilityManager.removeObserver(this@RecordTagSelectionFragment)
        super.onDestroy()
    }

    override fun onTagValueSelected(params: RecordTagValueSelectionParams, data: Double) {
        viewModel.onCategoryValueSelected(params, data)
    }

    private fun onTagSelected() {
        listeners.forEach { it.onTagSelected() }
    }

    private fun onKeyboardVisible(isVisible: Boolean) {
        binding.rvRecordTagSelectionList.safeUpdateLayoutParams<ConstraintLayout.LayoutParams> {
            verticalBias = if (isVisible) 0.0f else 0.5f
        }
    }

    private fun saveSoftInputMode() {
        activity?.window?.attributes?.softInputMode?.let { viewModel.softInputMode = it }
        activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
    }

    private fun restoreSoftInputMode() {
        viewModel.softInputMode?.let { activity?.window?.setSoftInputMode(it) }
    }

    companion object {
        fun newInstance(data: RecordTagSelectionParams) = RecordTagSelectionFragment().apply {
            arguments = Bundle().apply {
                putParcelable(ARGS_PARAMS, data)
            }
        }
    }
}
