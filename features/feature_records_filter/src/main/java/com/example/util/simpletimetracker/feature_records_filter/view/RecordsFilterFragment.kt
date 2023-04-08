package com.example.util.simpletimetracker.feature_records_filter.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.util.simpletimetracker.core.base.BaseFragment
import com.example.util.simpletimetracker.feature_base_adapter.BaseRecyclerAdapter
import com.example.util.simpletimetracker.feature_base_adapter.empty.createEmptyAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.loader.createLoaderAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.record.createRecordAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.recordFilter.createRecordFilterAdapterDelegate
import com.example.util.simpletimetracker.feature_records_filter.viewModel.RecordsFilterViewModel
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import dagger.hilt.android.AndroidEntryPoint
import com.example.util.simpletimetracker.feature_records_filter.databinding.RecordsFilterFragmentBinding as Binding

@AndroidEntryPoint
class RecordsFilterFragment : BaseFragment<Binding>() {

    override val inflater: (LayoutInflater, ViewGroup?, Boolean) -> Binding =
        Binding::inflate

    private val viewModel: RecordsFilterViewModel by viewModels()

    private val filtersAdapter: BaseRecyclerAdapter by lazy {
        BaseRecyclerAdapter(
            createRecordFilterAdapterDelegate(
                onClick = viewModel::onFilterClick,
                onRemoveClick = viewModel::onFilterRemoveClick,
            ),
            createLoaderAdapterDelegate(),
        )
    }
    private val recordsAdapter: BaseRecyclerAdapter by lazy {
        BaseRecyclerAdapter(
            createRecordAdapterDelegate(throttle(viewModel::onRecordClick)),
            createEmptyAdapterDelegate(),
            createLoaderAdapterDelegate()
        )
    }

    override fun initUi(): Unit = with(binding) {
        rvRecordsFilterFilters.apply {
            layoutManager = FlexboxLayoutManager(requireContext()).apply {
                flexDirection = FlexDirection.ROW
                justifyContent = JustifyContent.CENTER
                flexWrap = FlexWrap.WRAP
            }
            adapter = filtersAdapter
        }
        rvRecordsFilterList.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = recordsAdapter
        }
    }

    override fun initViewModel(): Unit = with(viewModel) {
        with(binding) {
            filtersViewData.observe(filtersAdapter::replace)
            recordsViewData.observe(recordsAdapter::replaceAsNew)
        }
    }
}
