package com.example.util.simpletimetracker.core.mapper

import androidx.annotation.ColorInt
import androidx.annotation.StringRes
import com.example.util.simpletimetracker.core.R
import com.example.util.simpletimetracker.core.interactor.GetCurrentRecordsDurationInteractor
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.extension.getDaily
import com.example.util.simpletimetracker.domain.extension.orZero
import com.example.util.simpletimetracker.domain.extension.value
import com.example.util.simpletimetracker.domain.model.AppColor
import com.example.util.simpletimetracker.domain.model.RecordType
import com.example.util.simpletimetracker.domain.model.RecordTypeGoal
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.empty.EmptyViewData
import com.example.util.simpletimetracker.feature_base_adapter.recordType.RecordTypeViewData
import com.example.util.simpletimetracker.feature_base_adapter.recordTypeSpecial.RunningRecordTypeSpecialViewData
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
        isDarkTheme: Boolean,
    ): RecordTypeViewData {
        return RecordTypeViewData(
            id = recordType.id,
            name = recordType.name,
            iconId = iconMapper.mapIcon(recordType.icon),
            iconColor = colorMapper.toIconColor(isDarkTheme),
            color = mapColor(recordType.color, isDarkTheme),
        )
    }

    fun map(
        recordType: RecordType,
        numberOfCards: Int,
        isDarkTheme: Boolean,
        isChecked: Boolean?,
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
            isChecked = isChecked,
        )
    }

    fun mapFiltered(
        recordType: RecordType,
        numberOfCards: Int,
        isDarkTheme: Boolean,
        isFiltered: Boolean,
        isChecked: Boolean?,
    ): RecordTypeViewData {
        val default = map(
            recordType = recordType,
            numberOfCards = numberOfCards,
            isDarkTheme = isDarkTheme,
            isChecked = isChecked,
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
            isChecked = null,
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
            isChecked = null,
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
            isChecked = null,
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
            // Somewhat weird logic, null - means do not show, false - red dot not checked.
            isChecked = if (isPomodoroStarted) false else null,
        )
    }

    fun mapGoalCheckmark(
        type: RecordType,
        goals: Map<Long, List<RecordTypeGoal>>,
        allDailyCurrents: Map<Long, GetCurrentRecordsDurationInteractor.Result>,
    ): Boolean? {
        return mapGoalCheckmark(
            goal = goals[type.id].orEmpty().getDaily(),
            dailyCurrent = allDailyCurrents[type.id],
        )
    }

    fun mapGoalCheckmark(
        goal: RecordTypeGoal?,
        dailyCurrent: GetCurrentRecordsDurationInteractor.Result?,
    ): Boolean? {
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
        val valueLeft = goalValue - current

        return if (goal != null) valueLeft <= 0L else null
    }

    private fun mapToSpecial(
        type: RunningRecordTypeSpecialViewData.Type,
        @StringRes name: Int,
        icon: RecordTypeIcon,
        numberOfCards: Int,
        isDarkTheme: Boolean,
        isChecked: Boolean?,
    ): RunningRecordTypeSpecialViewData {
        return RunningRecordTypeSpecialViewData(
            type = type,
            name = name.let(resourceRepo::getString),
            iconId = icon,
            color = colorMapper.toInactiveColor(isDarkTheme),
            width = recordTypeCardSizeMapper.toCardWidth(numberOfCards),
            height = recordTypeCardSizeMapper.toCardHeight(numberOfCards),
            asRow = recordTypeCardSizeMapper.toCardAsRow(numberOfCards),
            isChecked = isChecked,
        )
    }

    @ColorInt
    private fun mapColor(color: AppColor, isDarkTheme: Boolean): Int {
        return colorMapper.mapToColorInt(color, isDarkTheme)
    }
}