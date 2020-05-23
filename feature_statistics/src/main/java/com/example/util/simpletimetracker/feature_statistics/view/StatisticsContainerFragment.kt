package com.example.util.simpletimetracker.feature_statistics.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import com.example.util.simpletimetracker.feature_statistics.R
import com.example.util.simpletimetracker.feature_statistics.adapter.StatisticsContainerAdapter
import com.example.util.simpletimetracker.feature_statistics.di.StatisticsComponentProvider
import com.example.util.simpletimetracker.feature_statistics.viewModel.StatisticsContainerViewModel
import kotlinx.android.synthetic.main.statistics_container_fragment.*

class StatisticsContainerFragment : Fragment() {

    private val viewModel: StatisticsContainerViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(
            R.layout.statistics_container_fragment,
            container,
            false
        )
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        (activity?.application as StatisticsComponentProvider)
            .statisticsComponent?.inject(viewModel)

        setupPager()

        viewModel.title.observe(viewLifecycleOwner) {
            updateTitle(it)
        }

        viewModel.position.observe(viewLifecycleOwner) {
            pagerStatisticsContainer.currentItem = it + StatisticsContainerAdapter.FIRST
        }

        btnStatisticsContainerPrevious.setOnClickListener {
            viewModel.onPreviousClick()
        }

        btnStatisticsContainerToday.setOnClickListener {
            viewModel.onTodayClick()
        }

        btnStatisticsContainerNext.setOnClickListener {
            viewModel.onNextClick()
        }
    }

    private fun setupPager() {
        val adapter = StatisticsContainerAdapter(this)
        pagerStatisticsContainer.apply {
            this.adapter = adapter
            offscreenPageLimit = 2
            isUserInputEnabled = false
        }
    }

    private fun updateTitle(title: String) {
        btnStatisticsContainerToday.text = title
    }

    companion object {
        fun newInstance() = StatisticsContainerFragment()
    }
}