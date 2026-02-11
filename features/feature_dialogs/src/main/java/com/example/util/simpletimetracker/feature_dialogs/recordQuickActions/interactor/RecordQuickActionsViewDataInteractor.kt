package com.example.util.simpletimetracker.feature_dialogs.recordQuickActions.interactor

import android.text.SpannableStringBuilder
import androidx.core.text.bold
import com.example.util.simpletimetracker.core.extension.toViewData
import com.example.util.simpletimetracker.core.mapper.RecordQuickActionMapper
import com.example.util.simpletimetracker.core.mapper.RecordViewDataMapper
import com.example.util.simpletimetracker.core.mapper.TimeMapper
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.base.DurationFormat
import com.example.util.simpletimetracker.domain.prefs.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.record.interactor.RecordInteractor
import com.example.util.simpletimetracker.domain.record.interactor.RecordsContainerMultiselectInteractor
import com.example.util.simpletimetracker.domain.record.interactor.RunningRecordInteractor
import com.example.util.simpletimetracker.domain.record.model.MultiSelectedRecordId
import com.example.util.simpletimetracker.domain.record.model.RecordBase
import com.example.util.simpletimetracker.domain.recordAction.model.RecordQuickAction
import com.example.util.simpletimetracker.domain.recordTag.interactor.GetSelectableTagsInteractor
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_dialogs.R
import com.example.util.simpletimetracker.feature_dialogs.recordQuickActions.adapter.RecordQuickActionsButtonBigViewData
import com.example.util.simpletimetracker.feature_dialogs.recordQuickActions.adapter.RecordQuickActionsButtonViewData
import com.example.util.simpletimetracker.feature_dialogs.recordQuickActions.adapter.RecordQuickActionsWidthHolder
import com.example.util.simpletimetracker.feature_dialogs.recordQuickActions.model.RecordQuickActionsButton
import com.example.util.simpletimetracker.feature_dialogs.recordQuickActions.model.RecordQuickActionsState
import com.example.util.simpletimetracker.feature_views.extension.image
import com.example.util.simpletimetracker.navigation.params.screen.RecordQuickActionsParams
import com.example.util.simpletimetracker.navigation.params.screen.RecordQuickActionsParams.Type
import javax.inject.Inject

class RecordQuickActionsViewDataInteractor @Inject constructor(
    private val timeMapper: TimeMapper,
    private val resourceRepo: ResourceRepo,
    private val recordViewDataMapper: RecordViewDataMapper,
    private val prefsInteractor: PrefsInteractor,
    private val recordInteractor: RecordInteractor,
    private val runningRecordInteractor: RunningRecordInteractor,
    private val recordQuickActionMapper: RecordQuickActionMapper,
    private val getSelectableTagsInteractor: GetSelectableTagsInteractor,
    private val recordsContainerMultiselectInteractor: RecordsContainerMultiselectInteractor,
) {

    suspend fun getRecord(
        extra: RecordQuickActionsParams,
    ): RecordBase? {
        return when (val params = extra.type) {
            is Type.RecordTracked -> recordInteractor.get(params.id)
            is Type.RecordUntracked -> null
            is Type.RecordRunning -> runningRecordInteractor.get(params.id)
        }
    }

    suspend fun getRecords(
        ids: List<MultiSelectedRecordId>,
    ): List<RecordBase> {
        return ids.mapNotNull {
            when (it) {
                is MultiSelectedRecordId.Tracked -> recordInteractor.get(it.id)
                is MultiSelectedRecordId.Untracked -> null
                is MultiSelectedRecordId.Running -> runningRecordInteractor.get(it.id)
            }
        }
    }

    fun getParamsList(
        extra: RecordQuickActionsParams,
    ): List<Type> {
        return if (recordsContainerMultiselectInteractor.isEnabled) {
            recordsContainerMultiselectInteractor.selectedRecordIds.map {
                when (it) {
                    is MultiSelectedRecordId.Tracked -> Type.RecordTracked(it.id)
                    is MultiSelectedRecordId.Running -> Type.RecordRunning(it.id)
                    is MultiSelectedRecordId.Untracked -> Type.RecordUntracked(
                        timeStarted = it.timeStartedTimestamp,
                        timeEnded = it.timeEndedTimestamp,
                    )
                }
            }
        } else {
            listOfNotNull(extra.type)
        }
    }

    suspend fun getViewData(
        extra: RecordQuickActionsParams,
    ): RecordQuickActionsState {
        val isDarkTheme = prefsInteractor.getDarkMode()
        val useMilitaryTime = prefsInteractor.getUseMilitaryTimeFormat()
        val showSeconds = prefsInteractor.getShowSeconds()
        val durationFormat = prefsInteractor.getDurationFormat()
        val retroactiveTrackingModeEnabled = prefsInteractor.getRetroactiveTrackingMode()
        val multiSelectEnabled = recordsContainerMultiselectInteractor.isEnabled
        val canContinue = !retroactiveTrackingModeEnabled
        val allowedButtons = if (multiSelectEnabled) {
            val multiSelectedIds = recordsContainerMultiselectInteractor.selectedRecordIds
            val hasTags = getRecords(multiSelectedIds).map { it.typeIds }
                .flatten().distinct()
                .let { getSelectableTagsInteractor.execute(*it.toLongArray()) }
                .any { !it.archived }
            getAllowedInMultiselectButtons(
                hasTags = hasTags,
                multiSelectedIds = multiSelectedIds,
            )
        } else {
            val hasTags = getRecord(extra)?.typeIds?.firstOrNull()
                ?.let { getSelectableTagsInteractor.execute(it) }
                .orEmpty().any { !it.archived }
            getAllowedButtons(
                extra = extra,
                canContinue = canContinue,
                hasTags = hasTags,
            )
        }
        val buttons = getAllButtons(
            allowedButtons = allowedButtons,
        ).let(::applyWidth)
        val helpData = mapHelpData(
            allowedButtons = allowedButtons,
            isDarkTheme = isDarkTheme,
        )
        val hintData = mapHintData(
            extra = extra,
            record = getRecord(extra),
            useMilitaryTime = useMilitaryTime,
            showSeconds = showSeconds,
            durationFormat = durationFormat,
        )

        return RecordQuickActionsState(
            buttons = buttons,
            helpData = helpData,
            hintData = hintData,
        )
    }

    private fun getAllButtons(
        allowedButtons: List<RecordQuickActionsButton>,
    ): List<ViewHolderType> {
        val allActionsOrder = listOf(
            RecordQuickActionsButton.STATISTICS,
            RecordQuickActionsButton.DELETE,
            RecordQuickActionsButton.CONTINUE,
            RecordQuickActionsButton.REPEAT,
            RecordQuickActionsButton.DUPLICATE,
            RecordQuickActionsButton.MOVE,
            RecordQuickActionsButton.MERGE,
            RecordQuickActionsButton.STOP,
            RecordQuickActionsButton.MULTISELECT,
            RecordQuickActionsButton.CHANGE_ACTIVITY,
            RecordQuickActionsButton.CHANGE_TAG,
        ).filter {
            it in allowedButtons
        }

        return allActionsOrder.mapNotNull {
            when (it) {
                RecordQuickActionsButton.STATISTICS -> {
                    RecordQuickActionsButtonBigViewData(
                        block = it,
                        text = R.string.shortcut_navigation_statistics.let(resourceRepo::getString),
                        icon = R.drawable.statistics,
                    )
                }
                RecordQuickActionsButton.DELETE -> {
                    RecordQuickActionsButtonBigViewData(
                        block = it,
                        text = R.string.archive_dialog_delete.let(resourceRepo::getString),
                        icon = R.drawable.delete,
                    )
                }
                else -> {
                    val action = mapAction(it) ?: return@mapNotNull null
                    RecordQuickActionsButtonViewData(
                        block = it,
                        text = recordQuickActionMapper.mapText(action),
                        icon = recordQuickActionMapper.mapIcon(action),
                    )
                }
            }
        }
    }

    private fun getAllowedInMultiselectButtons(
        hasTags: Boolean,
        multiSelectedIds: List<MultiSelectedRecordId>,
    ): List<RecordQuickActionsButton> {
        return listOfNotNull(
            RecordQuickActionsButton.DELETE.takeIf {
                multiSelectedIds.all { it is MultiSelectedRecordId.Tracked }
            },
            RecordQuickActionsButton.DUPLICATE.takeIf {
                multiSelectedIds.all { it is MultiSelectedRecordId.Tracked }
            },
            RecordQuickActionsButton.MOVE.takeIf {
                multiSelectedIds.all { it is MultiSelectedRecordId.Tracked }
            },
            RecordQuickActionsButton.MULTISELECT,
            RecordQuickActionsButton.CHANGE_ACTIVITY,
            RecordQuickActionsButton.CHANGE_TAG.takeIf {
                multiSelectedIds.none { it is MultiSelectedRecordId.Untracked } && hasTags
            },
        )
    }

    private fun getAllowedButtons(
        extra: RecordQuickActionsParams,
        canContinue: Boolean,
        hasTags: Boolean,
    ): List<RecordQuickActionsButton> {
        return when (extra.type) {
            is Type.RecordTracked -> listOfNotNull(
                RecordQuickActionsButton.STATISTICS,
                RecordQuickActionsButton.DELETE,
                RecordQuickActionsButton.CONTINUE.takeIf { canContinue },
                RecordQuickActionsButton.REPEAT,
                RecordQuickActionsButton.DUPLICATE,
                RecordQuickActionsButton.MOVE,
                RecordQuickActionsButton.MULTISELECT,
                RecordQuickActionsButton.CHANGE_ACTIVITY,
                RecordQuickActionsButton.CHANGE_TAG.takeIf { hasTags },
            )
            is Type.RecordUntracked -> listOfNotNull(
                RecordQuickActionsButton.STATISTICS,
                RecordQuickActionsButton.MERGE,
                RecordQuickActionsButton.MULTISELECT,
                RecordQuickActionsButton.CHANGE_ACTIVITY,
            )
            is Type.RecordRunning -> listOfNotNull(
                RecordQuickActionsButton.STATISTICS,
                RecordQuickActionsButton.DELETE,
                RecordQuickActionsButton.STOP,
                RecordQuickActionsButton.MULTISELECT,
                RecordQuickActionsButton.CHANGE_ACTIVITY,
                RecordQuickActionsButton.CHANGE_TAG.takeIf { hasTags },
            )
        }
    }

    private fun applyWidth(
        buttons: List<ViewHolderType>,
    ): List<ViewHolderType> {
        val bigButtonsCount = buttons
            .count { it is RecordQuickActionsButtonBigViewData }
        val bigButtonLastIndex = buttons
            .indexOfLast { it is RecordQuickActionsButtonBigViewData }
        val smallButtonsCount = buttons
            .count { it is RecordQuickActionsButtonViewData }
        val smallButtonLastIndex = buttons
            .indexOfLast { it is RecordQuickActionsButtonViewData }

        return buttons.mapIndexed { index, button ->
            val isBigButtonFullWidth = bigButtonsCount % 2 != 0 && index == bigButtonLastIndex
            val isSmallButtonFullWidth = smallButtonsCount % 2 != 0 && index == smallButtonLastIndex
            when {
                button is RecordQuickActionsButtonBigViewData && isBigButtonFullWidth -> {
                    button.copy(width = RecordQuickActionsWidthHolder.Width.Full)
                }
                button is RecordQuickActionsButtonViewData && isSmallButtonFullWidth -> {
                    button.copy(width = RecordQuickActionsWidthHolder.Width.Full)
                }
                else -> button
            }
        }
    }

    private fun mapAction(
        action: RecordQuickActionsButton,
    ): RecordQuickAction? {
        return when (action) {
            RecordQuickActionsButton.STATISTICS -> null
            RecordQuickActionsButton.DELETE -> null
            RecordQuickActionsButton.CONTINUE -> RecordQuickAction.CONTINUE
            RecordQuickActionsButton.REPEAT -> RecordQuickAction.REPEAT
            RecordQuickActionsButton.DUPLICATE -> RecordQuickAction.DUPLICATE
            RecordQuickActionsButton.MOVE -> RecordQuickAction.MOVE
            RecordQuickActionsButton.MERGE -> RecordQuickAction.MERGE
            RecordQuickActionsButton.STOP -> RecordQuickAction.STOP
            RecordQuickActionsButton.MULTISELECT -> RecordQuickAction.MULTISELECT
            RecordQuickActionsButton.CHANGE_ACTIVITY -> RecordQuickAction.CHANGE_ACTIVITY
            RecordQuickActionsButton.CHANGE_TAG -> RecordQuickAction.CHANGE_TAG
        }
    }

    private fun mapHelpData(
        allowedButtons: List<RecordQuickActionsButton>,
        isDarkTheme: Boolean,
    ): CharSequence {
        val builder = SpannableStringBuilder()

        val iconColor = resourceRepo.getThemedAttr(R.attr.appTextHintColor, isDarkTheme)
        val imageTag = resourceRepo.getString(R.string.image_tag)
        var needDividers = false

        allowedButtons.mapNotNull { button ->
            val action = mapAction(button) ?: return@mapNotNull null
            val hint = recordQuickActionMapper.mapHint(action) ?: return@mapNotNull null
            val name = recordQuickActionMapper.mapText(action)
            val icon = recordQuickActionMapper.mapIcon(action)
                .let(resourceRepo::getDrawable)
                ?.mutate()
                ?.apply { setTint(iconColor) }

            if (needDividers) builder.appendLine().appendLine()
            if (icon != null) {
                builder.image(
                    drawable = icon,
                    sizeDp = 16,
                    isCentered = true,
                    builderAction = { append(imageTag) },
                )
                builder.append(" ")
            }
            builder.bold { append(name) }
            builder.appendLine()
            builder.append(hint)
            needDividers = true
        }

        return builder
    }

    private fun mapHintData(
        extra: RecordQuickActionsParams,
        record: RecordBase?,
        useMilitaryTime: Boolean,
        showSeconds: Boolean,
        durationFormat: DurationFormat,
    ): RecordQuickActionsState.Hint? {
        return if (recordsContainerMultiselectInteractor.isEnabled) {
            mapMultiSelectHint()
        } else {
            mapSelectedRecordHint(
                extra = extra,
                record = record,
                useMilitaryTime = useMilitaryTime,
                showSeconds = showSeconds,
                durationFormat = durationFormat,
            )
        }
    }

    private fun mapSelectedRecordHint(
        extra: RecordQuickActionsParams,
        record: RecordBase?,
        useMilitaryTime: Boolean,
        showSeconds: Boolean,
        durationFormat: DurationFormat,
    ): RecordQuickActionsState.Hint.Record? {
        fun formatRecordDuration(timeStarted: Long, timeEnded: Long): String {
            return timeMapper.formatInterval(
                interval = recordViewDataMapper.mapDuration(
                    timeStarted = timeStarted,
                    timeEnded = timeEnded,
                    showSeconds = showSeconds,
                ),
                forceSeconds = showSeconds,
                durationFormat = durationFormat,
            )
        }

        fun formatRunningDuration(timeStarted: Long): String {
            return timeMapper.formatInterval(
                interval = System.currentTimeMillis() - timeStarted,
                forceSeconds = true,
                durationFormat = durationFormat,
            )
        }

        val timeStarted: Long
        val timeEnded: Long?
        val duration: String

        when (val type = extra.type) {
            is Type.RecordTracked -> {
                timeStarted = record?.timeStarted ?: return null
                timeEnded = record.timeEnded
                duration = formatRecordDuration(timeStarted, timeEnded)
            }
            is Type.RecordUntracked -> {
                timeStarted = type.timeStarted
                timeEnded = type.timeEnded
                duration = formatRecordDuration(timeStarted, timeEnded)
            }
            is Type.RecordRunning -> {
                timeStarted = record?.timeStarted ?: return null
                timeEnded = null
                duration = formatRunningDuration(timeStarted)
            }
        }

        return RecordQuickActionsState.Hint.Record(
            name = extra.preview.name,
            iconId = extra.preview.iconId.toViewData(),
            color = extra.preview.color,
            timeStarted = timeMapper.formatTime(
                time = timeStarted,
                useMilitaryTime = useMilitaryTime,
                showSeconds = showSeconds,
            ),
            timeEnded = timeEnded?.let {
                timeMapper.formatTime(
                    time = it,
                    useMilitaryTime = useMilitaryTime,
                    showSeconds = showSeconds,
                )
            },
            duration = duration,
        )
    }

    private fun mapMultiSelectHint(): RecordQuickActionsState.Hint.MultiSelect {
        // Ex. "Selected: 5 Records"
        val recordsSelectedCount = recordsContainerMultiselectInteractor.selectedRecordIds.size
        val recordsSelectedString = resourceRepo.getString(
            R.string.separator_template,
            recordsSelectedCount,
            resourceRepo.getQuantityString(
                R.plurals.statistics_detail_times_tracked,
                recordsSelectedCount,
            ).lowercase(),
        )
        val text = resourceRepo.getString(
            R.string.separator_template,
            resourceRepo.getString(R.string.something_selected),
            recordsSelectedString,
        )
        return RecordQuickActionsState.Hint.MultiSelect(text)
    }
}