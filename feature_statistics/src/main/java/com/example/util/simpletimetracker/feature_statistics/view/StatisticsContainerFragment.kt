package com.example.util.simpletimetracker.feature_statistics.view

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import com.example.util.simpletimetracker.core.base.BaseFragment
import com.example.util.simpletimetracker.core.di.BaseViewModelFactory
import com.example.util.simpletimetracker.core.dialog.DateTimeDialogListener
import com.example.util.simpletimetracker.core.extension.setOnClick
import com.example.util.simpletimetracker.core.extension.setOnLongClick
import com.example.util.simpletimetracker.core.extension.visible
import com.example.util.simpletimetracker.core.viewData.RangesViewData
import com.example.util.simpletimetracker.domain.model.RangeLength
import com.example.util.simpletimetracker.feature_statistics.R
import com.example.util.simpletimetracker.feature_statistics.adapter.StatisticsContainerAdapter
import com.example.util.simpletimetracker.feature_statistics.viewModel.StatisticsContainerViewModel
import com.example.util.simpletimetracker.feature_statistics.viewModel.StatisticsSettingsViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.statistics_container_fragment.*
import javax.inject.Inject

@AndroidEntryPoint
class StatisticsContainerFragment : BaseFragment(),
    DateTimeDialogListener {

    override val layout: Int get() = R.layout.statistics_container_fragment

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

    override fun initUi() {
        pagerStatisticsContainer.apply {
            adapter = StatisticsContainerAdapter(this@StatisticsContainerFragment)
            offscreenPageLimit = 1
            isUserInputEnabled = false
        }
    }

    override fun initUx() {
        spinnerStatisticsContainer.onItemSelected = {
            viewModel.onRangeClick(it)
            settingsViewModel.onRangeClick(it)
        }
        btnStatisticsContainerPrevious.setOnClick(viewModel::onPreviousClick)
        btnStatisticsContainerNext.setOnClick(viewModel::onNextClick)
        btnStatisticsContainerToday.setOnClick { spinnerStatisticsContainer.performClick() }
        btnStatisticsContainerToday.setOnLongClick(viewModel::onTodayClick)
    }

    override fun onDateTimeSet(timestamp: Long, tag: String?) {
        viewModel.onDateTimeSet(timestamp, tag)
    }

    override fun initViewModel() {
        with(viewModel) {
            title.observe(viewLifecycleOwner, ::updateTitle)
            rangeItems.observe(viewLifecycleOwner, ::updateRangeItems)
            position.observe(viewLifecycleOwner, ::updatePosition)
        }
        with(settingsViewModel) {
            rangeLength.observe(viewLifecycleOwner, ::updateNavButtons)
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.onVisible()
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

    private fun updateRangeItems(viewData: RangesViewData) {
        spinnerStatisticsContainer.setData(viewData.items, viewData.selectedPosition)
    }

    private fun updateTitle(title: String) {
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