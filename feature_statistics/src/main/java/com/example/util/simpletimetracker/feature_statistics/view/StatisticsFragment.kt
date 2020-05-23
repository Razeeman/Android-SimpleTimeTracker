package com.example.util.simpletimetracker.feature_statistics.view

import android.os.Bundle
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.util.simpletimetracker.core.base.BaseFragment
import com.example.util.simpletimetracker.domain.extension.orZero
import com.example.util.simpletimetracker.feature_statistics.R
import com.example.util.simpletimetracker.feature_statistics.adapter.StatisticsAdapter
import com.example.util.simpletimetracker.feature_statistics.di.StatisticsComponentProvider
import com.example.util.simpletimetracker.feature_statistics.viewModel.StatisticsViewModel
import com.example.util.simpletimetracker.feature_statistics.viewModel.StatisticsViewModelFactory
import com.example.util.simpletimetracker.navigation.params.StatisticsParams
import kotlinx.android.synthetic.main.statistics_fragment.*

class StatisticsFragment : BaseFragment(R.layout.statistics_fragment) {

    private val viewModel: StatisticsViewModel by viewModels(
        factoryProducer = {
            StatisticsViewModelFactory(
                rangeStart = arguments?.getLong(ARGS_RANGE_START).orZero(),
                rangeEnd = arguments?.getLong(ARGS_RANGE_END).orZero()
            )
        }
    )
    private val statisticsAdapter: StatisticsAdapter by lazy {
        StatisticsAdapter()
    }

    override fun initDi() {
        val component = (activity?.application as StatisticsComponentProvider)
            .statisticsComponent

        component?.inject(viewModel)
    }

    override fun initUi() {
        rvStatisticsList.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = statisticsAdapter
        }
    }

    override fun initViewModel() {
        viewModel.statistics.observe(viewLifecycleOwner, statisticsAdapter::replace)
    }

    override fun onResume() {
        super.onResume()
        viewModel.onVisible()
    }

    companion object {
        private const val ARGS_RANGE_START = "args_range_start"
        private const val ARGS_RANGE_END = "args_range_end"

        fun newInstance(data: Any?): StatisticsFragment = StatisticsFragment().apply {
            val bundle = Bundle()
            when (data) {
                is StatisticsParams -> {
                    bundle.putLong(ARGS_RANGE_START, data.rangeStart)
                    bundle.putLong(ARGS_RANGE_END, data.rangeEnd)
                }
            }
            arguments = bundle
        }
    }
}
