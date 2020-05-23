package com.example.util.simpletimetracker.feature_statistics.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.util.simpletimetracker.domain.extension.orZero
import com.example.util.simpletimetracker.feature_statistics.R
import com.example.util.simpletimetracker.feature_statistics.adapter.StatisticsAdapter
import com.example.util.simpletimetracker.feature_statistics.di.StatisticsComponentProvider
import com.example.util.simpletimetracker.feature_statistics.viewModel.StatisticsViewModel
import com.example.util.simpletimetracker.feature_statistics.viewModel.StatisticsViewModelFactory
import com.example.util.simpletimetracker.navigation.params.StatisticsParams
import kotlinx.android.synthetic.main.statistics_fragment.*

class StatisticsFragment : Fragment() {

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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(
            R.layout.statistics_fragment,
            container,
            false
        )
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        rvStatisticsList.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = statisticsAdapter
        }

        (activity?.application as StatisticsComponentProvider)
            .statisticsComponent?.inject(viewModel)

        viewModel.statistics.observe(viewLifecycleOwner) {
            statisticsAdapter.replace(it)
        }
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
