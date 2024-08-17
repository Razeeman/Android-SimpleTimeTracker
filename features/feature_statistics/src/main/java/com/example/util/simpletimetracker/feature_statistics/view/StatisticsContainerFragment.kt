package com.example.util.simpletimetracker.feature_statistics.view

import com.example.util.simpletimetracker.feature_statistics.databinding.StatisticsContainerFragmentBinding as Binding
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.example.util.simpletimetracker.core.base.BaseFragment
import com.example.util.simpletimetracker.core.di.BaseViewModelFactory
import com.example.util.simpletimetracker.core.dialog.CustomRangeSelectionDialogListener
import com.example.util.simpletimetracker.core.dialog.DateTimeDialogListener
import com.example.util.simpletimetracker.core.dialog.DurationDialogListener
import com.example.util.simpletimetracker.core.utils.InsetConfiguration
import com.example.util.simpletimetracker.core.view.SafeFragmentStateAdapter
import com.example.util.simpletimetracker.core.viewData.RangesViewData
import com.example.util.simpletimetracker.domain.model.Range
import com.example.util.simpletimetracker.feature_statistics.adapter.StatisticsContainerAdapter
import com.example.util.simpletimetracker.feature_statistics.viewModel.StatisticsContainerViewModel
import com.example.util.simpletimetracker.feature_statistics.viewModel.StatisticsSettingsViewModel
import com.example.util.simpletimetracker.feature_views.extension.setOnClick
import com.example.util.simpletimetracker.feature_views.extension.setOnLongClick
import com.example.util.simpletimetracker.feature_views.extension.visible
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class StatisticsContainerFragment :
    BaseFragment<Binding>(),
    DateTimeDialogListener,
    DurationDialogListener,
    CustomRangeSelectionDialogListener {

    override val inflater: (LayoutInflater, ViewGroup?, Boolean) -> Binding =
        Binding::inflate

    override val insetConfiguration: InsetConfiguration =
        InsetConfiguration.ApplyToView { binding.root }

    @Inject
    lateinit var settingsViewModelFactory: BaseViewModelFactory<StatisticsSettingsViewModel>

    private val viewModel: StatisticsContainerViewModel by viewModels()
    private val settingsViewModel: StatisticsSettingsViewModel by activityViewModels(
        factoryProducer = { settingsViewModelFactory },
    )

    override fun initUi(): Unit = with(binding) {
        pagerStatisticsContainer.apply {
            adapter = SafeFragmentStateAdapter(StatisticsContainerAdapter(this@StatisticsContainerFragment))
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
        settingsViewModel.onCustomRangeSelected(range)
    }

    override fun onCountSet(count: Long, tag: String?) {
        settingsViewModel.onCountSet(count, tag)
    }

    override fun initViewModel() {
        with(viewModel) {
            title.observe(::updateTitle)
            rangeItems.observe(::updateRangeItems)
            position.observe(::updatePosition)
            navButtonsVisibility.observe(::updateNavButtons)
        }
        with(settingsViewModel) {
            rangeUpdated.observe(viewModel::onRangeUpdated)
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.onVisible()
    }

    private fun updateNavButtons(isVisible: Boolean) = with(binding) {
        btnStatisticsContainerPrevious.visible = isVisible
        btnStatisticsContainerNext.visible = isVisible
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
            viewPagerSmoothScroll,
        )
    }

    companion object {
        var viewPagerSmoothScroll: Boolean = true
        fun newInstance() = StatisticsContainerFragment()
    }
}