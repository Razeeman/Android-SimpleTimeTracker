package com.example.util.simpletimetracker.feature_statistics.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.util.simpletimetracker.core.adapter.BaseRecyclerAdapter
import com.example.util.simpletimetracker.core.adapter.hint.createHintAdapterDelegate
import com.example.util.simpletimetracker.core.adapter.loader.createLoaderAdapterDelegate
import com.example.util.simpletimetracker.core.adapter.statistics.createStatisticsAdapterDelegate
import com.example.util.simpletimetracker.core.base.BaseBindingFragment
import com.example.util.simpletimetracker.core.di.BaseViewModelFactory
import com.example.util.simpletimetracker.core.dialog.ChartFilterDialogListener
import com.example.util.simpletimetracker.domain.extension.orZero
import com.example.util.simpletimetracker.feature_statistics.adapter.createStatisticsChartAdapterDelegate
import com.example.util.simpletimetracker.feature_statistics.adapter.createStatisticsEmptyAdapterDelegate
import com.example.util.simpletimetracker.feature_statistics.adapter.createStatisticsInfoAdapterDelegate
import com.example.util.simpletimetracker.feature_statistics.extra.StatisticsExtra
import com.example.util.simpletimetracker.feature_statistics.viewModel.StatisticsSettingsViewModel
import com.example.util.simpletimetracker.feature_statistics.viewModel.StatisticsViewModel
import com.example.util.simpletimetracker.navigation.params.StatisticsParams
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import com.example.util.simpletimetracker.feature_statistics.databinding.StatisticsFragmentBinding as Binding

@AndroidEntryPoint
class StatisticsFragment :
    BaseBindingFragment<Binding>(),
    ChartFilterDialogListener {

    override val inflater: (LayoutInflater, ViewGroup?, Boolean) -> Binding =
        Binding::inflate

    @Inject
    lateinit var settingsViewModelFactory: BaseViewModelFactory<StatisticsSettingsViewModel>

    @Inject
    lateinit var viewModelFactory: BaseViewModelFactory<StatisticsViewModel>

    private val settingsViewModel: StatisticsSettingsViewModel by viewModels(
        ownerProducer = { activity as AppCompatActivity },
        factoryProducer = { settingsViewModelFactory }
    )
    private val viewModel: StatisticsViewModel by viewModels(
        factoryProducer = { viewModelFactory }
    )

    private val statisticsAdapter: BaseRecyclerAdapter by lazy {
        BaseRecyclerAdapter(
            createStatisticsChartAdapterDelegate(viewModel::onFilterClick),
            createStatisticsInfoAdapterDelegate(),
            createHintAdapterDelegate(),
            createStatisticsAdapterDelegate(
                addTransitionNames = true,
                onItemClick = viewModel::onItemClick
            ),
            createStatisticsEmptyAdapterDelegate(viewModel::onFilterClick),
            createLoaderAdapterDelegate()
        )
    }

    override fun initUi(): Unit = with(binding) {
        parentFragment?.postponeEnterTransition()

        rvStatisticsList.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = statisticsAdapter

            viewTreeObserver.addOnPreDrawListener {
                parentFragment?.startPostponedEnterTransition()
                true
            }
        }
    }

    override fun initViewModel() {
        with(viewModel) {
            extra = StatisticsExtra(shift = arguments?.getInt(ARGS_POSITION).orZero())
            statistics.observe(statisticsAdapter::replace)
        }
        with(settingsViewModel) {
            rangeLength.observe(viewModel::onNewRange)
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

    override fun onChartFilterDialogDismissed() {
        viewModel.onFilterApplied()
    }

    companion object {
        private const val ARGS_POSITION = "args_position"

        fun newInstance(data: Any?): StatisticsFragment = StatisticsFragment().apply {
            val bundle = Bundle()
            when (data) {
                is StatisticsParams -> {
                    bundle.putInt(ARGS_POSITION, data.shift)
                }
            }
            arguments = bundle
        }
    }
}
