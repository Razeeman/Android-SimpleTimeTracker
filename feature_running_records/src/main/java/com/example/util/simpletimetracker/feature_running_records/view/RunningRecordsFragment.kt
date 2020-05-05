package com.example.util.simpletimetracker.feature_running_records.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.util.simpletimetracker.feature_running_records.R
import com.example.util.simpletimetracker.feature_running_records.adapter.recordType.RecordTypeAdapter
import com.example.util.simpletimetracker.feature_running_records.adapter.runningRecord.RunningRecordAdapter
import com.example.util.simpletimetracker.feature_running_records.di.RunningRecordsComponentProvider
import com.example.util.simpletimetracker.feature_running_records.viewModel.RunningRecordsViewModel
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import kotlinx.android.synthetic.main.running_records_fragment.*

class RunningRecordsFragment : Fragment() {

    private val viewModel: RunningRecordsViewModel by viewModels()
    private val runningRecordsAdapter: RunningRecordAdapter by lazy {
        RunningRecordAdapter(
            viewModel::onRunningRecordClick
        )
    }
    private val recordTypesAdapter: RecordTypeAdapter by lazy {
        RecordTypeAdapter(
            viewModel::onRecordTypeClick,
            viewModel::onAddRecordTypeClick
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(
            R.layout.running_records_fragment,
            container,
            false
        )
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        rvRunningRecordsList.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = runningRecordsAdapter
        }
        rvRecordTypesList.apply {
            layoutManager = FlexboxLayoutManager(requireContext()).apply {
                flexDirection = FlexDirection.ROW
                justifyContent = JustifyContent.CENTER
                flexWrap = FlexWrap.WRAP
            }
            adapter = recordTypesAdapter
        }

        (activity?.application as RunningRecordsComponentProvider)
            .provideRunningRecordsComponent()
            ?.inject(viewModel)

        viewModel.recordTypes.observe(viewLifecycleOwner) {
            recordTypesAdapter.replace(it)
        }
        viewModel.runningRecords.observe(viewLifecycleOwner) {
            runningRecordsAdapter.replace(it)
        }

        btnClear.setOnClickListener {
            viewModel.clearRecordTypes()
        }
    }

    companion object {
        fun newInstance() = RunningRecordsFragment()
    }
}
