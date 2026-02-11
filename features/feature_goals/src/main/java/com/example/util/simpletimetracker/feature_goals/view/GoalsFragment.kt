package com.example.util.simpletimetracker.feature_goals.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.util.simpletimetracker.core.base.BaseFragment
import com.example.util.simpletimetracker.core.delegates.dateSelector.viewDelegate.DateSelectorViewDelegate
import com.example.util.simpletimetracker.core.dialog.DateTimeDialogListener
import com.example.util.simpletimetracker.core.di.BaseViewModelFactory
import com.example.util.simpletimetracker.core.sharedViewModel.MainTabsViewModel
import com.example.util.simpletimetracker.core.utils.InsetConfiguration
import com.example.util.simpletimetracker.feature_base_adapter.BaseRecyclerAdapter
import com.example.util.simpletimetracker.feature_base_adapter.hint.createHintAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.hintBig.createHintBigAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.loader.createLoaderAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.statisticsGoal.createStatisticsGoalAdapterDelegate
import com.example.util.simpletimetracker.feature_goals.databinding.GoalsFragmentBinding as Binding
import com.example.util.simpletimetracker.feature_goals.viewModel.GoalsViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class GoalsFragment : BaseFragment<Binding>(), DateTimeDialogListener {

    override val inflater: (LayoutInflater, ViewGroup?, Boolean) -> Binding =
        Binding::inflate

    override var insetConfiguration: InsetConfiguration =
        InsetConfiguration.DoNotApply

    @Inject
    lateinit var mainTabsViewModelFactory: BaseViewModelFactory<MainTabsViewModel>

    private val viewModel: GoalsViewModel by viewModels()
    private val mainTabsViewModel: MainTabsViewModel by activityViewModels(
        factoryProducer = { mainTabsViewModelFactory },
    )

    private val dateSelectorViewHolder by lazy {
        DateSelectorViewDelegate.getViewHolder(
            viewModel = viewModel.dateSelectorViewModelDelegate,
        )
    }

    private val goalsAdapter: BaseRecyclerAdapter by lazy {
        BaseRecyclerAdapter(
            createLoaderAdapterDelegate(),
            createHintAdapterDelegate(),
            createHintBigAdapterDelegate(),
            createStatisticsGoalAdapterDelegate(viewModel::onGoalClick),
        )
    }

    override fun initUi(): Unit = with(binding) {
        parentFragment?.postponeEnterTransition()

        rvGoalsList.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = goalsAdapter
        }

        DateSelectorViewDelegate.initUi(
            fragment = this@GoalsFragment,
            viewHolder = dateSelectorViewHolder,
            viewModel = viewModel.dateSelectorViewModelDelegate,
            binding = containerDatesSelector,
        )
        viewModel.initialize()

        setOnPreDrawListener {
            parentFragment?.startPostponedEnterTransition()
        }
    }

    override fun initUx() {
        DateSelectorViewDelegate.initUx(
            fragment = this,
            binding = binding.containerDatesSelector,
        )
    }

    override fun initViewModel() = with(binding) {
        with(viewModel) {
            goals.observe(goalsAdapter::replace)
            resetScreen.observe {
                rvGoalsList.smoothScrollToPosition(0)
                mainTabsViewModel.onHandled()
            }
        }
        with(mainTabsViewModel) {
            tabReselected.observe(viewModel::onTabReselected)
            isNavBatAtTheBottom.observe(::updateInsetConfiguration)
        }
        DateSelectorViewDelegate.initViewModel(
            fragment = this@GoalsFragment,
            viewHolder = dateSelectorViewHolder,
            viewModel = viewModel.dateSelectorViewModelDelegate,
            binding = containerDatesSelector,
        )
    }

    override fun onResume() {
        super.onResume()
        viewModel.onVisible()
    }

    override fun onPause() {
        super.onPause()
        viewModel.onHidden()
    }

    override fun onDateTimeSet(timestamp: Long, tag: String?) {
        viewModel.onDateTimeSet(timestamp, tag)
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
        fun newInstance(): GoalsFragment = GoalsFragment()
    }
}
