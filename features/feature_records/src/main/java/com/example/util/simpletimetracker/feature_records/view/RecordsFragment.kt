package com.example.util.simpletimetracker.feature_records.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.util.simpletimetracker.core.base.BaseFragment
import com.example.util.simpletimetracker.core.di.BaseViewModelFactory
import com.example.util.simpletimetracker.core.dialog.ChartFilterDialogListener
import com.example.util.simpletimetracker.core.dialog.RecordQuickActionDialogListener
import com.example.util.simpletimetracker.core.sharedViewModel.MainTabsViewModel
import com.example.util.simpletimetracker.core.sharedViewModel.RemoveRecordViewModel
import com.example.util.simpletimetracker.core.utils.InsetConfiguration
import com.example.util.simpletimetracker.domain.extension.orZero
import com.example.util.simpletimetracker.feature_base_adapter.BaseRecyclerAdapter
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.empty.createEmptyAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.hint.createHintAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.hintBig.createHintBigAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.loader.createLoaderAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.record.createRecordAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.runningRecord.createRunningRecordAdapterDelegate
import com.example.util.simpletimetracker.feature_records.R
import com.example.util.simpletimetracker.feature_records.databinding.RecordsFragmentShareBinding
import com.example.util.simpletimetracker.feature_records.extra.RecordsExtra
import com.example.util.simpletimetracker.feature_records.model.RecordsShareState
import com.example.util.simpletimetracker.feature_records.model.RecordsState
import com.example.util.simpletimetracker.feature_records.viewModel.RecordsViewModel
import com.example.util.simpletimetracker.feature_views.TransitionNames
import com.example.util.simpletimetracker.feature_views.extension.animateAlpha
import com.example.util.simpletimetracker.feature_views.extension.getThemedAttr
import com.example.util.simpletimetracker.feature_views.extension.visible
import com.example.util.simpletimetracker.navigation.params.screen.RecordsParams
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import com.example.util.simpletimetracker.feature_records.databinding.RecordsFragmentBinding as Binding

@AndroidEntryPoint
class RecordsFragment :
    BaseFragment<Binding>(),
    RecordQuickActionDialogListener,
    ChartFilterDialogListener {

    override val inflater: (LayoutInflater, ViewGroup?, Boolean) -> Binding =
        Binding::inflate

    override val insetConfiguration: InsetConfiguration =
        InsetConfiguration.DoNotApply

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
    private val recordsAdapter: BaseRecyclerAdapter by lazy { buildAdapter() }

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
        binding.viewRecordsCalendar.root.setClickListener(throttle(viewModel::onCalendarClick))
        binding.viewRecordsCalendar.root.setLongClickListener(throttle(viewModel::onCalendarLongClick))
    }

    override fun initViewModel() = with(binding) {
        with(viewModel) {
            extra = RecordsExtra(shift = arguments?.getInt(ARGS_POSITION).orZero())
            isCalendarView.observe(::switchState)
            records.observe(::setRecordsState)
            calendarData.observe(::setCalendarState)
            resetScreen.observe { resetScreen() }
            sharingData.observe(::onNewSharingData)
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

    override fun onChartFilterDialogDismissed() {
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
                viewRecordsCalendar.root.alpha = 0f
            }
            is RecordsState.CalendarData.Data -> {
                loaderRecordsCalendar.root.animateAlpha(isVisible = false, duration = 200)
                viewRecordsCalendar.root.animateAlpha(isVisible = true, duration = 100)
                viewRecordsCalendar.root.setData(state.data)
            }
        }
    }

    private fun resetScreen() = with(binding) {
        rvRecordsList.smoothScrollToPosition(0)
        viewRecordsCalendar.root.reset()
        mainTabsViewModel.onHandled()
    }

    private fun onNewSharingData(data: RecordsShareState) {
        val context = binding.root.context
        val view = RecordsFragmentShareBinding.inflate(layoutInflater)
        context.getThemedAttr(R.attr.appBackgroundColor).let(view.root::setBackgroundColor)
        val originalCalendar = binding.viewRecordsCalendar.root
        view.tvRecordsShareTitle.visible = true
        view.viewRecordsShareDivider.visible = true
        view.tvRecordsShareTitle.text = data.shareTitle
        view.rvRecordsList.visible = false
        view.viewRecordsCalendar.root.visible = false

        when (data.state) {
            is RecordsShareState.State.Records -> {
                view.rvRecordsList.apply {
                    layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
                    visible = true
                    val adapter = buildAdapter()
                    adapter.replace(data.state.data)
                    layoutManager = LinearLayoutManager(context)
                    this.adapter = adapter
                }
            }
            is RecordsShareState.State.Calendar -> {
                view.viewRecordsCalendar.root.apply {
                    visible = true
                    layoutParams.height = originalCalendar.height
                    setData(data.state.data)
                    setScaleState(originalCalendar.getScaleState())
                }
            }
        }

        viewModel.onShareView(view.root)
    }

    private fun buildAdapter(): BaseRecyclerAdapter {
        return BaseRecyclerAdapter(
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

    companion object {
        private const val ARGS_POSITION = "args_position"

        fun newInstance(data: RecordsParams): RecordsFragment = RecordsFragment().apply {
            val bundle = Bundle()
            bundle.putInt(ARGS_POSITION, data.shift)
            arguments = bundle
        }
    }
}
