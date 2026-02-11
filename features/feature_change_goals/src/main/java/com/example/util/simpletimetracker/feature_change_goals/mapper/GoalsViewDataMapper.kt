package com.example.util.simpletimetracker.feature_change_goals.mapper

import com.example.util.simpletimetracker.core.mapper.DayOfWeekViewDataMapper
import com.example.util.simpletimetracker.core.mapper.TimeMapper
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.extension.orEmpty
import com.example.util.simpletimetracker.domain.extension.orZero
import com.example.util.simpletimetracker.domain.daysOfWeek.model.DayOfWeek
import com.example.util.simpletimetracker.domain.recordType.model.RecordTypeGoal
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.dayOfWeek.DayOfWeekViewData
import com.example.util.simpletimetracker.feature_change_goals.R
import com.example.util.simpletimetracker.feature_change_goals.viewData.ChangeRecordTypeGoalsState
import com.example.util.simpletimetracker.feature_change_goals.api.ChangeRecordTypeGoalsViewData
import com.example.util.simpletimetracker.feature_change_goals.viewData.ChangeRecordTypeGoalSubtypeViewData
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
        firstDayOfWeek: DayOfWeek,
    ): ChangeRecordTypeGoalsViewData {
        val selectedCount = listOf(
            goalsState.session,
            goalsState.daily,
            goalsState.weekly,
            goalsState.monthly,
        ).count {
            it.type.value > 0
        }

        return ChangeRecordTypeGoalsViewData(
            selectedCount = selectedCount,
            session = mapGoalViewData(
                title = resourceRepo.getString(R.string.change_record_type_session_goal_time),
                state = goalsState.session,
            ),
            daily = mapGoalViewData(
                title = resourceRepo.getString(R.string.change_record_type_daily_goal_time),
                state = goalsState.daily,
            ),
            weekly = mapGoalViewData(
                title = resourceRepo.getString(R.string.change_record_type_weekly_goal_time),
                state = goalsState.weekly,
            ),
            monthly = mapGoalViewData(
                title = resourceRepo.getString(R.string.change_record_type_monthly_goal_time),
                state = goalsState.monthly,
            ),
            daysOfWeek = mapDaysOfWeekViewData(
                goal = goalsState.daily.type,
                selectedDaysOfWeek = goalsState.daysOfWeek,
                isDarkTheme = isDarkTheme,
                firstDayOfWeek = firstDayOfWeek,
            ),
        )
    }

    fun getDefaultGoalState(): ChangeRecordTypeGoalsState {
        return ChangeRecordTypeGoalsState(
            session = getDefaultGoal(),
            daily = getDefaultGoal(),
            weekly = getDefaultGoal(),
            monthly = getDefaultGoal(),
            daysOfWeek = DayOfWeek.entries.toSet(),
        )
    }

    fun getDefaultGoal(): ChangeRecordTypeGoalsState.GoalState {
        return ChangeRecordTypeGoalsState.GoalState(
            type = RecordTypeGoal.Type.Duration(0),
            subtype = RecordTypeGoal.Subtype.Goal,
        )
    }

    private fun mapGoalViewData(
        title: String,
        state: ChangeRecordTypeGoalsState.GoalState,
    ): ChangeRecordTypeGoalsViewData.GoalViewData {
        val goal = state.type
        val goalViewData = when (goal) {
            is RecordTypeGoal.Type.Duration -> ChangeRecordTypeGoalsViewData.Type.Duration
            is RecordTypeGoal.Type.Count -> ChangeRecordTypeGoalsViewData.Type.Count
        }
        val position = goalTypeList.indexOf(goalViewData)
            .takeUnless { it == -1 }.orZero()
        val value = when (goal) {
            is RecordTypeGoal.Type.Duration -> toDurationGoalText(goal.value)
            is RecordTypeGoal.Type.Count -> goal.value.takeUnless { it == 0L }?.toString().orEmpty()
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
        val subtypeItems = listOf(
            RecordTypeGoal.Subtype.Goal,
            RecordTypeGoal.Subtype.Limit,
        ).takeIf {
            goal.value > 0L
        }.orEmpty().map {
            val name = when (it) {
                is RecordTypeGoal.Subtype.Goal -> R.string.change_record_type_goal_time_hint
                is RecordTypeGoal.Subtype.Limit -> R.string.change_record_type_limit_time_hint
            }.let(resourceRepo::getString)
            ChangeRecordTypeGoalSubtypeViewData(
                subtype = it,
                name = name,
                isSelected = it::class.java == state.subtype::class.java,
                textSizeSp = null,
            )
        }

        return ChangeRecordTypeGoalsViewData.GoalViewData(
            title = title,
            typeItems = items,
            typeSelectedPosition = position,
            type = goalViewData,
            subtypeItems = subtypeItems,
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
        selectedDaysOfWeek: Set<DayOfWeek>,
        isDarkTheme: Boolean,
        firstDayOfWeek: DayOfWeek,
    ): List<ViewHolderType> {
        if (goal.value == 0L) return emptyList()

        return dayOfWeekViewDataMapper.mapViewData(
            selectedDaysOfWeek = selectedDaysOfWeek,
            isDarkTheme = isDarkTheme,
            firstDayOfWeek = firstDayOfWeek,
            width = DayOfWeekViewData.Width.MatchParent,
            paddingHorizontalDp = 2,
        )
    }
}