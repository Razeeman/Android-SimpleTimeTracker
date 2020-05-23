package com.example.util.simpletimetracker.feature_running_records.view

import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.util.simpletimetracker.core.base.BaseFragment
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

class RunningRecordsFragment : BaseFragment(R.layout.running_records_fragment) {

    private val viewModel: RunningRecordsViewModel by viewModels()
    private val runningRecordsAdapter: RunningRecordAdapter by lazy {
        RunningRecordAdapter(
            viewModel::onRunningRecordClick
        )
    }
    private val recordTypesAdapter: RecordTypeAdapter by lazy {
        RecordTypeAdapter(
            viewModel::onRecordTypeClick,
            viewModel::onRecordTypeLongClick,
            viewModel::onAddRecordTypeClick
        )
    }

    override fun initDi() {
        val component = (activity?.application as RunningRecordsComponentProvider)
            .runningRecordsComponent

        component?.inject(viewModel)
    }

    override fun initUi() {
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
    }

    override fun initViewModel() {
        viewModel.recordTypes.observe(viewLifecycleOwner, recordTypesAdapter::replace)
        viewModel.runningRecords.observe(viewLifecycleOwner, runningRecordsAdapter::replace)
    }

    override fun onResume() {
        super.onResume()
        viewModel.onVisible()
    }

    companion object {
        fun newInstance() = RunningRecordsFragment()
    }
}
