package com.example.util.simpletimetracker.core.mapper

import androidx.annotation.ColorInt
import androidx.annotation.StringRes
import com.example.util.simpletimetracker.core.R
import com.example.util.simpletimetracker.core.interactor.GetCurrentRecordsDurationInteractor
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.base.ARCHIVED_BUTTON_ITEM_ID
import com.example.util.simpletimetracker.domain.recordType.extension.getDaily
import com.example.util.simpletimetracker.domain.extension.orFalse
import com.example.util.simpletimetracker.domain.extension.orZero
import com.example.util.simpletimetracker.domain.recordType.extension.isReached
import com.example.util.simpletimetracker.domain.color.model.AppColor
import com.example.util.simpletimetracker.domain.recordType.extension.adjustedValue
import com.example.util.simpletimetracker.domain.recordType.model.RecordType
import com.example.util.simpletimetracker.domain.recordType.model.RecordTypeGoal
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.empty.EmptyViewData
import com.example.util.simpletimetracker.feature_base_adapter.recordType.RecordTypeViewData
import com.example.util.simpletimetracker.feature_base_adapter.recordTypeSpecial.RunningRecordTypeSpecialViewData
import com.example.util.simpletimetracker.feature_views.GoalCheckmarkView
import com.example.util.simpletimetracker.feature_views.viewData.RecordTypeIcon
import javax.inject.Inject

class RecordTypeViewDataMapper @Inject constructor(
    private val iconMapper: IconMapper,
    private val colorMapper: ColorMapper,
    private val resourceRepo: ResourceRepo,
    private val recordTypeCardSizeMapper: RecordTypeCardSizeMapper,
) {

    fun mapToEmpty(): List<ViewHolderType> {
        return EmptyViewData(
            message = resourceRepo.getString(R.string.record_types_empty),
        ).let(::listOf)
    }

    fun map(
        recordType: RecordType,
        numberOfCards: Int,
        isDarkTheme: Boolean,
    ): RecordTypeViewData {
        return map(
            recordType = recordType,
            numberOfCards = numberOfCards,
            isDarkTheme = isDarkTheme,
            checkStates = emptyList(),
            isComplete = false,
        )
    }

    fun map(
        recordType: RecordType,
        numberOfCards: Int,
        isDarkTheme: Boolean,
        checkStates: List<GoalCheckmarkView.CheckState>,
        isComplete: Boolean,
    ): RecordTypeViewData {
        return RecordTypeViewData(
            id = recordType.id,
            name = recordType.name,
            iconId = iconMapper.mapIcon(recordType.icon),
            iconColor = colorMapper.toIconColor(isDarkTheme),
            color = mapColor(recordType.color, isDarkTheme),
            width = recordTypeCardSizeMapper.toCardWidth(numberOfCards),
            height = recordTypeCardSizeMapper.toCardHeight(numberOfCards),
            asRow = recordTypeCardSizeMapper.toCardAsRow(numberOfCards),
            checkStates = checkStates,
            isComplete = isComplete,
        )
    }

    fun mapFiltered(
        recordType: RecordType,
        numberOfCards: Int,
        isDarkTheme: Boolean,
        isFiltered: Boolean,
        checkStates: List<GoalCheckmarkView.CheckState>,
        isComplete: Boolean,
    ): RecordTypeViewData {
        val default = map(
            recordType = recordType,
            numberOfCards = numberOfCards,
            isDarkTheme = isDarkTheme,
            checkStates = checkStates,
            isComplete = isComplete,
        )

        return if (isFiltered) {
            default.copy(
                color = colorMapper.toFilteredColor(isDarkTheme),
                iconColor = colorMapper.toFilteredIconColor(isDarkTheme),
                iconAlpha = colorMapper.toIconAlpha(default.iconId, true),
                itemIsFiltered = true,
            )
        } else {
            default
        }
    }

    fun mapToAddItem(
        numberOfCards: Int,
        isDarkTheme: Boolean,
    ): RunningRecordTypeSpecialViewData {
        return mapToSpecial(
            type = RunningRecordTypeSpecialViewData.Type.Add,
            name = R.string.running_records_add_type,
            icon = RecordTypeIcon.Image(R.drawable.add),
            numberOfCards = numberOfCards,
            isDarkTheme = isDarkTheme,
            checkStates = emptyList(),
        )
    }

    fun mapToAddDefaultItem(
        numberOfCards: Int,
        isDarkTheme: Boolean,
    ): RunningRecordTypeSpecialViewData {
        return mapToSpecial(
            type = RunningRecordTypeSpecialViewData.Type.Default,
            name = R.string.running_records_add_default,
            icon = RecordTypeIcon.Image(R.drawable.add),
            numberOfCards = numberOfCards,
            isDarkTheme = isDarkTheme,
            checkStates = emptyList(),
        )
    }

    fun mapToRepeatItem(
        numberOfCards: Int,
        isDarkTheme: Boolean,
    ): RunningRecordTypeSpecialViewData {
        return mapToSpecial(
            type = RunningRecordTypeSpecialViewData.Type.Repeat,
            name = R.string.running_records_repeat,
            icon = RecordTypeIcon.Image(R.drawable.repeat),
            numberOfCards = numberOfCards,
            isDarkTheme = isDarkTheme,
            checkStates = emptyList(),
        )
    }

    fun mapToPomodoroItem(
        numberOfCards: Int,
        isDarkTheme: Boolean,
        isPomodoroStarted: Boolean,
    ): RunningRecordTypeSpecialViewData {
        return mapToSpecial(
            type = RunningRecordTypeSpecialViewData.Type.Pomodoro,
            name = R.string.running_records_pomodoro,
            icon = RecordTypeIcon.Image(R.drawable.pomodoro),
            numberOfCards = numberOfCards,
            isDarkTheme = isDarkTheme,
            // Somewhat weird logic, GOAL_NOT_REACHED - red dot not checked.
            checkStates = if (isPomodoroStarted) {
                listOf(GoalCheckmarkView.CheckState.GOAL_NOT_REACHED)
            } else {
                emptyList()
            },
        )
    }

    fun mapToArchivedItem(
        isEnabled: Boolean,
        numberOfCards: Int,
        isDarkTheme: Boolean,
    ): RecordTypeViewData {
        return RecordTypeViewData(
            id = ARCHIVED_BUTTON_ITEM_ID,
            name = R.string.settings_archive
                .let(resourceRepo::getString),
            iconId = RecordTypeIcon.Image(R.drawable.archive),
            iconColor = colorMapper.toIconColor(isDarkTheme),
            color = if (isEnabled) {
                colorMapper.toActiveColor(isDarkTheme)
            } else {
                colorMapper.toInactiveColor(isDarkTheme)
            },
            width = recordTypeCardSizeMapper.toCardWidth(numberOfCards),
            height = recordTypeCardSizeMapper.toCardHeight(numberOfCards),
            asRow = recordTypeCardSizeMapper.toCardAsRow(numberOfCards),
        )
    }

    fun mapGoalCheckmarks(
        type: RecordType,
        goals: Map<Long, List<RecordTypeGoal>>,
        allDailyCurrents: Map<Long, GetCurrentRecordsDurationInteractor.Result>,
    ): List<GoalCheckmarkView.CheckState> {
        return mapGoalCheckmarks(
            goals = goals[type.id].orEmpty(),
            dailyCurrent = allDailyCurrents[type.id],
        )
    }

    fun mapGoalCheckmarks(
        goals: List<RecordTypeGoal>,
        dailyCurrent: GetCurrentRecordsDurationInteractor.Result?,
    ): List<GoalCheckmarkView.CheckState> {
        val dailyGoals = goals.getDaily()
        val goalEntries = dailyGoals.filter { it.subtype is RecordTypeGoal.Subtype.Goal }
        val limitEntries = dailyGoals.filter { it.subtype is RecordTypeGoal.Subtype.Limit }

        val goalsCheck = if (goalEntries.isNotEmpty()) {
            val allReached = goalEntries.all { isReached(it, dailyCurrent) }
            if (allReached) {
                GoalCheckmarkView.CheckState.GOAL_REACHED
            } else {
                GoalCheckmarkView.CheckState.GOAL_NOT_REACHED
            }
        } else {
            null
        }
        val limitsCheck = if (limitEntries.isNotEmpty()) {
            val anyReached = limitEntries.any { isReached(it, dailyCurrent) }
            if (anyReached) {
                GoalCheckmarkView.CheckState.LIMIT_REACHED
            } else {
                GoalCheckmarkView.CheckState.LIMIT_NOT_REACHED
            }
        } else {
            null
        }

        return listOfNotNull(goalsCheck, limitsCheck)
    }

    private fun isReached(
        goal: RecordTypeGoal,
        dailyCurrent: GetCurrentRecordsDurationInteractor.Result?,
    ): Boolean {
        val current = when (goal.type) {
            is RecordTypeGoal.Type.Duration -> dailyCurrent?.duration.orZero()
            is RecordTypeGoal.Type.Count -> dailyCurrent?.count.orZero()
        }
        return goal.subtype.isReached(current, goal.adjustedValue)
    }

    private fun mapToSpecial(
        type: RunningRecordTypeSpecialViewData.Type,
        @StringRes name: Int,
        icon: RecordTypeIcon,
        numberOfCards: Int,
        isDarkTheme: Boolean,
        checkStates: List<GoalCheckmarkView.CheckState>,
    ): RunningRecordTypeSpecialViewData {
        return RunningRecordTypeSpecialViewData(
            type = type,
            name = name.let(resourceRepo::getString),
            iconId = icon,
            color = colorMapper.toInactiveColor(isDarkTheme),
            width = numberOfCards.let(recordTypeCardSizeMapper::toCardWidth),
            height = numberOfCards.let(recordTypeCardSizeMapper::toCardHeight),
            asRow = numberOfCards.let(recordTypeCardSizeMapper::toCardAsRow).orFalse(),
            checkStates = checkStates,
        )
    }

    @ColorInt
    private fun mapColor(color: AppColor, isDarkTheme: Boolean): Int {
        return colorMapper.mapToColorInt(color, isDarkTheme)
    }
}