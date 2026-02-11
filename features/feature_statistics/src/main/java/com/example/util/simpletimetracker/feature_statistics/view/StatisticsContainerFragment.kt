package com.example.util.simpletimetracker.feature_statistics.view

import com.example.util.simpletimetracker.feature_statistics.databinding.StatisticsContainerFragmentBinding as Binding
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.example.util.simpletimetracker.core.base.BaseFragment
import com.example.util.simpletimetracker.core.delegates.dateSelector.viewDelegate.DateSelectorViewDelegate
import com.example.util.simpletimetracker.core.di.BaseViewModelFactory
import com.example.util.simpletimetracker.core.dialog.CustomRangeSelectionDialogListener
import com.example.util.simpletimetracker.core.dialog.DateTimeDialogListener
import com.example.util.simpletimetracker.core.dialog.DurationDialogListener
import com.example.util.simpletimetracker.core.dialog.OptionsListDialogListener
import com.example.util.simpletimetracker.core.sharedViewModel.MainTabsViewModel
import com.example.util.simpletimetracker.core.utils.InsetConfiguration
import com.example.util.simpletimetracker.core.view.SafeFragmentStateAdapter
import com.example.util.simpletimetracker.core.viewData.RangeSelectionOptionsListItem
import com.example.util.simpletimetracker.domain.record.model.Range
import com.example.util.simpletimetracker.feature_statistics.adapter.StatisticsContainerAdapter
import com.example.util.simpletimetracker.feature_statistics.api.StatisticsContainerOptionsListItem
import com.example.util.simpletimetracker.feature_statistics.viewModel.StatisticsContainerViewModel
import com.example.util.simpletimetracker.navigation.params.screen.OptionsListParams
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class StatisticsContainerFragment :
    BaseFragment<Binding>(),
    DateTimeDialogListener,
    DurationDialogListener,
    CustomRangeSelectionDialogListener,
    OptionsListDialogListener {

    override val inflater: (LayoutInflater, ViewGroup?, Boolean) -> Binding =
        Binding::inflate

    override var insetConfiguration: InsetConfiguration =
        InsetConfiguration.DoNotApply

    @Inject
    lateinit var mainTabsViewModelFactory: BaseViewModelFactory<MainTabsViewModel>

    private val viewModel: StatisticsContainerViewModel by viewModels()
    private val mainTabsViewModel: MainTabsViewModel by activityViewModels(
        factoryProducer = { mainTabsViewModelFactory },
    )
    private val dateSelectorViewHolder by lazy {
        DateSelectorViewDelegate.getViewHolder(
            viewModel = viewModel.dateSelectorViewModelDelegate,
        )
    }

    override fun initUi(): Unit = with(binding) {
        pagerStatisticsContainer.apply {
            adapter = SafeFragmentStateAdapter(
                StatisticsContainerAdapter(this@StatisticsContainerFragment),
            )
            offscreenPageLimit = 1
            isUserInputEnabled = false
        }
        DateSelectorViewDelegate.initUi(
            fragment = this@StatisticsContainerFragment,
            viewHolder = dateSelectorViewHolder,
            viewModel = viewModel.dateSelectorViewModelDelegate,
            binding = containerDatesSelector,
        )
        viewModel.initialize()
    }

    override fun initUx() {
        DateSelectorViewDelegate.initUx(
            fragment = this,
            binding = binding.containerDatesSelector,
            onOptionsClick = viewModel::onOptionsClick,
            onOptionsLongClick = viewModel::onOptionsLongClick,
        )
    }

    override fun onDateTimeSet(timestamp: Long, tag: String?) {
        viewModel.onDateTimeSet(timestamp, tag)
    }

    override fun onCustomRangeSelected(range: Range) {
        viewModel.onCustomRangeSelected(range)
    }

    override fun onCountSet(count: Long, tag: String?) {
        viewModel.onCountSet(count, tag)
    }

    override fun initViewModel() {
        with(viewModel) {
            position.observe(::updatePosition)
        }
        with(mainTabsViewModel) {
            isNavBatAtTheBottom.observe(::updateInsetConfiguration)
        }
        DateSelectorViewDelegate.initViewModel(
            fragment = this,
            viewHolder = dateSelectorViewHolder,
            viewModel = viewModel.dateSelectorViewModelDelegate,
            binding = binding.containerDatesSelector,
        )
    }

    override fun onResume() {
        super.onResume()
        viewModel.onVisible()
    }

    override fun onOptionsItemClick(id: OptionsListParams.Item.Id) {
        when (id) {
            is StatisticsContainerOptionsListItem -> {
                viewModel.onOptionsItemClick(id)
            }
            is RangeSelectionOptionsListItem -> {
                viewModel.onRangeSelected(id)
            }
        }
    }

    override fun onOptionsDialogOpened() {
        viewModel.onOptionsDialogOpened()
    }

    override fun onOptionsDialogClosed() {
        viewModel.onOptionsDialogClosed()
    }

    private fun updatePosition(position: Int) = with(binding) {
        pagerStatisticsContainer.setCurrentItem(
            position + StatisticsContainerAdapter.FIRST,
            viewPagerSmoothScroll,
        )
    }

    private fun updateInsetConfiguration(isNavBatAtTheBottom: Boolean) {
        insetConfiguration = if (isNavBatAtTheBottom) {
            InsetConfiguration.DoNotApply
        } else {
            InsetConfiguration.ApplyToView { binding.root }
        }
        initInsets()
    }

    companion object {
        var viewPagerSmoothScroll: Boolean = true
        fun newInstance() = StatisticsContainerFragment()
    }
}