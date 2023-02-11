package com.example.util.simpletimetracker.feature_records.view

import com.example.util.simpletimetracker.feature_records.databinding.RecordsFragmentBinding as Binding
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.util.simpletimetracker.core.base.BaseFragment
import com.example.util.simpletimetracker.core.di.BaseViewModelFactory
import com.example.util.simpletimetracker.core.sharedViewModel.MainTabsViewModel
import com.example.util.simpletimetracker.core.sharedViewModel.RemoveRecordViewModel
import com.example.util.simpletimetracker.domain.extension.orZero
import com.example.util.simpletimetracker.feature_base_adapter.BaseRecyclerAdapter
import com.example.util.simpletimetracker.feature_base_adapter.empty.createEmptyAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.hint.createHintAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.loader.createLoaderAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.record.createRecordAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.runningRecord.createRunningRecordAdapterDelegate
import com.example.util.simpletimetracker.feature_records.extra.RecordsExtra
import com.example.util.simpletimetracker.feature_records.viewModel.RecordsViewModel
import com.example.util.simpletimetracker.navigation.params.screen.RecordsParams
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class RecordsFragment : BaseFragment<Binding>() {

    override val inflater: (LayoutInflater, ViewGroup?, Boolean) -> Binding =
        Binding::inflate

    @Inject
    lateinit var removeRecordViewModelFactory: BaseViewModelFactory<RemoveRecordViewModel>

    @Inject
    lateinit var mainTabsViewModelFactory: BaseViewModelFactory<MainTabsViewModel>

    private val viewModel: RecordsViewModel by viewModels()
    private val removeRecordViewModel: RemoveRecordViewModel by activityViewModels(
        factoryProducer = { removeRecordViewModelFactory }
    )
    private val mainTabsViewModel: MainTabsViewModel by activityViewModels(
        factoryProducer = { mainTabsViewModelFactory }
    )
    private val recordsAdapter: BaseRecyclerAdapter by lazy {
        BaseRecyclerAdapter(
            createRunningRecordAdapterDelegate(viewModel::onRunningRecordClick),
            createRecordAdapterDelegate(viewModel::onRecordClick),
            createEmptyAdapterDelegate(),
            createLoaderAdapterDelegate(),
            createHintAdapterDelegate(),
        )
    }

    override fun initUi(): Unit = with(binding) {
        parentFragment?.postponeEnterTransition()

        rvRecordsList.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = recordsAdapter
        }

        setOnPreDrawListener {
            parentFragment?.startPostponedEnterTransition()
        }
    }

    override fun initUx() {
        binding.viewRecordsCalendar.setClickListener(viewModel::onCalendarClick)
    }

    override fun initViewModel() = with(binding) {
        with(viewModel) {
            extra = RecordsExtra(shift = arguments?.getInt(ARGS_POSITION).orZero())
            isCalendarView.observe(::switchState)
            records.observe(recordsAdapter::replace)
            calendarData.observe(binding.viewRecordsCalendar::setData)
            resetScreen.observe {
                rvRecordsList.smoothScrollToPosition(0)
                viewRecordsCalendar.reset()
                mainTabsViewModel.onHandled()
            }
        }
        with(removeRecordViewModel) {
            needUpdate.observe {
                if (it && this@RecordsFragment.isResumed) {
                    viewModel.onNeedUpdate()
                    removeRecordViewModel.onUpdated()
                }
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

    private fun switchState(isCalendarView: Boolean) = with(binding) {
        groupRecordsList.isVisible = !isCalendarView
        groupRecordsCalendar.isVisible = isCalendarView
    }

    companion object {
        private const val ARGS_POSITION = "args_position"

        fun newInstance(data: RecordsParams): RecordsFragment = RecordsFragment().apply {
            val bundle = Bundle()
            bundle.putInt(ARGS_POSITION, data.shift)
            arguments = bundle
        }
    }
}
