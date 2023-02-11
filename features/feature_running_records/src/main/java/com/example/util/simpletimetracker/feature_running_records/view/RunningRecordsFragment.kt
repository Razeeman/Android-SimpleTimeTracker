package com.example.util.simpletimetracker.feature_running_records.view

import com.example.util.simpletimetracker.feature_running_records.databinding.RunningRecordsFragmentBinding as Binding
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.example.util.simpletimetracker.core.base.BaseFragment
import com.example.util.simpletimetracker.core.di.BaseViewModelFactory
import com.example.util.simpletimetracker.core.dialog.OnTagSelectedListener
import com.example.util.simpletimetracker.core.sharedViewModel.MainTabsViewModel
import com.example.util.simpletimetracker.feature_base_adapter.BaseRecyclerAdapter
import com.example.util.simpletimetracker.feature_base_adapter.activityFilter.createActivityFilterAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.activityFilter.createActivityFilterAddAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.divider.createDividerAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.empty.createEmptyAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.loader.createLoaderAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.runningRecord.createRunningRecordAdapterDelegate
import com.example.util.simpletimetracker.feature_running_records.adapter.createRunningRecordTypeAdapterDelegate
import com.example.util.simpletimetracker.feature_running_records.adapter.createRunningRecordTypeAddAdapterDelegate
import com.example.util.simpletimetracker.feature_running_records.viewModel.RunningRecordsViewModel
import com.example.util.simpletimetracker.feature_views.TransitionNames
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class RunningRecordsFragment :
    BaseFragment<Binding>(),
    OnTagSelectedListener {

    override val inflater: (LayoutInflater, ViewGroup?, Boolean) -> Binding =
        Binding::inflate

    @Inject
    lateinit var mainTabsViewModelFactory: BaseViewModelFactory<MainTabsViewModel>

    private val viewModel: RunningRecordsViewModel by viewModels()
    private val mainTabsViewModel: MainTabsViewModel by activityViewModels(
        factoryProducer = { mainTabsViewModelFactory }
    )

    private val runningRecordsAdapter: BaseRecyclerAdapter by lazy {
        BaseRecyclerAdapter(
            createLoaderAdapterDelegate(),
            createEmptyAdapterDelegate(),
            createDividerAdapterDelegate(),
            createRunningRecordAdapterDelegate(
                transitionNamePrefix = TransitionNames.RUNNING_RECORD_FROM_MAIN,
                onItemClick = viewModel::onRunningRecordClick,
                onItemLongClick = viewModel::onRunningRecordLongClick
            ),
            createRunningRecordTypeAdapterDelegate(viewModel::onRecordTypeClick, viewModel::onRecordTypeLongClick),
            createRunningRecordTypeAddAdapterDelegate(viewModel::onAddRecordTypeClick),
            createActivityFilterAdapterDelegate(viewModel::onActivityFilterClick, viewModel::onActivityFilterLongClick),
            createActivityFilterAddAdapterDelegate(viewModel::onAddActivityFilterClick),
        )
    }

    override fun initUi(): Unit = with(binding) {
        parentFragment?.postponeEnterTransition()

        rvRunningRecordsList.apply {
            layoutManager = FlexboxLayoutManager(requireContext()).apply {
                flexDirection = FlexDirection.ROW
                justifyContent = JustifyContent.CENTER
                flexWrap = FlexWrap.WRAP
            }
            adapter = runningRecordsAdapter
            setHasFixedSize(true)
        }

        setOnPreDrawListener {
            parentFragment?.startPostponedEnterTransition()
        }
    }

    override fun initViewModel() = with(binding) {
        with(viewModel) {
            runningRecords.observe(runningRecordsAdapter::replace)
            resetScreen.observe {
                rvRunningRecordsList.smoothScrollToPosition(0)
                mainTabsViewModel.onHandled()
            }
        }
        with(mainTabsViewModel) {
            tabReselected.observe(viewModel::onTabReselected)
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.onVisible()
    }

    override fun onPause() {
        super.onPause()
        viewModel.onHidden()
    }

    override fun onTagSelected() {
        viewModel.onTagSelected()
    }

    companion object {
        fun newInstance() = RunningRecordsFragment()
    }
}
