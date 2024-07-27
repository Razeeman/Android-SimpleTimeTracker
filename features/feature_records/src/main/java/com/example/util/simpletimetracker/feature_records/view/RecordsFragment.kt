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
import com.example.util.simpletimetracker.core.dialog.RecordQuickActionDialogListener
import com.example.util.simpletimetracker.core.sharedViewModel.MainTabsViewModel
import com.example.util.simpletimetracker.core.sharedViewModel.RemoveRecordViewModel
import com.example.util.simpletimetracker.domain.extension.orZero
import com.example.util.simpletimetracker.feature_base_adapter.BaseRecyclerAdapter
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.empty.createEmptyAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.hint.createHintAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.hintBig.createHintBigAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.loader.createLoaderAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.record.createRecordAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.runningRecord.createRunningRecordAdapterDelegate
import com.example.util.simpletimetracker.feature_records.extra.RecordsExtra
import com.example.util.simpletimetracker.feature_records.model.RecordsState
import com.example.util.simpletimetracker.feature_records.viewModel.RecordsViewModel
import com.example.util.simpletimetracker.feature_views.TransitionNames
import com.example.util.simpletimetracker.feature_views.extension.animateAlpha
import com.example.util.simpletimetracker.navigation.params.screen.RecordsParams
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class RecordsFragment :
    BaseFragment<Binding>(),
    RecordQuickActionDialogListener {

    override val inflater: (LayoutInflater, ViewGroup?, Boolean) -> Binding =
        Binding::inflate

    @Inject
    lateinit var removeRecordViewModelFactory: BaseViewModelFactory<RemoveRecordViewModel>

    @Inject
    lateinit var mainTabsViewModelFactory: BaseViewModelFactory<MainTabsViewModel>

    private val viewModel: RecordsViewModel by viewModels()
    private val removeRecordViewModel: RemoveRecordViewModel by activityViewModels(
        factoryProducer = { removeRecordViewModelFactory },
    )
    private val mainTabsViewModel: MainTabsViewModel by activityViewModels(
        factoryProducer = { mainTabsViewModelFactory },
    )
    private val recordsAdapter: BaseRecyclerAdapter by lazy {
        BaseRecyclerAdapter(
            createRunningRecordAdapterDelegate(
                transitionNamePrefix = TransitionNames.RUNNING_RECORD_FROM_RECORDS,
                onItemClick = throttle(viewModel::onRunningRecordClick),
                onItemLongClick = throttle(viewModel::onRunningRecordLongClick),
            ),
            createRecordAdapterDelegate(
                onItemClick = throttle(viewModel::onRecordClick),
                onItemLongClick = throttle(viewModel::onRecordLongClick),
            ),
            createEmptyAdapterDelegate(),
            createLoaderAdapterDelegate(),
            createHintAdapterDelegate(),
            createHintBigAdapterDelegate(),
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
        binding.viewRecordsCalendar.setClickListener(throttle(viewModel::onCalendarClick))
        binding.viewRecordsCalendar.setLongClickListener(throttle(viewModel::onCalendarLongClick))
    }

    override fun initViewModel() = with(binding) {
        with(viewModel) {
            extra = RecordsExtra(shift = arguments?.getInt(ARGS_POSITION).orZero())
            isCalendarView.observe(::switchState)
            records.observe(::setRecordsState)
            calendarData.observe(::setCalendarState)
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

    override fun onActionComplete() {
        viewModel.onNeedUpdate()
    }

    private fun switchState(isCalendarView: Boolean) = with(binding) {
        groupRecordsList.isVisible = !isCalendarView
        groupRecordsCalendar.isVisible = isCalendarView
    }

    private fun setRecordsState(
        state: List<ViewHolderType>,
    ) {
        recordsAdapter.replace(state)
    }

    private fun setCalendarState(
        state: RecordsState.CalendarData,
    ) = with(binding) {
        when (state) {
            is RecordsState.CalendarData.Loading -> {
                loaderRecordsCalendar.root.alpha = 1f
                viewRecordsCalendar.alpha = 0f
            }
            is RecordsState.CalendarData.Data -> {
                loaderRecordsCalendar.root.animateAlpha(isVisible = false, duration = 200)
                viewRecordsCalendar.animateAlpha(isVisible = true, duration = 100)
                viewRecordsCalendar.setData(state.data)
            }
        }
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
