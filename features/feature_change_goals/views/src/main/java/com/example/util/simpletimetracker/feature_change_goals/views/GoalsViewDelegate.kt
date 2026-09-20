package com.example.util.simpletimetracker.feature_change_goals.views

import androidx.recyclerview.widget.LinearLayoutManager
import com.example.util.simpletimetracker.feature_base_adapter.BaseRecyclerAdapter
import com.example.util.simpletimetracker.feature_change_goals.api.ChangeRecordTypeGoalsViewData
import com.example.util.simpletimetracker.feature_change_goals.api.GoalsViewModelDelegate
import com.example.util.simpletimetracker.feature_change_goals.views.databinding.ChangeGoalsLayoutBinding

// TODO GOALS move to api?
object GoalsViewDelegate {

    fun initGoalUi(
        layout: ChangeGoalsLayoutBinding,
        viewModel: GoalsViewModelDelegate,
    ) = with(layout) {
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

    fun onResume(viewModel: GoalsViewModelDelegate) {
        viewModel.onGoalsVisible()
    }

    fun updateGoalsState(
        state: ChangeRecordTypeGoalsViewData,
        layout: ChangeGoalsLayoutBinding,
    ) {
        (layout.rvChangeRecordTypeGoals.adapter as? BaseRecyclerAdapter)?.replace(state.viewData)
    }
}
