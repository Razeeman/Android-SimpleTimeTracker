package com.example.util.simpletimetracker.feature_running_records.view

import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.util.simpletimetracker.core.base.BaseFragment
import com.example.util.simpletimetracker.core.di.BaseViewModelFactory
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
import javax.inject.Inject

class RunningRecordsFragment : BaseFragment(R.layout.running_records_fragment) {

    @Inject
    lateinit var viewModelFactory: BaseViewModelFactory<RunningRecordsViewModel>

    private val viewModel: RunningRecordsViewModel by viewModels(
        factoryProducer = { viewModelFactory }
    )

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
        (activity?.application as RunningRecordsComponentProvider)
            .runningRecordsComponent
            ?.inject(this)
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

    override fun initViewModel(): Unit = with(viewModel) {
        recordTypes.observe(viewLifecycleOwner, recordTypesAdapter::replace)
        runningRecords.observe(viewLifecycleOwner, runningRecordsAdapter::replace)
    }

    override fun onResume() {
        super.onResume()
        viewModel.onVisible()
    }

    override fun onPause() {
        super.onPause()
        viewModel.onHidden()
    }

    companion object {
        fun newInstance() = RunningRecordsFragment()
    }
}
