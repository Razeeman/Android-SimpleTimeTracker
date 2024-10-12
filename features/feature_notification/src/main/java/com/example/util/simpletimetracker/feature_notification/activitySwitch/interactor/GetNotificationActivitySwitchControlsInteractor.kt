package com.example.util.simpletimetracker.feature_notification.activitySwitch.interactor

import com.example.util.simpletimetracker.core.interactor.CompleteTypesStateInteractor
import com.example.util.simpletimetracker.core.interactor.GetCurrentRecordsDurationInteractor
import com.example.util.simpletimetracker.core.mapper.ColorMapper
import com.example.util.simpletimetracker.core.mapper.IconMapper
import com.example.util.simpletimetracker.core.mapper.RecordTagViewDataMapper
import com.example.util.simpletimetracker.core.mapper.RecordTypeViewDataMapper
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.REPEAT_BUTTON_ITEM_ID
import com.example.util.simpletimetracker.domain.model.RecordTag
import com.example.util.simpletimetracker.domain.model.RecordType
import com.example.util.simpletimetracker.domain.model.RecordTypeGoal
import com.example.util.simpletimetracker.domain.model.RunningRecord
import com.example.util.simpletimetracker.feature_notification.R
import com.example.util.simpletimetracker.feature_notification.activitySwitch.manager.NotificationControlsParams
import com.example.util.simpletimetracker.feature_views.viewData.RecordTypeIcon
import javax.inject.Inject

class GetNotificationActivitySwitchControlsInteractor @Inject constructor(
    private val iconMapper: IconMapper,
    private val colorMapper: ColorMapper,
    private val recordTypeViewDataMapper: RecordTypeViewDataMapper,
    private val resourceRepo: ResourceRepo,
    private val recordTagViewDataMapper: RecordTagViewDataMapper,
    private val completeTypesStateInteractor: CompleteTypesStateInteractor,
) {

    fun getControls(
        hintIsVisible: Boolean,
        isDarkTheme: Boolean,
        types: List<RecordType>,
        runningRecords: List<RunningRecord> = emptyList(),
        showRepeatButton: Boolean,
        typesShift: Int = 0,
        tags: List<RecordTag> = emptyList(),
        tagsShift: Int = 0,
        selectedTypeId: Long? = null,
        goals: Map<Long, List<RecordTypeGoal>>,
        allDailyCurrents: Map<Long, GetCurrentRecordsDurationInteractor.Result>,
    ): NotificationControlsParams {
        val typesMap = types.associateBy { it.id }
        val runningRecordsIds = runningRecords.map { it.id }

        val repeatButtonViewData = if (showRepeatButton) {
            val viewData = recordTypeViewDataMapper.mapToRepeatItem(
                numberOfCards = 0,
                isDarkTheme = isDarkTheme,
            )
            NotificationControlsParams.Type(
                id = REPEAT_BUTTON_ITEM_ID,
                icon = viewData.iconId,
                color = viewData.color,
                isChecked = null,
                isComplete = false,
            ).let(::listOf)
        } else {
            emptyList()
        }

        val typesViewData = types
            .filter { !it.hidden }
            .map { type ->
                NotificationControlsParams.Type(
                    id = type.id,
                    icon = type.icon.let(iconMapper::mapIcon),
                    color = if (type.id in runningRecordsIds) {
                        colorMapper.toFilteredColor(isDarkTheme)
                    } else {
                        type.color.let { colorMapper.mapToColorInt(it, isDarkTheme) }
                    },
                    isChecked = recordTypeViewDataMapper.mapGoalCheckmark(
                        type = type,
                        goals = goals,
                        allDailyCurrents = allDailyCurrents,
                    ),
                    isComplete = type.id in completeTypesStateInteractor.notificationTypeIds,
                )
            }

        val tagsViewData = tags
            .filter { !it.archived }
            .map { tag ->
                NotificationControlsParams.Tag(
                    id = tag.id,
                    text = tag.name,
                    color = recordTagViewDataMapper.mapColor(
                        tag = tag,
                        types = typesMap,
                    ).let { colorMapper.mapToColorInt(it, isDarkTheme) },
                )
            }
            .let {
                if (it.isNotEmpty()) {
                    val untagged = NotificationControlsParams.Tag(
                        id = 0L,
                        text = R.string.change_record_untagged.let(resourceRepo::getString),
                        color = colorMapper.toUntrackedColor(isDarkTheme),
                    ).let(::listOf)
                    untagged + it
                } else {
                    it
                }
            }
        return NotificationControlsParams.Enabled(
            hintIsVisible = hintIsVisible,
            types = repeatButtonViewData + typesViewData,
            typesShift = typesShift,
            tags = tagsViewData,
            tagsShift = tagsShift,
            controlIconPrev = RecordTypeIcon.Image(R.drawable.arrow_left),
            controlIconNext = RecordTypeIcon.Image(R.drawable.arrow_right),
            controlIconColor = colorMapper.toInactiveColor(isDarkTheme),
            selectedTypeId = selectedTypeId,
        )
    }
}