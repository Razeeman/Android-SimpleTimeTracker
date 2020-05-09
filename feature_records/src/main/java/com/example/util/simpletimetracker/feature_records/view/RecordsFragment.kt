package com.example.util.simpletimetracker.feature_records.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.util.simpletimetracker.feature_records.R
import com.example.util.simpletimetracker.feature_records.adapter.RecordAdapter
import com.example.util.simpletimetracker.feature_records.di.RecordsComponentProvider
import com.example.util.simpletimetracker.feature_records.viewModel.RecordsViewModel
import kotlinx.android.synthetic.main.records_fragment.*

class RecordsFragment : Fragment() {

    private val viewModel: RecordsViewModel by viewModels()
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
            .provideRecordsComponent()
            ?.inject(viewModel)

        viewModel.records.observe(viewLifecycleOwner) {
            recordsAdapter.replace(it)
        }

        btnClear.setOnClickListener {
            viewModel.clearRecordTypes()
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.onVisible()
    }

    companion object {
        fun newInstance() = RecordsFragment()
    }
}
