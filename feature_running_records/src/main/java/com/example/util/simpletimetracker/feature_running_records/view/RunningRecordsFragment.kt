package com.example.util.simpletimetracker.feature_running_records.view

import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import com.example.util.simpletimetracker.core.adapter.BaseRecyclerAdapter
import com.example.util.simpletimetracker.core.adapter.divider.createDividerAdapterDelegate
import com.example.util.simpletimetracker.core.adapter.empty.createEmptyAdapterDelegate
import com.example.util.simpletimetracker.core.adapter.loader.createLoaderAdapterDelegate
import com.example.util.simpletimetracker.core.base.BaseFragment
import com.example.util.simpletimetracker.core.di.BaseViewModelFactory
import com.example.util.simpletimetracker.feature_running_records.R
import com.example.util.simpletimetracker.feature_running_records.adapter.createRunningRecordAdapterDelegate
import com.example.util.simpletimetracker.feature_running_records.adapter.createRunningRecordTypeAdapterDelegate
import com.example.util.simpletimetracker.feature_running_records.adapter.createRunningRecordTypeAddAdapterDelegate
import com.example.util.simpletimetracker.feature_running_records.di.RunningRecordsComponentProvider
import com.example.util.simpletimetracker.feature_running_records.viewModel.RunningRecordsViewModel
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import kotlinx.android.synthetic.main.running_records_fragment.rvRunningRecordsList
import javax.inject.Inject

class RunningRecordsFragment : BaseFragment(R.layout.running_records_fragment) {

    @Inject
    lateinit var viewModelFactory: BaseViewModelFactory<RunningRecordsViewModel>

    private val viewModel: RunningRecordsViewModel by viewModels(
        factoryProducer = { viewModelFactory }
    )

    private val runningRecordsAdapter: BaseRecyclerAdapter by lazy {
        BaseRecyclerAdapter(
            createLoaderAdapterDelegate(),
            createEmptyAdapterDelegate(),
            createDividerAdapterDelegate(),
            createRunningRecordAdapterDelegate(viewModel::onRunningRecordClick, viewModel::onRunningRecordLongClick),
            createRunningRecordTypeAdapterDelegate(viewModel::onRecordTypeClick, viewModel::onRecordTypeLongClick),
            createRunningRecordTypeAddAdapterDelegate(viewModel::onAddRecordTypeClick)
        )
    }

    override fun initDi() {
        (activity?.application as RunningRecordsComponentProvider)
            .runningRecordsComponent
            ?.inject(this)
    }

    override fun initUi() {
        parentFragment?.postponeEnterTransition()

        rvRunningRecordsList.apply {
            layoutManager = FlexboxLayoutManager(requireContext()).apply {
                flexDirection = FlexDirection.ROW
                justifyContent = JustifyContent.CENTER
                flexWrap = FlexWrap.WRAP
            }
            adapter = runningRecordsAdapter
            setHasFixedSize(true)

            viewTreeObserver.addOnPreDrawListener {
                parentFragment?.startPostponedEnterTransition()
                true
            }
        }
    }

    override fun initViewModel(): Unit = with(viewModel) {
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
