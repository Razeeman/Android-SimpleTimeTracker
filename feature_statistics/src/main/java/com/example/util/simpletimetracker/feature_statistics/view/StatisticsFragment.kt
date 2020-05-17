package com.example.util.simpletimetracker.feature_statistics.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.util.simpletimetracker.feature_statistics.R
import com.example.util.simpletimetracker.feature_statistics.adapter.StatisticsAdapter
import com.example.util.simpletimetracker.feature_statistics.adapter.StatisticsViewData
import com.example.util.simpletimetracker.feature_statistics.customView.PiePortion
import com.example.util.simpletimetracker.feature_statistics.di.StatisticsComponentProvider
import com.example.util.simpletimetracker.feature_statistics.viewModel.StatisticsViewModel
import kotlinx.android.synthetic.main.statistics_fragment.*

class StatisticsFragment : Fragment() {

    private val viewModel: StatisticsViewModel by viewModels()
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
            setPieChart(it)
        }
    }

    private fun setPieChart(statistics: List<StatisticsViewData>) {
        statistics.map {
            PiePortion(
                value = it.duration.length.toLong(), // TODO
                colorInt = it.color,
                iconId = it.iconId
            )
        }.let(pieChartStatistics::setSegments)
    }

    override fun onResume() {
        super.onResume()
        viewModel.onVisible()
    }

    companion object {
        fun newInstance() = StatisticsFragment()
    }
}
