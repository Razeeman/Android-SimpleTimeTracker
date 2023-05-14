package com.example.util.simpletimetracker.feature_records_filter.view

import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.util.simpletimetracker.core.base.BaseBottomSheetFragment
import com.example.util.simpletimetracker.core.dialog.DateTimeDialogListener
import com.example.util.simpletimetracker.core.dialog.RecordsFilterListener
import com.example.util.simpletimetracker.core.extension.blockContentScroll
import com.example.util.simpletimetracker.core.extension.findListener
import com.example.util.simpletimetracker.core.extension.hideKeyboard
import com.example.util.simpletimetracker.core.extension.setFullScreen
import com.example.util.simpletimetracker.core.extension.setSkipCollapsed
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
import com.example.util.simpletimetracker.feature_base_adapter.recordsDateDivider.createRecordsDateDividerAdapterDelegate
import com.example.util.simpletimetracker.feature_records_filter.adapter.createRecordsFilterButtonAdapterDelegate
import com.example.util.simpletimetracker.feature_records_filter.adapter.createRecordsFilterCommentAdapterDelegate
import com.example.util.simpletimetracker.feature_records_filter.adapter.createRecordsFilterRangeAdapterDelegate
import com.example.util.simpletimetracker.feature_records_filter.model.RecordsFilterSelectedRecordsViewData
import com.example.util.simpletimetracker.feature_records_filter.viewModel.RecordsFilterViewModel
import com.example.util.simpletimetracker.navigation.params.screen.RecordsFilterParams
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import dagger.hilt.android.AndroidEntryPoint
import com.example.util.simpletimetracker.feature_records_filter.databinding.RecordsFilterFragmentBinding as Binding

@AndroidEntryPoint
class RecordsFilterFragment :
    BaseBottomSheetFragment<Binding>(),
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
            createRecordsDateDividerAdapterDelegate(),
            createRecordTypeAdapterDelegate(viewModel::onRecordTypeClick),
            createCategoryAdapterDelegate(viewModel::onCategoryClick),
            createRecordAdapterDelegate(viewModel::onRecordClick),
            createRecordsFilterCommentAdapterDelegate(viewModel::onCommentChange),
            createRecordsFilterButtonAdapterDelegate(viewModel::onInnerFilterButtonClick),
            createRecordsFilterRangeAdapterDelegate(
                viewModel::onRangeTimeStartedClick,
                viewModel::onRangeTimeEndedClick,
            ),
            createRecordFilterAdapterDelegate(
                onClick = viewModel::onInnerFilterClick,
                onRemoveClick = {},
            ),
        )
    }
    private val recordsAdapter: BaseRecyclerAdapter by lazy {
        BaseRecyclerAdapter(
            createLoaderAdapterDelegate(),
            createEmptyAdapterDelegate(),
            createRecordsDateDividerAdapterDelegate(),
            createRecordAdapterDelegate(viewModel::onRecordClick)
        )
    }
    private val params: RecordsFilterParams by fragmentArgumentDelegate(
        key = ARGS_PARAMS, default = RecordsFilterParams()
    )
    private var listener: RecordsFilterListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = context.findListener()
    }

    override fun initDialog() {
        setSkipCollapsed()
        setFullScreen()
        blockContentScroll(binding.rvRecordsFilterList)
        blockContentScroll(binding.rvRecordsFilterSelection)
    }

    override fun initUi(): Unit = with(binding) {
        rvRecordsFilterFilters.isNestedScrollingEnabled = false
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

    override fun initViewModel(): Unit = with(viewModel) {
        with(binding) {
            init(params)
            filtersViewData.observe(filtersAdapter::replace)
            filterSelectionContent.observe(filterSelectionAdapter::replace)
            recordsViewData.observe(::setSelectedRecords)
            filterSelectionVisibility.observe(::setFilterSelectionVisibility)
            keyboardVisibility.observe(::showKeyboard)
            changedFilters.observe { listener?.onFilterChanged(it) }
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        listener?.onFilterDismissed(params.tag)
        super.onDismiss(dialog)
    }

    override fun onDateTimeSet(timestamp: Long, tag: String?) {
        viewModel.onDateTimeSet(timestamp, tag)
    }

    private fun setFilterSelectionVisibility(filterOpened: Boolean) = with(binding) {
        rvRecordsFilterList.isVisible = !filterOpened
        rvRecordsFilterList.isNestedScrollingEnabled = !filterOpened
        rvRecordsFilterSelection.isVisible = filterOpened
        rvRecordsFilterSelection.isNestedScrollingEnabled = filterOpened
    }

    private fun setSelectedRecords(viewData: RecordsFilterSelectedRecordsViewData) = with(binding) {
        loaderRecordsFilter.isVisible = viewData.isLoading
        tvRecordsFilterTitle.isInvisible = viewData.isLoading
        tvRecordsFilterTitle.text = viewData.selectedRecordsCount
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
