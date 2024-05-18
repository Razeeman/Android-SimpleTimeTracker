package com.example.util.simpletimetracker.feature_statistics.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.util.simpletimetracker.core.base.BaseFragment
import com.example.util.simpletimetracker.core.di.BaseViewModelFactory
import com.example.util.simpletimetracker.core.dialog.ChartFilterDialogListener
import com.example.util.simpletimetracker.core.repo.DeviceRepo
import com.example.util.simpletimetracker.core.sharedViewModel.MainTabsViewModel
import com.example.util.simpletimetracker.domain.extension.orZero
import com.example.util.simpletimetracker.feature_base_adapter.BaseRecyclerAdapter
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.divider.createDividerAdapterDelegate
import com.example.util.simpletimetracker.feature_views.extension.getThemedAttr
import com.example.util.simpletimetracker.feature_base_adapter.hint.createHintAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.hintBig.createHintBigAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.loader.createLoaderAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.statistics.createStatisticsAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.statisticsGoal.createStatisticsGoalAdapterDelegate
import com.example.util.simpletimetracker.feature_statistics.R
import com.example.util.simpletimetracker.feature_statistics.adapter.createStatisticsChartAdapterDelegate
import com.example.util.simpletimetracker.feature_statistics.adapter.createStatisticsEmptyAdapterDelegate
import com.example.util.simpletimetracker.feature_statistics.adapter.createStatisticsInfoAdapterDelegate
import com.example.util.simpletimetracker.feature_statistics.adapter.createStatisticsTitleAdapterDelegate
import com.example.util.simpletimetracker.feature_statistics.extra.StatisticsExtra
import com.example.util.simpletimetracker.feature_statistics.viewData.StatisticsChartViewData
import com.example.util.simpletimetracker.feature_statistics.viewModel.StatisticsSettingsViewModel
import com.example.util.simpletimetracker.feature_statistics.viewModel.StatisticsViewModel
import com.example.util.simpletimetracker.feature_views.pieChart.PieChartView
import com.example.util.simpletimetracker.navigation.params.screen.StatisticsParams
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import com.example.util.simpletimetracker.feature_statistics.databinding.StatisticsFragmentBinding as Binding

@AndroidEntryPoint
class StatisticsFragment :
    BaseFragment<Binding>(),
    ChartFilterDialogListener {

    override val inflater: (LayoutInflater, ViewGroup?, Boolean) -> Binding =
        Binding::inflate

    @Inject
    lateinit var settingsViewModelFactory: BaseViewModelFactory<StatisticsSettingsViewModel>

    @Inject
    lateinit var mainTabsViewModelFactory: BaseViewModelFactory<MainTabsViewModel>

    @Inject
    lateinit var deviceRepo: DeviceRepo

    private val viewModel: StatisticsViewModel by viewModels()
    private val settingsViewModel: StatisticsSettingsViewModel by activityViewModels(
        factoryProducer = { settingsViewModelFactory },
    )
    private val mainTabsViewModel: MainTabsViewModel by activityViewModels(
        factoryProducer = { mainTabsViewModelFactory },
    )

    private val statisticsAdapter: BaseRecyclerAdapter by lazy { buildAdapter() }

    override fun initUi(): Unit = with(binding) {
        parentFragment?.postponeEnterTransition()

        rvStatisticsList.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = statisticsAdapter
        }

        setOnPreDrawListener {
            parentFragment?.startPostponedEnterTransition()
        }
    }

    override fun initViewModel() = with(binding) {
        with(viewModel) {
            extra = StatisticsExtra(shift = arguments?.getInt(ARGS_POSITION).orZero())
            statistics.observe(statisticsAdapter::replace)
            sharingData.observe(::onNewSharingData)
            resetScreen.observe {
                rvStatisticsList.smoothScrollToPosition(0)
                mainTabsViewModel.onHandled()
            }
            animateChartParticles.observe(::setAnimateParticles)
        }
        with(settingsViewModel) {
            rangeUpdated.observe { viewModel.onRangeUpdated() }
        }
        with(mainTabsViewModel) {
            tabReselected.observe(viewModel::onTabReselected)
            isScrolling.observe(viewModel::isScrolling)
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

    private fun setAnimateParticles(animate: Boolean) {
        statisticsAdapter.currentList
            .indexOfFirst { it is StatisticsChartViewData }
            .let {
                binding.rvStatisticsList
                    .findViewHolderForAdapterPosition(it)
                    ?.itemView?.findViewById<PieChartView>(R.id.chartStatisticsItem)
            }
            ?.setAnimateParticles(animate)
    }

    override fun onChartFilterDialogDismissed() {
        viewModel.onFilterApplied()
    }

    private fun onNewSharingData(data: List<ViewHolderType>) {
        val context = binding.root.context
        val adapter = buildAdapter()
        adapter.replace(data)
        val view = RecyclerView(context).apply {
            layoutManager = LinearLayoutManager(context)
            this.adapter = adapter
            context.getThemedAttr(R.attr.appBackgroundColor).let(::setBackgroundColor)
        }
        viewModel.onShareView(view)
    }

    private fun buildAdapter(): BaseRecyclerAdapter {
        return BaseRecyclerAdapter(
            createStatisticsChartAdapterDelegate(
                onFilterClick = viewModel::onFilterClick,
                onShareClick = throttle(viewModel::onShareClick),
                onChartAttached = viewModel::onChartAttached,
            ),
            createStatisticsInfoAdapterDelegate(),
            createStatisticsAdapterDelegate(
                onItemClick = throttle(viewModel::onItemClick),
            ),
            createStatisticsEmptyAdapterDelegate(
                onFilterClick = viewModel::onFilterClick,
            ),
            createStatisticsGoalAdapterDelegate(viewModel::onGoalClick),
            createStatisticsTitleAdapterDelegate(),
            createHintAdapterDelegate(),
            createHintBigAdapterDelegate(),
            createLoaderAdapterDelegate(),
            createDividerAdapterDelegate(),
        )
    }

    companion object {
        private const val ARGS_POSITION = "args_position"

        fun newInstance(data: StatisticsParams): StatisticsFragment = StatisticsFragment().apply {
            val bundle = Bundle()
            bundle.putInt(ARGS_POSITION, data.shift)
            arguments = bundle
        }
    }
}
