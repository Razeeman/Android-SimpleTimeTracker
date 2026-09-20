package com.example.util.simpletimetracker.feature_change_goals.views

import androidx.recyclerview.widget.LinearLayoutManager
import com.example.util.simpletimetracker.feature_base_adapter.BaseRecyclerAdapter
import com.example.util.simpletimetracker.feature_change_goals.api.ChangeRecordTypeGoalsViewData
import com.example.util.simpletimetracker.feature_change_goals.api.GoalsViewModelDelegate
import com.example.util.simpletimetracker.feature_change_goals.views.databinding.ChangeGoalsLayoutBinding
import com.example.util.simpletimetracker.feature_views.extension.setOnClick

// TODO GOALS move to api?
object GoalsViewDelegate {

    fun initGoalUi(
        layout: ChangeGoalsLayoutBinding,
        viewModel: GoalsViewModelDelegate,
    ) = with(layout) {
        rvChangeRecordTypeGoals.apply {
            itemAnimator = null
            layoutManager = LinearLayoutManager(context)
            adapter = BaseRecyclerAdapter(createGoalAdapterDelegate(viewModel))
        }
    }

    fun initGoalUx(
        viewModel: GoalsViewModelDelegate,
        layout: ChangeGoalsLayoutBinding,
    ) = with(layout) {
        btnChangeRecordTypeGoalAdd.setOnClick(viewModel::onGoalAdd)
        containerChangeRecordTypeGoalNotificationsHint
            .setOnActionClick(viewModel::onNotificationsHintClick)
    }

    fun onResume(viewModel: GoalsViewModelDelegate) {
        viewModel.onGoalsVisible()
    }

    fun updateGoalsState(
        state: ChangeRecordTypeGoalsViewData,
        layout: ChangeGoalsLayoutBinding,
    ) {
        (layout.rvChangeRecordTypeGoals.adapter as? BaseRecyclerAdapter)?.replace(state.goals)
    }
}
