package com.example.util.simpletimetracker.feature_change_goals.mapper

import android.graphics.Typeface.BOLD
import android.text.style.StyleSpan
import com.example.util.simpletimetracker.core.mapper.DayOfWeekViewDataMapper
import com.example.util.simpletimetracker.core.mapper.GoalViewDataMapper
import com.example.util.simpletimetracker.core.mapper.TimeMapper
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.extension.orEmpty
import com.example.util.simpletimetracker.domain.extension.orZero
import com.example.util.simpletimetracker.domain.daysOfWeek.model.DayOfWeek
import com.example.util.simpletimetracker.domain.recordType.model.RecordTypeGoal
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.dayOfWeek.DayOfWeekViewData
import com.example.util.simpletimetracker.feature_change_goals.R
import com.example.util.simpletimetracker.feature_change_goals.adapter.GoalsFooterViewData
import com.example.util.simpletimetracker.feature_change_goals.adapter.GoalsHeaderViewData
import com.example.util.simpletimetracker.feature_change_goals.api.ChangeRecordTypeGoalsViewData
import com.example.util.simpletimetracker.feature_change_goals.viewData.ChangeRecordTypeGoalSubtypeViewData
import com.example.util.simpletimetracker.feature_change_goals.viewData.ChangeRecordTypeGoalsState
import com.example.util.simpletimetracker.feature_views.extension.joinToSpannable
import com.example.util.simpletimetracker.feature_views.extension.setSpan
import com.example.util.simpletimetracker.feature_views.extension.toSpannableString
import com.example.util.simpletimetracker.feature_views.spinner.CustomSpinner
import javax.inject.Inject

class GoalsViewDataMapper @Inject constructor(
    private val resourceRepo: ResourceRepo,
    private val timeMapper: TimeMapper,
    private val dayOfWeekViewDataMapper: DayOfWeekViewDataMapper,
    private val goalViewDataMapper: GoalViewDataMapper,
) {

    private val goalTypeList: List<ChangeRecordTypeGoalsViewData.Type> = listOf(
        ChangeRecordTypeGoalsViewData.Type.Duration,
        ChangeRecordTypeGoalsViewData.Type.Count,
    )
    private val goalRangeList: List<RecordTypeGoal.Range> = listOf(
        RecordTypeGoal.Range.Session,
        RecordTypeGoal.Range.Daily,
        RecordTypeGoal.Range.Weekly,
        RecordTypeGoal.Range.Monthly,
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

    fun toGoalRange(position: Int): RecordTypeGoal.Range {
        return goalRangeList.getOrNull(position) ?: goalRangeList.first()
    }

    fun mapGoalsState(
        goalsState: ChangeRecordTypeGoalsState,
        notificationsHintVisible: Boolean,
        isDarkTheme: Boolean,
        firstDayOfWeek: DayOfWeek,
    ): ChangeRecordTypeGoalsViewData {
        val viewData = mutableListOf<ViewHolderType>()
        viewData += GoalsHeaderViewData(notificationsHintVisible)
        viewData += goalsState.data.map {
            mapGoalViewData(
                state = it,
                isExpanded = it.key == goalsState.expandedGoalKey,
                isDarkTheme = isDarkTheme,
                firstDayOfWeek = firstDayOfWeek,
            )
        }
        viewData += GoalsFooterViewData

        return ChangeRecordTypeGoalsViewData(
            selectedCount = goalsState.data.count { it.type.value > 0 },
            viewData = viewData,
        )
    }

    fun getDefaultGoalState(): ChangeRecordTypeGoalsState {
        return ChangeRecordTypeGoalsState(
            data = emptyList(),
            expandedGoalKey = null,
        )
    }

    fun getDefaultGoal(key: Long): ChangeRecordTypeGoalsState.GoalState {
        return ChangeRecordTypeGoalsState.GoalState(
            key = key,
            id = 0L,
            range = RecordTypeGoal.Range.Daily,
            type = RecordTypeGoal.Type.Duration(0),
            subtype = RecordTypeGoal.Subtype.Goal,
            daysOfWeek = DayOfWeek.entries.toSet(),
            requestScroll = true,
        )
    }

    private fun mapGoalViewData(
        state: ChangeRecordTypeGoalsState.GoalState,
        isExpanded: Boolean,
        isDarkTheme: Boolean,
        firstDayOfWeek: DayOfWeek,
    ): ChangeRecordTypeGoalsViewData.GoalViewData {
        val goal = state.type

        val goalViewData = when (goal) {
            is RecordTypeGoal.Type.Duration -> ChangeRecordTypeGoalsViewData.Type.Duration
            is RecordTypeGoal.Type.Count -> ChangeRecordTypeGoalsViewData.Type.Count
        }

        // Type Duration / Count
        val availableGoalTypes = if (state.range !is RecordTypeGoal.Range.Session) {
            goalTypeList
        } else {
            // No count goal for session.
            listOf(ChangeRecordTypeGoalsViewData.Type.Duration)
        }
        val typeSelectedPosition = availableGoalTypes.indexOf(goalViewData)
            .takeUnless { it == -1 }.orZero()
        val value = when (goal) {
            is RecordTypeGoal.Type.Duration -> toDurationGoalText(goal.value)
            is RecordTypeGoal.Type.Count -> goal.value.takeUnless { it == 0L }?.toString().orEmpty()
        }
        val typeItems = availableGoalTypes.map {
            when (it) {
                is ChangeRecordTypeGoalsViewData.Type.Duration -> R.string.change_record_type_goal_duration
                is ChangeRecordTypeGoalsViewData.Type.Count -> R.string.change_record_type_goal_count
            }.let(resourceRepo::getString)
        }.map(CustomSpinner::CustomSpinnerTextItem)

        // Subtype Goal / Limit
        val subtypeItems = listOf(
            RecordTypeGoal.Subtype.Goal,
            RecordTypeGoal.Subtype.Limit,
        ).takeIf {
            goal.value > 0L
        }.orEmpty().map {
            val name = goalViewDataMapper.mapSubtype(it)
            ChangeRecordTypeGoalSubtypeViewData(
                subtype = it,
                name = name,
                isSelected = it::class.java == state.subtype::class.java,
                textSizeSp = null,
            )
        }

        // Range
        val rangeItems = goalRangeList.map {
            val name = goalViewDataMapper.mapType(it)
            CustomSpinner.CustomSpinnerTextItem(name)
        }
        val rangeSelectedPosition = goalRangeList
            .indexOfFirst { it::class.java == state.range::class.java }
            .takeUnless { it == -1 }.orZero()

        // Days
        val daysOfWeek = if (state.range is RecordTypeGoal.Range.Daily) {
            mapDaysOfWeekViewData(
                goal = goal,
                selectedDaysOfWeek = state.daysOfWeek,
                isDarkTheme = isDarkTheme,
                firstDayOfWeek = firstDayOfWeek,
            )
        } else {
            emptyList()
        }

        return ChangeRecordTypeGoalsViewData.GoalViewData(
            key = state.key,
            rangeItems = rangeItems,
            rangeSelectedPosition = rangeSelectedPosition,
            typeItems = typeItems,
            typeSelectedPosition = typeSelectedPosition,
            type = goalViewData,
            subtypeItems = subtypeItems,
            value = value,
            daysOfWeek = daysOfWeek,
            summary = mapSummary(state, firstDayOfWeek),
            isExpanded = isExpanded,
            requestScroll = state.requestScroll,
        )
    }

    private fun mapSummary(
        state: ChangeRecordTypeGoalsState.GoalState,
        firstDayOfWeek: DayOfWeek,
    ): CharSequence {
        val subtype = goalViewDataMapper.mapSubtype(state.subtype)
        val range = goalViewDataMapper.mapType(state.range)
        val value = when {
            state.type.value <= 0L -> {
                resourceRepo.getString(R.string.change_record_type_goal_time_disabled)
            }
            state.type is RecordTypeGoal.Type.Duration -> {
                timeMapper.formatDuration(state.type.value)
            }
            else -> {
                val count = state.type.value
                "$count " + resourceRepo.getQuantityString(
                    stringResId = R.plurals.statistics_detail_times_tracked,
                    quantity = count.toInt(),
                )
            }
        }.toSpannableString().setSpan(span = StyleSpan(BOLD))
        val days = if (state.range is RecordTypeGoal.Range.Daily && state.type.value > 0L) {
            timeMapper.formatDays(
                firstDayOfWeek = firstDayOfWeek,
                selectedDaysOfWeek = state.daysOfWeek,
            ).takeIf(String::isNotEmpty)
        } else {
            null
        }

        return listOfNotNull(
            subtype,
            range,
            value,
            days,
        ).joinToSpannable(separator = " · ")
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