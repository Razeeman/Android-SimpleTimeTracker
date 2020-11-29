package com.example.util.simpletimetracker.feature_statistics.view

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.util.simpletimetracker.core.base.BaseFragment
import com.example.util.simpletimetracker.core.di.BaseViewModelFactory
import com.example.util.simpletimetracker.core.dialog.DateTimeDialogListener
import com.example.util.simpletimetracker.core.extension.flipVisibility
import com.example.util.simpletimetracker.core.extension.setOnClick
import com.example.util.simpletimetracker.core.extension.setOnLongClick
import com.example.util.simpletimetracker.core.extension.visible
import com.example.util.simpletimetracker.feature_statistics.R
import com.example.util.simpletimetracker.feature_statistics.adapter.StatisticsContainerAdapter
import com.example.util.simpletimetracker.feature_statistics.adapter.StatisticsRangeAdapter
import com.example.util.simpletimetracker.feature_statistics.di.StatisticsComponentProvider
import com.example.util.simpletimetracker.feature_statistics.viewData.RangeLength
import com.example.util.simpletimetracker.feature_statistics.viewModel.StatisticsContainerViewModel
import com.example.util.simpletimetracker.feature_statistics.viewModel.StatisticsSettingsViewModel
import kotlinx.android.synthetic.main.statistics_container_fragment.*
import javax.inject.Inject

class StatisticsContainerFragment : BaseFragment(R.layout.statistics_container_fragment),
    DateTimeDialogListener {

    @Inject
    lateinit var settingsViewModelFactory: BaseViewModelFactory<StatisticsSettingsViewModel>

    @Inject
    lateinit var viewModelFactory: BaseViewModelFactory<StatisticsContainerViewModel>

    private val settingsViewModel: StatisticsSettingsViewModel by viewModels(
        ownerProducer = { activity as AppCompatActivity },
        factoryProducer = { settingsViewModelFactory }
    )
    private val viewModel: StatisticsContainerViewModel by viewModels(
        factoryProducer = { viewModelFactory }
    )

    private val adapterButtons: StatisticsRangeAdapter by lazy {
        StatisticsRangeAdapter(
            onRangeClick = {
                viewModel.onRangeClick(it)
                settingsViewModel.onRangeClick(it)
            },
            onSelectDateClick = viewModel::onSelectDateClick
        )
    }

    override fun initUi() {
        pagerStatisticsContainer.apply {
            adapter = StatisticsContainerAdapter(this@StatisticsContainerFragment)
            offscreenPageLimit = 1
            isUserInputEnabled = false
        }
        rvStatisticsRanges.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = adapterButtons
        }
    }

    override fun initDi() {
        (activity?.application as StatisticsComponentProvider)
            .statisticsComponent
            ?.inject(this)
    }

    override fun initUx() {
        btnStatisticsContainerPrevious.setOnClick(viewModel::onPreviousClick)
        btnStatisticsContainerNext.setOnClick(viewModel::onNextClick)
        btnStatisticsContainerToday.setOnClick(layoutStatisticsContainerButtons::flipVisibility)
        btnStatisticsContainerToday.setOnLongClick(viewModel::onTodayClick)
    }

    override fun onDateTimeSet(timestamp: Long, tag: String?) {
        viewModel.onDateTimeSet(timestamp, tag)
    }

    override fun initViewModel() {
        with(viewModel) {
            title.observe(viewLifecycleOwner, ::updateTitle)
            buttons.observe(viewLifecycleOwner, adapterButtons::replaceAsNew)
            position.observe(viewLifecycleOwner, ::updatePosition)
        }
        with(settingsViewModel) {
            rangeLength.observe(viewLifecycleOwner, ::updateNavButtons)
        }
    }

    private fun updateNavButtons(rangeLength: RangeLength) {
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
        pagerStatisticsContainer.setCurrentItem(
            position + StatisticsContainerAdapter.FIRST,
            viewPagerSmoothScroll
        )
    }

    companion object {
        var viewPagerSmoothScroll: Boolean = true
        fun newInstance() = StatisticsContainerFragment()
    }
}