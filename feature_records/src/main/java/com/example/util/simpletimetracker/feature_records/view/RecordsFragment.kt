package com.example.util.simpletimetracker.feature_records.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.util.simpletimetracker.domain.extension.orZero
import com.example.util.simpletimetracker.feature_records.R
import com.example.util.simpletimetracker.feature_records.adapter.RecordAdapter
import com.example.util.simpletimetracker.feature_records.di.RecordsComponentProvider
import com.example.util.simpletimetracker.feature_records.viewModel.RecordsViewModel
import com.example.util.simpletimetracker.feature_records.viewModel.RecordsViewModelFactory
import com.example.util.simpletimetracker.navigation.params.RecordsParams
import kotlinx.android.synthetic.main.records_fragment.*

class RecordsFragment : Fragment() {

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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(
            R.layout.records_fragment,
            container,
            false
        )
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        rvRecordsList.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = recordsAdapter
        }

        (activity?.application as RecordsComponentProvider)
            .recordsComponent?.inject(viewModel)

        viewModel.records.observe(viewLifecycleOwner) {
            recordsAdapter.replace(it)
        }
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
