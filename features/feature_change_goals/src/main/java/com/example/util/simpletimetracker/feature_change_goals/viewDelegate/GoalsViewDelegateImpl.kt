package com.example.util.simpletimetracker.feature_change_goals.viewDelegate

import androidx.recyclerview.widget.LinearLayoutManager
import com.example.util.simpletimetracker.feature_base_adapter.BaseRecyclerAdapter
import com.example.util.simpletimetracker.feature_change_goals.api.ChangeRecordTypeGoalsViewData
import com.example.util.simpletimetracker.feature_change_goals.api.GoalsViewModelDelegate
import com.example.util.simpletimetracker.feature_change_goals.api.databinding.ChangeGoalsLayoutBinding
import com.example.util.simpletimetracker.feature_change_goals.api.viewDelegate.GoalsViewDelegate
import com.example.util.simpletimetracker.feature_change_goals.adapter.createGoalAdapterDelegate
import com.example.util.simpletimetracker.feature_change_goals.adapter.createGoalsFooterAdapterDelegate
import com.example.util.simpletimetracker.feature_change_goals.adapter.createGoalsHeaderAdapterDelegate

class GoalsViewDelegateImpl(
    private val viewModel: GoalsViewModelDelegate,
    private val binding: ChangeGoalsLayoutBinding,
) : GoalsViewDelegate {

    override fun initUi(): Unit = with(binding) {
        val goalsAdapter = BaseRecyclerAdapter(
            createGoalsHeaderAdapterDelegate(viewModel::onNotificationsHintClick),
            createGoalAdapterDelegate(viewModel),
            createGoalsFooterAdapterDelegate(viewModel::onGoalAdd),
        )
        rvChangeRecordTypeGoals.apply {
            itemAnimator = null
            layoutManager = LinearLayoutManager(context)
            adapter = goalsAdapter
        }
    }

    override fun onResume() {
        viewModel.onGoalsVisible()
    }

    override fun updateGoalsState(state: ChangeRecordTypeGoalsViewData) {
        val adapter = binding.rvChangeRecordTypeGoals.adapter as? BaseRecyclerAdapter
        adapter?.replace(state.viewData)
    }
}