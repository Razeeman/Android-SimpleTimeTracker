package com.example.util.simpletimetracker.feature_tag_selection.view

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import com.example.util.simpletimetracker.core.base.BaseFragment
import com.example.util.simpletimetracker.core.dialog.OnTagSelectedListener
import com.example.util.simpletimetracker.core.extension.findListeners
import com.example.util.simpletimetracker.core.utils.InsetConfiguration
import com.example.util.simpletimetracker.core.utils.fragmentArgumentDelegate
import com.example.util.simpletimetracker.feature_base_adapter.BaseRecyclerAdapter
import com.example.util.simpletimetracker.feature_base_adapter.category.createCategoryAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.divider.createDividerAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.empty.createEmptyAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.info.createInfoAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.loader.createLoaderAdapterDelegate
import com.example.util.simpletimetracker.feature_tag_selection.viewData.RecordTagSelectionViewState
import com.example.util.simpletimetracker.feature_tag_selection.viewModel.RecordTagSelectionViewModel
import com.example.util.simpletimetracker.feature_views.extension.setOnClick
import com.example.util.simpletimetracker.feature_views.extension.visible
import com.example.util.simpletimetracker.navigation.params.screen.RecordTagSelectionParams
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import dagger.hilt.android.AndroidEntryPoint
import com.example.util.simpletimetracker.feature_tag_selection.databinding.RecordTagSelectionFragmentBinding as Binding

@AndroidEntryPoint
class RecordTagSelectionFragment : BaseFragment<Binding>() {

    override val inflater: (LayoutInflater, ViewGroup?, Boolean) -> Binding =
        Binding::inflate

    override var insetConfiguration: InsetConfiguration =
        InsetConfiguration.DoNotApply

    private val viewModel: RecordTagSelectionViewModel by viewModels()

    private val adapter: BaseRecyclerAdapter by lazy {
        BaseRecyclerAdapter(
            createLoaderAdapterDelegate(),
            createCategoryAdapterDelegate(viewModel::onCategoryClick),
            createDividerAdapterDelegate(),
            createInfoAdapterDelegate(),
            createEmptyAdapterDelegate(),
        )
    }
    private val params: RecordTagSelectionParams by fragmentArgumentDelegate(
        key = ARGS_PARAMS, default = RecordTagSelectionParams.Empty,
    )
    private val listeners: MutableList<OnTagSelectedListener> = mutableListOf()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listeners += context.findListeners<OnTagSelectedListener>()
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
        etRecordTagSelectionCommentItem.doAfterTextChanged { viewModel.onCommentChange(it.toString()) }
        btnRecordTagSelectionSave.setOnClick(viewModel::onSaveClick)
    }

    override fun initViewModel(): Unit = with(viewModel) {
        extra = params
        viewData.observe(adapter::replace)
        saveButtonVisibility.observe(binding.btnRecordTagSelectionSave::visible::set)
        viewState.observe(::setState)
        saveClicked.observe { onTagSelected() }
    }

    private fun setState(data: RecordTagSelectionViewState) = with(binding) {
        val showComment = RecordTagSelectionViewState.Field.Comment in data.fields
        tvRecordTagSelectionCommentHint.isVisible = showComment
        inputRecordTagSelectionComment.isVisible = showComment

        val showTags = RecordTagSelectionViewState.Field.Tags in data.fields
        tvRecordTagSelectionTagHint.isVisible = showTags
        rvRecordTagSelectionList.isVisible = showTags
    }

    private fun onTagSelected() {
        listeners.forEach { it.onTagSelected() }
    }

    companion object {
        private const val ARGS_PARAMS = "args_params"

        fun newInstance(data: RecordTagSelectionParams) = RecordTagSelectionFragment().apply {
            arguments = Bundle().apply {
                putParcelable(ARGS_PARAMS, data)
            }
        }
    }
}
