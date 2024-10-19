package com.example.util.simpletimetracker.feature_running_records.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.example.util.simpletimetracker.core.base.BaseFragment
import com.example.util.simpletimetracker.core.di.BaseViewModelFactory
import com.example.util.simpletimetracker.core.dialog.OnTagSelectedListener
import com.example.util.simpletimetracker.core.sharedViewModel.MainTabsViewModel
import com.example.util.simpletimetracker.core.utils.InsetConfiguration
import com.example.util.simpletimetracker.core.utils.updateRunningRecordPreview
import com.example.util.simpletimetracker.domain.interactor.UpdateRunningRecordFromChangeScreenInteractor
import com.example.util.simpletimetracker.feature_base_adapter.BaseRecyclerAdapter
import com.example.util.simpletimetracker.feature_base_adapter.activityFilter.createActivityFilterAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.activityFilter.createActivityFilterAddAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.divider.createDividerAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.empty.createEmptyAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.hint.createHintAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.hintBig.createHintBigAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.loader.createLoaderAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.recordType.createRecordTypeAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.recordTypeSpecial.createRunningRecordTypeSpecialAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.recordWithHint.createRecordWithHintAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.runningRecord.createRunningRecordAdapterDelegate
import com.example.util.simpletimetracker.feature_running_records.viewModel.RunningRecordsViewModel
import com.example.util.simpletimetracker.feature_views.TransitionNames
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import com.example.util.simpletimetracker.feature_running_records.databinding.RunningRecordsFragmentBinding as Binding

@AndroidEntryPoint
class RunningRecordsFragment :
    BaseFragment<Binding>(),
    OnTagSelectedListener {

    override val inflater: (LayoutInflater, ViewGroup?, Boolean) -> Binding =
        Binding::inflate

    override var insetConfiguration: InsetConfiguration =
        InsetConfiguration.DoNotApply

    @Inject
    lateinit var mainTabsViewModelFactory: BaseViewModelFactory<MainTabsViewModel>

    private val viewModel: RunningRecordsViewModel by viewModels()
    private val mainTabsViewModel: MainTabsViewModel by activityViewModels(
        factoryProducer = { mainTabsViewModelFactory },
    )

    private val runningRecordsAdapter: BaseRecyclerAdapter by lazy {
        BaseRecyclerAdapter(
            createLoaderAdapterDelegate(),
            createEmptyAdapterDelegate(),
            createHintAdapterDelegate(),
            createHintBigAdapterDelegate(),
            createDividerAdapterDelegate(),
            createRunningRecordAdapterDelegate(
                transitionNamePrefix = TransitionNames.RUNNING_RECORD_FROM_MAIN,
                onItemClick = viewModel::onRunningRecordClick,
                onItemLongClick = viewModel::onRunningRecordLongClick,
            ),
            createRecordTypeAdapterDelegate(
                onItemClick = viewModel::onRecordTypeClick,
                onItemLongClick = viewModel::onRecordTypeLongClick,
                withTransition = true,
            ),
            createRecordWithHintAdapterDelegate(),
            createRunningRecordTypeSpecialAdapterDelegate(throttle(viewModel::onSpecialRecordTypeClick)),
            createActivityFilterAdapterDelegate(viewModel::onActivityFilterClick, viewModel::onActivityFilterLongClick),
            createActivityFilterAddAdapterDelegate(throttle(viewModel::onAddActivityFilterClick)),
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
            resetScreen.observe { resetScreen() }
            previewUpdate.observe(::onPreviewUpdate)
        }
        with(mainTabsViewModel) {
            tabReselected.observe(viewModel::onTabReselected)
            isNavBatAtTheBottom.observe(::updateInsetConfiguration)
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

    private fun resetScreen() = with(binding) {
        rvRunningRecordsList.smoothScrollToPosition(0)
        mainTabsViewModel.onHandled()
    }

    private fun onPreviewUpdate(update: UpdateRunningRecordFromChangeScreenInteractor.Update) {
        updateRunningRecordPreview(
            currentList = runningRecordsAdapter.currentList,
            recyclerView = binding.rvRunningRecordsList,
            update = update,
        )
    }

    private fun updateInsetConfiguration(isNavBatAtTheBottom: Boolean) {
        insetConfiguration = if (isNavBatAtTheBottom) {
            InsetConfiguration.DoNotApply
        } else {
            InsetConfiguration.ApplyToView { binding.rvRunningRecordsList }
        }
        initInsets()
    }

    companion object {
        fun newInstance() = RunningRecordsFragment()
    }
}
