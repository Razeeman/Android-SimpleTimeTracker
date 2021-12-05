package com.example.util.simpletimetracker.feature_statistics.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.viewModels
import com.example.util.simpletimetracker.core.base.BaseFragment
import com.example.util.simpletimetracker.core.di.BaseViewModelFactory
import com.example.util.simpletimetracker.core.dialog.CustomRangeSelectionDialogListener
import com.example.util.simpletimetracker.core.dialog.DateTimeDialogListener
import com.example.util.simpletimetracker.feature_views.extension.setOnClick
import com.example.util.simpletimetracker.feature_views.extension.setOnLongClick
import com.example.util.simpletimetracker.feature_views.extension.visible
import com.example.util.simpletimetracker.core.viewData.RangesViewData
import com.example.util.simpletimetracker.domain.model.Range
import com.example.util.simpletimetracker.domain.model.RangeLength
import com.example.util.simpletimetracker.feature_statistics.adapter.StatisticsContainerAdapter
import com.example.util.simpletimetracker.feature_statistics.viewModel.StatisticsContainerViewModel
import com.example.util.simpletimetracker.feature_statistics.viewModel.StatisticsSettingsViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import com.example.util.simpletimetracker.feature_statistics.databinding.StatisticsContainerFragmentBinding as Binding

@AndroidEntryPoint
class StatisticsContainerFragment :
    BaseFragment<Binding>(),
    DateTimeDialogListener,
    CustomRangeSelectionDialogListener {

    override val inflater: (LayoutInflater, ViewGroup?, Boolean) -> Binding =
        Binding::inflate

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

    override fun initUi(): Unit = with(binding) {
        pagerStatisticsContainer.apply {
            adapter = StatisticsContainerAdapter(this@StatisticsContainerFragment)
            offscreenPageLimit = 1
            isUserInputEnabled = false
        }
    }

    override fun initUx() = with(binding) {
        spinnerStatisticsContainer.onItemSelected = {
            viewModel.onRangeSelected(it)
            settingsViewModel.onRangeSelected(it)
        }
        btnStatisticsContainerPrevious.setOnClick(viewModel::onPreviousClick)
        btnStatisticsContainerNext.setOnClick(viewModel::onNextClick)
        btnStatisticsContainerToday.setOnClick { spinnerStatisticsContainer.performClick() }
        btnStatisticsContainerToday.setOnLongClick(viewModel::onTodayClick)
    }

    override fun onDateTimeSet(timestamp: Long, tag: String?) {
        viewModel.onDateTimeSet(timestamp, tag)
    }

    override fun onCustomRangeSelected(range: Range) {
        viewModel.onCustomRangeSelected(range)
        settingsViewModel.onCustomRangeSelected(range)
    }

    override fun initViewModel() {
        with(viewModel) {
            title.observe(::updateTitle)
            rangeItems.observe(::updateRangeItems)
            position.observe(::updatePosition)
        }
        with(settingsViewModel) {
            rangeLength.observe(::updateNavButtons)
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.onVisible()
    }

    private fun updateNavButtons(rangeLength: RangeLength) = with(binding) {
        if (rangeLength is RangeLength.All || rangeLength is RangeLength.Custom) {
            btnStatisticsContainerPrevious.visible = false
            btnStatisticsContainerNext.visible = false
        } else {
            btnStatisticsContainerPrevious.visible = true
            btnStatisticsContainerNext.visible = true
        }
    }

    private fun updateRangeItems(viewData: RangesViewData) = with(binding) {
        spinnerStatisticsContainer.setData(viewData.items, viewData.selectedPosition)
    }

    private fun updateTitle(title: String) = with(binding) {
        btnStatisticsContainerToday.text = title
    }

    private fun updatePosition(position: Int) = with(binding) {
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