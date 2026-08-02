package com.example.util.simpletimetracker.feature_change_record.mapper

import com.example.util.simpletimetracker.core.mapper.ChangeRecordDateTimeMapper
import com.example.util.simpletimetracker.core.mapper.RecordQuickActionMapper
import com.example.util.simpletimetracker.core.mapper.RecordViewDataMapper
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.base.DurationFormat
import com.example.util.simpletimetracker.domain.color.model.AppColor
import com.example.util.simpletimetracker.domain.record.model.Record
import com.example.util.simpletimetracker.domain.recordAction.model.RecordQuickAction
import com.example.util.simpletimetracker.domain.recordTag.model.RecordTag
import com.example.util.simpletimetracker.domain.recordType.model.RecordType
import com.example.util.simpletimetracker.feature_base_adapter.button.ButtonViewData
import com.example.util.simpletimetracker.feature_change_record.R
import com.example.util.simpletimetracker.feature_change_record.api.model.ChangeRecordActionsBlock
import com.example.util.simpletimetracker.feature_change_record.api.model.ChangeRecordDateTimeFieldsState
import com.example.util.simpletimetracker.feature_change_record.api.viewData.ChangeRecordSimpleViewData
import com.example.util.simpletimetracker.feature_change_record.viewData.ChangeRecordQuickActionsButtonViewData
import com.example.util.simpletimetracker.feature_change_record.viewData.ChangeRecordViewData
import javax.inject.Inject

class ChangeRecordViewDataMapper @Inject constructor(
    private val resourceRepo: ResourceRepo,
    private val recordViewDataMapper: RecordViewDataMapper,
    private val changeRecordDateTimeMapper: ChangeRecordDateTimeMapper,
    private val recordQuickActionMapper: RecordQuickActionMapper,
) {

    fun map(
        record: Record,
        recordType: RecordType?,
        recordTags: List<RecordTag>,
        isDarkTheme: Boolean,
        useMilitaryTime: Boolean,
        durationFormat: DurationFormat,
        showSeconds: Boolean,
        dateTimeFieldState: ChangeRecordDateTimeFieldsState,
    ): ChangeRecordViewData {
        // Type data for untracked will be placed later.
        val emptyTypeForUntracked = RecordType(
            id = 0,
            name = "",
            icon = "",
            color = AppColor(colorId = 0, colorInt = ""),
            defaultDuration = 0,
            note = "",
        )
        val recordPreview = recordViewDataMapper.map(
            record = record,
            recordType = recordType ?: emptyTypeForUntracked,
            recordTags = recordTags,
            isDarkTheme = isDarkTheme,
            useMilitaryTime = useMilitaryTime,
            durationFormat = durationFormat,
            showSeconds = showSeconds,
        ).let {
            // TODO do better
            if (recordType == null) {
                val untrackedPreview = recordViewDataMapper.mapToUntracked(
                    timeStarted = record.timeStarted,
                    timeEnded = record.timeEnded,
                    isDarkTheme = isDarkTheme,
                    useMilitaryTime = useMilitaryTime,
                    durationFormat = durationFormat,
                    showSeconds = showSeconds,
                )
                it.copy(
                    name = untrackedPreview.name,
                    color = untrackedPreview.color,
                    iconId = untrackedPreview.iconId,
                )
            } else {
                it
            }
        }

        return ChangeRecordViewData(
            recordPreview = recordPreview,
            dateTimeStarted = changeRecordDateTimeMapper.map(
                param = when (dateTimeFieldState.start) {
                    is ChangeRecordDateTimeFieldsState.State.DateTime -> {
                        ChangeRecordDateTimeMapper.Param.DateTime(record.timeStarted)
                    }
                    is ChangeRecordDateTimeFieldsState.State.Duration -> {
                        ChangeRecordDateTimeMapper.Param.Duration(record.duration)
                    }
                },
                field = ChangeRecordDateTimeMapper.Field.Start,
                useMilitaryTimeFormat = useMilitaryTime,
                showSeconds = showSeconds,
                durationFormat = durationFormat,
            ),
            dateTimeFinished = changeRecordDateTimeMapper.map(
                param = when (dateTimeFieldState.end) {
                    is ChangeRecordDateTimeFieldsState.State.DateTime -> {
                        ChangeRecordDateTimeMapper.Param.DateTime(record.timeEnded)
                    }
                    is ChangeRecordDateTimeFieldsState.State.Duration -> {
                        ChangeRecordDateTimeMapper.Param.Duration(record.duration)
                    }
                },
                field = ChangeRecordDateTimeMapper.Field.End,
                useMilitaryTimeFormat = useMilitaryTime,
                showSeconds = showSeconds,
                durationFormat = durationFormat,
            ),
        )
    }

    fun mapSimple(
        preview: ChangeRecordViewData,
        showTimeEnded: Boolean,
        timeStartedChanged: Boolean,
        timeEndedChanged: Boolean,
    ): ChangeRecordSimpleViewData {
        return ChangeRecordSimpleViewData(
            name = preview.recordPreview.name,
            timeStarted = preview.recordPreview.timeStarted,
            timeEnded = if (showTimeEnded) {
                preview.recordPreview.timeFinished
            } else {
                ""
            },
            timeStartedChanged = timeStartedChanged,
            timeEndedChanged = timeEndedChanged,
            duration = preview.recordPreview.duration,
            iconId = preview.recordPreview.iconId,
            color = preview.recordPreview.color,
        )
    }

    fun mapRecordActionButton(
        action: RecordQuickAction,
        isEnabled: Boolean,
        isDarkTheme: Boolean,
    ): ButtonViewData? {
        return ButtonViewData(
            id = ChangeRecordQuickActionsButtonViewData(
                block = mapRecordAction(action) ?: return null,
            ),
            text = recordQuickActionMapper.mapText(action),
            icon = ButtonViewData.Icon.Present(
                icon = recordQuickActionMapper.mapIcon(action),
                iconColor = resourceRepo.getThemedAttr(R.attr.appLightTextColor, isDarkTheme),
                iconBackgroundColor = resourceRepo.getThemedAttr(R.attr.appInactiveColor, isDarkTheme),
            ),
            backgroundColor = resourceRepo.getThemedAttr(R.attr.appActiveColor, isDarkTheme),
            isEnabled = isEnabled,
            marginHorizontalDp = 4,
        )
    }

    private fun mapRecordAction(
        action: RecordQuickAction,
    ): ChangeRecordActionsBlock? {
        return when (action) {
            RecordQuickAction.CONTINUE -> ChangeRecordActionsBlock.ContinueButton
            RecordQuickAction.REPEAT -> ChangeRecordActionsBlock.RepeatButton
            RecordQuickAction.DUPLICATE -> ChangeRecordActionsBlock.DuplicateButton
            RecordQuickAction.MOVE -> ChangeRecordActionsBlock.MoveButton
            RecordQuickAction.MERGE -> ChangeRecordActionsBlock.MergeButton
            RecordQuickAction.SPLIT -> ChangeRecordActionsBlock.SplitButton
            RecordQuickAction.ADJUST -> ChangeRecordActionsBlock.AdjustButton
            RecordQuickAction.SHORTCUT -> ChangeRecordActionsBlock.ShortcutButton
            RecordQuickAction.STOP -> null
            RecordQuickAction.MULTISELECT -> null
            RecordQuickAction.CHANGE_ACTIVITY -> null
            RecordQuickAction.CHANGE_TAG -> null
        }
    }
}