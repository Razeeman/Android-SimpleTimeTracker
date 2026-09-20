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
import com.example.util.simpletimetracker.domain.recordType.extension.value
import com.example.util.simpletimetracker.domain.color.model.AppColor
import com.example.util.simpletimetracker.domain.recordType.extension.getLongest
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
            checkState = GoalCheckmarkView.CheckState.HIDDEN,
            isComplete = false,
        )
    }

    fun map(
        recordType: RecordType,
        numberOfCards: Int,
        isDarkTheme: Boolean,
        checkState: GoalCheckmarkView.CheckState,
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
            checkState = checkState,
            isComplete = isComplete,
        )
    }

    fun mapFiltered(
        recordType: RecordType,
        numberOfCards: Int,
        isDarkTheme: Boolean,
        isFiltered: Boolean,
        checkState: GoalCheckmarkView.CheckState,
        isComplete: Boolean,
    ): RecordTypeViewData {
        val default = map(
            recordType = recordType,
            numberOfCards = numberOfCards,
            isDarkTheme = isDarkTheme,
            checkState = checkState,
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
            checkState = GoalCheckmarkView.CheckState.HIDDEN,
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
            checkState = GoalCheckmarkView.CheckState.HIDDEN,
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
            checkState = GoalCheckmarkView.CheckState.HIDDEN,
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
            checkState = if (isPomodoroStarted) {
                GoalCheckmarkView.CheckState.GOAL_NOT_REACHED
            } else {
                GoalCheckmarkView.CheckState.HIDDEN
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

    fun mapGoalCheckmark(
        type: RecordType,
        goals: Map<Long, List<RecordTypeGoal>>,
        allDailyCurrents: Map<Long, GetCurrentRecordsDurationInteractor.Result>,
    ): GoalCheckmarkView.CheckState {
        return mapGoalCheckmark(
            goal = goals[type.id].orEmpty().getDaily().getLongest(),
            dailyCurrent = allDailyCurrents[type.id],
        )
    }

    fun mapGoalCheckmark(
        goal: RecordTypeGoal?,
        dailyCurrent: GetCurrentRecordsDurationInteractor.Result?,
    ): GoalCheckmarkView.CheckState {
        val goalValue = when (goal?.type) {
            is RecordTypeGoal.Type.Duration -> goal.value * 1000
            is RecordTypeGoal.Type.Count -> goal.value
            else -> 0
        }
        val current = when (goal?.type) {
            is RecordTypeGoal.Type.Duration -> dailyCurrent?.duration.orZero()
            is RecordTypeGoal.Type.Count -> dailyCurrent?.count.orZero()
            else -> 0
        }
        val isLimit = goal?.subtype == RecordTypeGoal.Subtype.Limit

        // TODO GOAL detailed stats, excess graph, count deficit when should have a goal.
        // TODO GOAL streaks, skip count days when should not have a goal (daily goals).
        return if (goal != null) {
            if (goal.subtype.isReached(current, goalValue)) {
                if (isLimit) {
                    GoalCheckmarkView.CheckState.LIMIT_REACHED
                } else {
                    GoalCheckmarkView.CheckState.GOAL_REACHED
                }
            } else {
                if (isLimit) {
                    GoalCheckmarkView.CheckState.LIMIT_NOT_REACHED
                } else {
                    GoalCheckmarkView.CheckState.GOAL_NOT_REACHED
                }
            }
        } else {
            GoalCheckmarkView.CheckState.HIDDEN
        }
    }

    private fun mapToSpecial(
        type: RunningRecordTypeSpecialViewData.Type,
        @StringRes name: Int,
        icon: RecordTypeIcon,
        numberOfCards: Int,
        isDarkTheme: Boolean,
        checkState: GoalCheckmarkView.CheckState,
    ): RunningRecordTypeSpecialViewData {
        return RunningRecordTypeSpecialViewData(
            type = type,
            name = name.let(resourceRepo::getString),
            iconId = icon,
            color = colorMapper.toInactiveColor(isDarkTheme),
            width = numberOfCards.let(recordTypeCardSizeMapper::toCardWidth),
            height = numberOfCards.let(recordTypeCardSizeMapper::toCardHeight),
            asRow = numberOfCards.let(recordTypeCardSizeMapper::toCardAsRow).orFalse(),
            checkState = checkState,
        )
    }

    @ColorInt
    private fun mapColor(color: AppColor, isDarkTheme: Boolean): Int {
        return colorMapper.mapToColorInt(color, isDarkTheme)
    }
}