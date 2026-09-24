package com.example.util.simpletimetracker.feature_notification.goalTime.interactor

import com.example.util.simpletimetracker.core.mapper.ColorMapper
import com.example.util.simpletimetracker.core.mapper.GoalViewDataMapper
import com.example.util.simpletimetracker.core.mapper.IconMapper
import com.example.util.simpletimetracker.core.mapper.RecordTagViewDataMapper
import com.example.util.simpletimetracker.core.mapper.TimeMapper
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.category.interactor.CategoryInteractor
import com.example.util.simpletimetracker.domain.color.model.AppColor
import com.example.util.simpletimetracker.domain.prefs.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.recordTag.interactor.RecordTagInteractor
import com.example.util.simpletimetracker.domain.recordType.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.domain.recordType.model.RecordTypeGoal
import com.example.util.simpletimetracker.feature_notification.R
import com.example.util.simpletimetracker.feature_notification.goalTime.manager.NotificationGoalTimeParams
import com.example.util.simpletimetracker.feature_views.GoalCheckmarkView
import com.example.util.simpletimetracker.feature_views.viewData.RecordTypeIcon
import javax.inject.Inject

class NotificationGoalParamsInteractor @Inject constructor(
    private val resourceRepo: ResourceRepo,
    private val recordTypeInteractor: RecordTypeInteractor,
    private val categoryInteractor: CategoryInteractor,
    private val recordTagInteractor: RecordTagInteractor,
    private val prefsInteractor: PrefsInteractor,
    private val timeMapper: TimeMapper,
    private val colorMapper: ColorMapper,
    private val iconMapper: IconMapper,
    private val recordTagViewDataMapper: RecordTagViewDataMapper,
    private val goalViewDataMapper: GoalViewDataMapper,
) {

    suspend fun execute(goal: RecordTypeGoal): NotificationGoalTimeParams? {
        data class DataHolder(
            val icon: String?,
            val color: AppColor,
            val name: String,
        )

        val dataHolder: DataHolder

        when (val idData = goal.idData) {
            is RecordTypeGoal.IdData.Type -> {
                val recordType = recordTypeInteractor.get(idData.value) ?: return null
                dataHolder = DataHolder(
                    icon = recordType.icon,
                    color = recordType.color,
                    name = recordType.name,
                )
            }
            is RecordTypeGoal.IdData.Category -> {
                val category = categoryInteractor.get(idData.value) ?: return null
                dataHolder = DataHolder(
                    icon = null,
                    color = category.color,
                    name = category.name,
                )
            }
            is RecordTypeGoal.IdData.Tag -> {
                val tag = recordTagInteractor.get(idData.value) ?: return null
                val colorSource = recordTypeInteractor.get(tag.iconColorSource)
                dataHolder = DataHolder(
                    icon = recordTagViewDataMapper.mapIcon(tag, colorSource),
                    color = recordTagViewDataMapper.mapColor(tag, colorSource),
                    name = tag.name,
                )
            }
        }
        val isDarkTheme = prefsInteractor.getDarkMode()

        val goalValueString = when (val type = goal.type) {
            // ex. 5h 30m
            is RecordTypeGoal.Type.Duration -> {
                type.value.let(timeMapper::formatDuration)
            }
            // ex. 3 Records
            is RecordTypeGoal.Type.Count -> {
                type.value.let {
                    "$it " + resourceRepo.getQuantityString(
                        stringResId = R.plurals.statistics_detail_times_tracked,
                        quantity = it.toInt(),
                    )
                }
            }
        }

        val goalTypeString = goalViewDataMapper.mapType(goal.range).let { "($it)" }

        val subtype = goal.subtype

        val goalSubtypeString = when (subtype) {
            is RecordTypeGoal.Subtype.Goal -> R.string.notification_goal_time_description
            is RecordTypeGoal.Subtype.Limit -> R.string.notification_limit_time_description
        }.let(resourceRepo::getString)

        val description = goalSubtypeString +
            " - " +
            goalValueString +
            " " +
            goalTypeString

        val checkState = when (subtype) {
            is RecordTypeGoal.Subtype.Goal -> GoalCheckmarkView.CheckState.GOAL_REACHED
            is RecordTypeGoal.Subtype.Limit -> GoalCheckmarkView.CheckState.LIMIT_REACHED
        }

        return NotificationGoalTimeParams(
            goalId = goal.id,
            goalType = goal.type,
            icon = dataHolder.icon
                ?.let(iconMapper::mapIcon)
                ?: RecordTypeIcon.Text(""),
            color = colorMapper.mapToColorInt(dataHolder.color, isDarkTheme),
            text = dataHolder.name,
            description = description,
            checkState = checkState,
        )
    }
}