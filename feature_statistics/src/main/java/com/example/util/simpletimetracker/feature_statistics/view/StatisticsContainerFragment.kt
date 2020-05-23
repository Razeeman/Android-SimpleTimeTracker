package com.example.util.simpletimetracker.feature_statistics.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import com.example.util.simpletimetracker.core.extension.flipVisibility
import com.example.util.simpletimetracker.core.extension.setOnClick
import com.example.util.simpletimetracker.core.extension.setOnLongClick
import com.example.util.simpletimetracker.core.extension.visible
import com.example.util.simpletimetracker.feature_statistics.R
import com.example.util.simpletimetracker.feature_statistics.adapter.StatisticsContainerAdapter
import com.example.util.simpletimetracker.feature_statistics.di.StatisticsComponentProvider
import com.example.util.simpletimetracker.feature_statistics.viewData.RangeLength
import com.example.util.simpletimetracker.feature_statistics.viewModel.StatisticsContainerViewModel
import kotlinx.android.synthetic.main.statistics_container_fragment.*

class StatisticsContainerFragment : Fragment() {

    private val viewModel: StatisticsContainerViewModel by viewModels()
    private var adapter: StatisticsContainerAdapter? = null

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

        viewModel.title.observe(viewLifecycleOwner, ::updateTitle)
        viewModel.rangeLength.observe(viewLifecycleOwner, ::updateRange)
        viewModel.position.observe(viewLifecycleOwner, ::updatePosition)

        btnStatisticsContainerPrevious.setOnClick(viewModel::onPreviousClick)
        btnStatisticsContainerNext.setOnClick(viewModel::onNextClick)
        btnStatisticsContainerToday.setOnClick(layoutStatisticsContainerButtons::flipVisibility)
        btnStatisticsContainerToday.setOnLongClick(viewModel::onTodayClick)
        // TODO recycler?
        btnStatisticsContainerRange1.setOnClick { viewModel.onRangeClick(1) }
        btnStatisticsContainerRange2.setOnClick { viewModel.onRangeClick(2) }
        btnStatisticsContainerRange3.setOnClick { viewModel.onRangeClick(3) }
        btnStatisticsContainerRange4.setOnClick { viewModel.onRangeClick(4) }
    }

    private fun setupPager(rangeLength: RangeLength) {
        adapter = StatisticsContainerAdapter(this, rangeLength)
        pagerStatisticsContainer.apply {
            this.adapter = this@StatisticsContainerFragment.adapter
            offscreenPageLimit = 2
            isUserInputEnabled = false
        }
    }

    private fun updateRange(rangeLength: RangeLength) {
        // TODO avoid recreation, implement update
        setupPager(rangeLength)
        if (rangeLength == RangeLength.ALL) {
            btnStatisticsContainerPrevious.visible = false
            btnStatisticsContainerNext.visible = false
        } else {
            btnStatisticsContainerPrevious.visible = true
            btnStatisticsContainerNext.visible = true
        }
    }

    private fun updateTitle(title: String) {
        layoutStatisticsContainerButtons.visible = false
        btnStatisticsContainerToday.text = title
    }

    private fun updatePosition(position: Int) {
        pagerStatisticsContainer.currentItem = position + StatisticsContainerAdapter.FIRST
    }

    companion object {
        fun newInstance() = StatisticsContainerFragment()
    }
}