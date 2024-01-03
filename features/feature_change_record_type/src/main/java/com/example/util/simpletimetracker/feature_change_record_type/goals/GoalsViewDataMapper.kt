package com.example.util.simpletimetracker.feature_change_record_type.goals

import com.example.util.simpletimetracker.core.mapper.DayOfWeekViewDataMapper
import com.example.util.simpletimetracker.core.mapper.TimeMapper
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.extension.orZero
import com.example.util.simpletimetracker.domain.model.DayOfWeek
import com.example.util.simpletimetracker.domain.model.RecordTypeGoal
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.dayOfWeek.DayOfWeekViewData
import com.example.util.simpletimetracker.feature_change_record_type.R
import com.example.util.simpletimetracker.feature_change_record_type.viewData.ChangeRecordTypeGoalsState
import com.example.util.simpletimetracker.feature_change_record_type.viewData.ChangeRecordTypeGoalsViewData
import com.example.util.simpletimetracker.feature_views.spinner.CustomSpinner
import javax.inject.Inject

class GoalsViewDataMapper @Inject constructor(
    private val resourceRepo: ResourceRepo,
    private val timeMapper: TimeMapper,
    private val dayOfWeekViewDataMapper: DayOfWeekViewDataMapper,
) {

    private val goalTypeList: List<ChangeRecordTypeGoalsViewData.Type> = listOf(
        ChangeRecordTypeGoalsViewData.Type.Duration,
        ChangeRecordTypeGoalsViewData.Type.Count,
    )

    fun toGoalType(position: Int): RecordTypeGoal.Type {
        return when (goalTypeList.getOrNull(position) ?: goalTypeList.first()) {
            is ChangeRecordTypeGoalsViewData.Type.Duration -> {
                RecordTypeGoal.Type.Duration(0)
            }
            is ChangeRecordTypeGoalsViewData.Type.Count -> {
                RecordTypeGoal.Type.Count(0)
            }
        }
    }

    fun mapGoalsState(
        goalsState: ChangeRecordTypeGoalsState,
        isDarkTheme: Boolean,
    ): ChangeRecordTypeGoalsViewData {
        return ChangeRecordTypeGoalsViewData(
            session = mapGoalViewData(
                title = resourceRepo.getString(R.string.change_record_type_session_goal_time),
                goal = goalsState.session,
            ),
            daily = mapGoalViewData(
                title = resourceRepo.getString(R.string.change_record_type_daily_goal_time),
                goal = goalsState.daily,
            ),
            weekly = mapGoalViewData(
                title = resourceRepo.getString(R.string.change_record_type_weekly_goal_time),
                goal = goalsState.weekly,
            ),
            monthly = mapGoalViewData(
                title = resourceRepo.getString(R.string.change_record_type_monthly_goal_time),
                goal = goalsState.monthly,
            ),
            daysOfWeek = mapDaysOfWeekViewData(
                goal = goalsState.daily,
                selectedDaysOfWeek = goalsState.daysOfWeek,
                isDarkTheme = isDarkTheme,
            )
        )
    }

    fun getDefaultGoalState(): ChangeRecordTypeGoalsState {
        return ChangeRecordTypeGoalsState(
            session = getDefaultGoal(),
            daily = getDefaultGoal(),
            weekly = getDefaultGoal(),
            monthly = getDefaultGoal(),
            daysOfWeek = DayOfWeek.values().toList(),
        )
    }

    fun getDefaultGoal(): RecordTypeGoal.Type {
        return RecordTypeGoal.Type.Duration(0)
    }

    private fun mapGoalViewData(
        title: String,
        goal: RecordTypeGoal.Type,
    ): ChangeRecordTypeGoalsViewData.GoalViewData {
        val goalViewData = when (goal) {
            is RecordTypeGoal.Type.Duration -> ChangeRecordTypeGoalsViewData.Type.Duration
            is RecordTypeGoal.Type.Count -> ChangeRecordTypeGoalsViewData.Type.Count
        }
        val position = goalTypeList.indexOf(goalViewData)
            .takeUnless { it == -1 }.orZero()
        val value = when (goal) {
            is RecordTypeGoal.Type.Duration -> toDurationGoalText(goal.value.orZero())
            is RecordTypeGoal.Type.Count -> goal.value.orZero().toString()
        }
        val items = goalTypeList.map {
            when (it) {
                is ChangeRecordTypeGoalsViewData.Type.Duration -> {
                    resourceRepo.getString(R.string.change_record_type_goal_duration)
                }
                is ChangeRecordTypeGoalsViewData.Type.Count -> {
                    resourceRepo.getString(R.string.change_record_type_goal_count)
                }
            }
        }.map(CustomSpinner::CustomSpinnerTextItem)

        return ChangeRecordTypeGoalsViewData.GoalViewData(
            title = title,
            typeItems = items,
            typeSelectedPosition = position,
            type = goalViewData,
            value = value,
        )
    }

    private fun toDurationGoalText(duration: Long): String {
        return if (duration > 0) {
            timeMapper.formatDuration(duration)
        } else {
            resourceRepo.getString(R.string.change_record_type_goal_time_disabled)
        }
    }

    private fun mapDaysOfWeekViewData(
        goal: RecordTypeGoal.Type,
        selectedDaysOfWeek: List<DayOfWeek>,
        isDarkTheme: Boolean,
    ): List<ViewHolderType> {
        if (goal.value == 0L) return emptyList()

        return dayOfWeekViewDataMapper.mapViewData(
            selectedDaysOfWeek = selectedDaysOfWeek,
            isDarkTheme = isDarkTheme,
            width = DayOfWeekViewData.Width.MatchParent,
            paddingHorizontalDp = 2,
        )
    }
}