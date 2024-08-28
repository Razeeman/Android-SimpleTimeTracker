package com.example.util.simpletimetracker.feature_change_goals.views

import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import com.example.util.simpletimetracker.domain.model.RecordTypeGoal
import com.example.util.simpletimetracker.feature_base_adapter.BaseRecyclerAdapter
import com.example.util.simpletimetracker.feature_change_goals.api.ChangeRecordTypeGoalsViewData
import com.example.util.simpletimetracker.feature_change_goals.api.GoalsViewModelDelegate
import com.example.util.simpletimetracker.feature_change_goals.views.databinding.ChangeGoalLayoutBinding
import com.example.util.simpletimetracker.feature_change_goals.views.databinding.ChangeGoalsLayoutBinding
import com.example.util.simpletimetracker.feature_views.extension.setOnClick
import com.example.util.simpletimetracker.feature_views.extension.visible
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent

object GoalsViewDelegate {

    fun initGoalUi(
        layout: ChangeGoalsLayoutBinding,
        dayOfWeekAdapter: BaseRecyclerAdapter,
    ) = with(layout) {
        listOf(
            layoutChangeRecordTypeGoalSession,
            layoutChangeRecordTypeGoalDaily,
            layoutChangeRecordTypeGoalWeekly,
            layoutChangeRecordTypeGoalMonthly,
        ).forEach {
            it.spinnerRecordTypeGoalType.setProcessSameItemSelection(false)
        }

        // No count goal for session.
        layoutChangeRecordTypeGoalSession.arrowChangeRecordTypeGoalType.visible = false
        layoutChangeRecordTypeGoalSession.fieldRecordTypeGoalType.isEnabled = false

        // Init goal days only for daily goal.
        listOf(
            layoutChangeRecordTypeGoalSession,
            layoutChangeRecordTypeGoalWeekly,
            layoutChangeRecordTypeGoalMonthly,
        ).forEach {
            it.rvChangeRecordTypeGoalDays.visible = false
        }
        layoutChangeRecordTypeGoalDaily.rvChangeRecordTypeGoalDays.apply {
            layoutManager = FlexboxLayoutManager(context).apply {
                flexDirection = FlexDirection.ROW
                justifyContent = JustifyContent.CENTER
                flexWrap = FlexWrap.NOWRAP
            }
            adapter = dayOfWeekAdapter
        }
    }

    fun initGoalUx(
        viewModel: GoalsViewModelDelegate,
        layout: ChangeGoalsLayoutBinding,
    ) = with(layout) {
        fun initUx(
            range: RecordTypeGoal.Range,
            view: ChangeGoalLayoutBinding,
        ) {
            view.fieldRecordTypeGoalType.setOnClick(view.spinnerRecordTypeGoalType::performClick)
            view.spinnerRecordTypeGoalType.onPositionSelected = { position ->
                viewModel.onGoalTypeSelected(range, position)
            }
            view.fieldChangeRecordTypeGoalDuration.setOnClick {
                viewModel.onGoalTimeClick(range)
            }
            view.etChangeRecordTypeGoalCountValue.doAfterTextChanged {
                viewModel.onGoalCountChange(range, it.toString())
            }
        }

        initUx(RecordTypeGoal.Range.Session, layoutChangeRecordTypeGoalSession)
        initUx(RecordTypeGoal.Range.Daily, layoutChangeRecordTypeGoalDaily)
        initUx(RecordTypeGoal.Range.Weekly, layoutChangeRecordTypeGoalWeekly)
        initUx(RecordTypeGoal.Range.Monthly, layoutChangeRecordTypeGoalMonthly)
        containerChangeRecordTypeGoalNotificationsHint
            .setOnActionClick(viewModel::onNotificationsHintClick)
    }

    fun onResume(
        layout: ChangeGoalsLayoutBinding,
    ) {
        with(layout) {
            layoutChangeRecordTypeGoalSession.spinnerRecordTypeGoalType
                .jumpDrawablesToCurrentState()
        }
    }

    fun updateGoalsState(
        state: ChangeRecordTypeGoalsViewData,
        layout: ChangeGoalsLayoutBinding,
    ) = with(layout) {
        fun applyGoalToView(
            goal: ChangeRecordTypeGoalsViewData.GoalViewData,
            view: ChangeGoalLayoutBinding,
        ) {
            view.tvChangeRecordTypeGoalTitle.text = goal.title
            view.spinnerRecordTypeGoalType.setData(
                items = goal.typeItems,
                selectedPosition = goal.typeSelectedPosition,
            )
            view.tvChangeRecordTypeGoalType.text = goal.typeItems
                .getOrNull(goal.typeSelectedPosition)?.text.orEmpty()

            val value = goal.value
            when (goal.type) {
                is ChangeRecordTypeGoalsViewData.Type.Duration -> {
                    view.tvChangeRecordTypeGoalDurationValue.text = value
                    view.fieldChangeRecordTypeGoalDuration.isVisible = true
                    view.inputChangeRecordTypeGoalCount.isInvisible = true
                }
                is ChangeRecordTypeGoalsViewData.Type.Count -> {
                    val current = view.etChangeRecordTypeGoalCountValue.text.toString().toLongOrNull()
                    val new = value.toLongOrNull()
                    if (current != new) {
                        view.etChangeRecordTypeGoalCountValue.setText(value)
                        view.etChangeRecordTypeGoalCountValue.setSelection(value.length)
                    }
                    view.fieldChangeRecordTypeGoalDuration.isInvisible = true
                    view.inputChangeRecordTypeGoalCount.isVisible = true
                }
            }
        }

        applyGoalToView(state.session, layoutChangeRecordTypeGoalSession)
        applyGoalToView(state.daily, layoutChangeRecordTypeGoalDaily)
        applyGoalToView(state.weekly, layoutChangeRecordTypeGoalWeekly)
        applyGoalToView(state.monthly, layoutChangeRecordTypeGoalMonthly)

        layoutChangeRecordTypeGoalDaily.rvChangeRecordTypeGoalDays.apply {
            (adapter as? BaseRecyclerAdapter)?.replace(state.daysOfWeek)
        }
    }
}