package com.example.util.simpletimetracker.feature_records_filter.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.util.simpletimetracker.core.base.BaseFragment
import com.example.util.simpletimetracker.core.dialog.DateTimeDialogListener
import com.example.util.simpletimetracker.core.extension.hideKeyboard
import com.example.util.simpletimetracker.core.utils.fragmentArgumentDelegate
import com.example.util.simpletimetracker.feature_base_adapter.BaseRecyclerAdapter
import com.example.util.simpletimetracker.feature_base_adapter.category.createCategoryAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.divider.createDividerAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.empty.createEmptyAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.hint.createHintAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.loader.createLoaderAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.record.createRecordAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.recordFilter.createRecordFilterAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.recordType.createRecordTypeAdapterDelegate
import com.example.util.simpletimetracker.feature_records_filter.adapter.createRecordsFilterCommentAdapterDelegate
import com.example.util.simpletimetracker.feature_records_filter.adapter.createRecordsFilterRangeAdapterDelegate
import com.example.util.simpletimetracker.feature_records_filter.model.RecordsFilterSelectedRecordsViewData
import com.example.util.simpletimetracker.feature_records_filter.viewModel.RecordsFilterViewModel
import com.example.util.simpletimetracker.feature_views.extension.setOnClick
import com.example.util.simpletimetracker.navigation.params.screen.RecordsFilterParams
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import dagger.hilt.android.AndroidEntryPoint
import com.example.util.simpletimetracker.feature_records_filter.databinding.RecordsFilterFragmentBinding as Binding

@AndroidEntryPoint
class RecordsFilterFragment :
    BaseFragment<Binding>(),
    DateTimeDialogListener {

    override val inflater: (LayoutInflater, ViewGroup?, Boolean) -> Binding =
        Binding::inflate

    private val viewModel: RecordsFilterViewModel by viewModels()

    private val filtersAdapter: BaseRecyclerAdapter by lazy {
        BaseRecyclerAdapter(
            createLoaderAdapterDelegate(),
            createRecordFilterAdapterDelegate(
                onClick = viewModel::onFilterClick,
                onRemoveClick = viewModel::onFilterRemoveClick,
            ),
        )
    }
    private val filterSelectionAdapter: BaseRecyclerAdapter by lazy {
        BaseRecyclerAdapter(
            createLoaderAdapterDelegate(),
            createHintAdapterDelegate(),
            createDividerAdapterDelegate(),
            createRecordTypeAdapterDelegate(viewModel::onRecordTypeClick),
            createCategoryAdapterDelegate(viewModel::onCategoryClick),
            createRecordsFilterCommentAdapterDelegate(viewModel::onCommentChange),
            createRecordsFilterRangeAdapterDelegate(
                viewModel::onRangeTimeStartedClick,
                viewModel::onRangeTimeEndedClick,
            ),
        )
    }
    private val recordsAdapter: BaseRecyclerAdapter by lazy {
        BaseRecyclerAdapter(
            createLoaderAdapterDelegate(),
            createEmptyAdapterDelegate(),
            createRecordAdapterDelegate(throttle(viewModel::onRecordClick))
        )
    }
    private val params: RecordsFilterParams by fragmentArgumentDelegate(
        key = ARGS_PARAMS, default = RecordsFilterParams()
    )

    override fun initUi(): Unit = with(binding) {
        rvRecordsFilterFilters.apply {
            layoutManager = FlexboxLayoutManager(requireContext()).apply {
                flexDirection = FlexDirection.ROW
                justifyContent = JustifyContent.CENTER
                flexWrap = FlexWrap.WRAP
            }
            adapter = filtersAdapter
        }
        rvRecordsFilterSelection.apply {
            layoutManager = FlexboxLayoutManager(requireContext()).apply {
                flexDirection = FlexDirection.ROW
                justifyContent = JustifyContent.CENTER
                flexWrap = FlexWrap.WRAP
            }
            adapter = filterSelectionAdapter
        }
        rvRecordsFilterList.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = recordsAdapter
        }
    }

    override fun initUx() = with(binding) {
        btnRecordsFilterSelect.setOnClick(viewModel::onFilterSelect)
        btnRecordsFilterApply.setOnClick(viewModel::onFilterApplied)
    }

    override fun initViewModel(): Unit = with(viewModel) {
        with(binding) {
            extra = params
            filtersViewData.observe(filtersAdapter::replace)
            filterSelectionContent.observe(filterSelectionAdapter::replace)
            recordsViewData.observe(::setSelectedRecords)
            filterSelectionVisibility.observe(groupRecordsFilterContent::isVisible::set)
            keyboardVisibility.observe(::showKeyboard)
        }
    }

    override fun onDateTimeSet(timestamp: Long, tag: String?) {
        viewModel.onDateTimeSet(timestamp, tag)
    }

    private fun setSelectedRecords(viewData: RecordsFilterSelectedRecordsViewData) {
        binding.tvRecordsFilterTitle.text = viewData.selectedRecordsCount
        recordsAdapter.replaceAsNew(viewData.recordsViewData)
    }

    private fun showKeyboard(visible: Boolean) {
        if (visible) {
            // Do nothing.
        } else {
            hideKeyboard()
        }
    }

    companion object {
        private const val ARGS_PARAMS = "args_params"

        fun createBundle(data: RecordsFilterParams): Bundle = Bundle().apply {
            putParcelable(ARGS_PARAMS, data)
        }
    }
}
