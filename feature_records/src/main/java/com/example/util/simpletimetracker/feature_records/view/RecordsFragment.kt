package com.example.util.simpletimetracker.feature_records.view

import android.os.Bundle
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.util.simpletimetracker.core.base.BaseFragment
import com.example.util.simpletimetracker.domain.extension.orZero
import com.example.util.simpletimetracker.feature_records.R
import com.example.util.simpletimetracker.feature_records.adapter.RecordAdapter
import com.example.util.simpletimetracker.feature_records.di.RecordsComponentProvider
import com.example.util.simpletimetracker.feature_records.viewModel.RecordsViewModel
import com.example.util.simpletimetracker.feature_records.viewModel.RecordsViewModelFactory
import com.example.util.simpletimetracker.navigation.params.RecordsParams
import kotlinx.android.synthetic.main.records_fragment.*

class RecordsFragment : BaseFragment(R.layout.records_fragment) {

    private val viewModel: RecordsViewModel by viewModels(
        factoryProducer = {
            RecordsViewModelFactory(
                rangeStart = arguments?.getLong(ARGS_RANGE_START).orZero(),
                rangeEnd = arguments?.getLong(ARGS_RANGE_END).orZero()
            )
        }
    )
    private val recordsAdapter: RecordAdapter by lazy {
        RecordAdapter(
            viewModel::onRecordClick
        )
    }

    override fun initDi() {
        val component = (activity?.application as RecordsComponentProvider)
            .recordsComponent

        component?.inject(viewModel)
    }

    override fun initUi() {
        rvRecordsList.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = recordsAdapter
        }
    }

    override fun initViewModel() {
        viewModel.records.observe(viewLifecycleOwner, recordsAdapter::replace)
    }

    override fun onResume() {
        super.onResume()
        viewModel.onVisible()
    }

    companion object {
        private const val ARGS_RANGE_START = "args_range_start"
        private const val ARGS_RANGE_END = "args_range_end"

        fun newInstance(data: Any?): RecordsFragment = RecordsFragment().apply {
            val bundle = Bundle()
            when (data) {
                is RecordsParams -> {
                    bundle.putLong(ARGS_RANGE_START, data.rangeStart)
                    bundle.putLong(ARGS_RANGE_END, data.rangeEnd)
                }
            }
            arguments = bundle
        }
    }
}
