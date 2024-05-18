package com.example.util.simpletimetracker.feature_goals.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.util.simpletimetracker.core.base.BaseFragment
import com.example.util.simpletimetracker.core.di.BaseViewModelFactory
import com.example.util.simpletimetracker.core.sharedViewModel.MainTabsViewModel
import com.example.util.simpletimetracker.feature_base_adapter.BaseRecyclerAdapter
import com.example.util.simpletimetracker.feature_base_adapter.empty.createEmptyAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.hint.createHintAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.loader.createLoaderAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.statisticsGoal.createStatisticsGoalAdapterDelegate
import com.example.util.simpletimetracker.feature_goals.viewModel.GoalsViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import com.example.util.simpletimetracker.feature_goals.databinding.GoalsFragmentBinding as Binding

@AndroidEntryPoint
class GoalsFragment : BaseFragment<Binding>() {

    override val inflater: (LayoutInflater, ViewGroup?, Boolean) -> Binding =
        Binding::inflate

    @Inject
    lateinit var mainTabsViewModelFactory: BaseViewModelFactory<MainTabsViewModel>

    private val viewModel: GoalsViewModel by viewModels()
    private val mainTabsViewModel: MainTabsViewModel by activityViewModels(
        factoryProducer = { mainTabsViewModelFactory },
    )

    private val goalsAdapter: BaseRecyclerAdapter by lazy {
        BaseRecyclerAdapter(
            createLoaderAdapterDelegate(),
            createHintAdapterDelegate(),
            createEmptyAdapterDelegate(),
            createStatisticsGoalAdapterDelegate(viewModel::onGoalClick),
        )
    }

    override fun initUi(): Unit = with(binding) {
        parentFragment?.postponeEnterTransition()

        rvGoalsList.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = goalsAdapter
        }

        setOnPreDrawListener {
            parentFragment?.startPostponedEnterTransition()
        }
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
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.onVisible()
    }

    override fun onPause() {
        super.onPause()
        viewModel.onHidden()
    }

    companion object {
        fun newInstance(): GoalsFragment = GoalsFragment()
    }
}
