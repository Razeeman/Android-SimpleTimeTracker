package com.example.util.simpletimetracker.feature_notification.recevier

import android.app.AlarmManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.util.simpletimetracker.core.extension.goAsync
import com.example.util.simpletimetracker.core.utils.ACTION_EXTERNAL_ADD_RECORD
import com.example.util.simpletimetracker.core.utils.ACTION_EXTERNAL_AUTOMATIC_BACKUP
import com.example.util.simpletimetracker.core.utils.ACTION_EXTERNAL_AUTOMATIC_EXPORT
import com.example.util.simpletimetracker.core.utils.ACTION_EXTERNAL_CHANGE_RECORD
import com.example.util.simpletimetracker.core.utils.ACTION_EXTERNAL_CREATE_RECORD_TAG
import com.example.util.simpletimetracker.core.utils.ACTION_EXTERNAL_RESTART_ACTIVITY
import com.example.util.simpletimetracker.core.utils.ACTION_EXTERNAL_START_ACTIVITY
import com.example.util.simpletimetracker.core.utils.ACTION_EXTERNAL_STOP_ACTIVITY
import com.example.util.simpletimetracker.core.utils.ACTION_EXTERNAL_STOP_ALL_ACTIVITIES
import com.example.util.simpletimetracker.core.utils.ACTION_EXTERNAL_STOP_LONGEST_ACTIVITY
import com.example.util.simpletimetracker.core.utils.ACTION_EXTERNAL_STOP_SHORTEST_ACTIVITY
import com.example.util.simpletimetracker.core.utils.EXTRA_ACTIVITY_NAME
import com.example.util.simpletimetracker.core.utils.EXTRA_FIND_RECORD_MODE
import com.example.util.simpletimetracker.core.utils.EXTRA_FIND_RECORD_WITH_ACTIVITY_NAME
import com.example.util.simpletimetracker.core.utils.EXTRA_RECORD_COMMENT
import com.example.util.simpletimetracker.core.utils.EXTRA_RECORD_COMMENT_MODE
import com.example.util.simpletimetracker.core.utils.EXTRA_RECORD_TAG_NAME
import com.example.util.simpletimetracker.core.utils.EXTRA_RECORD_TIME_ENDED
import com.example.util.simpletimetracker.core.utils.EXTRA_RECORD_TIME_STARTED
import com.example.util.simpletimetracker.core.utils.EXTRA_RECORD_TYPE_ICON
import com.example.util.simpletimetracker.domain.record.model.RecordBase
import com.example.util.simpletimetracker.domain.recordType.model.RecordTypeGoal
import com.example.util.simpletimetracker.feature_notification.activity.controller.NotificationActivityBroadcastController
import com.example.util.simpletimetracker.feature_notification.activitySwitch.manager.NotificationActivitySwitchManager.Companion.ACTION_NOTIFICATION_SWITCH_CANCEL
import com.example.util.simpletimetracker.feature_notification.activitySwitch.manager.NotificationControlsManager.Companion.ACTION_NOTIFICATION_CONTROLS_APPLY_TAGS
import com.example.util.simpletimetracker.feature_notification.activitySwitch.manager.NotificationControlsManager.Companion.ACTION_NOTIFICATION_CONTROLS_CLEAR_TAGS
import com.example.util.simpletimetracker.feature_notification.activitySwitch.manager.NotificationControlsManager.Companion.ACTION_NOTIFICATION_CONTROLS_REPEAT
import com.example.util.simpletimetracker.feature_notification.activitySwitch.manager.NotificationControlsManager.Companion.ACTION_NOTIFICATION_CONTROLS_STOP
import com.example.util.simpletimetracker.feature_notification.activitySwitch.manager.NotificationControlsManager.Companion.ACTION_NOTIFICATION_CONTROLS_TAGS_NEXT
import com.example.util.simpletimetracker.feature_notification.activitySwitch.manager.NotificationControlsManager.Companion.ACTION_NOTIFICATION_CONTROLS_TAGS_PREV
import com.example.util.simpletimetracker.feature_notification.activitySwitch.manager.NotificationControlsManager.Companion.ACTION_NOTIFICATION_CONTROLS_TAG_CLICK
import com.example.util.simpletimetracker.feature_notification.activitySwitch.manager.NotificationControlsManager.Companion.ACTION_NOTIFICATION_CONTROLS_TAG_VALUE_BACK
import com.example.util.simpletimetracker.feature_notification.activitySwitch.manager.NotificationControlsManager.Companion.ACTION_NOTIFICATION_CONTROLS_TAG_VALUE_REMOVE
import com.example.util.simpletimetracker.feature_notification.activitySwitch.manager.NotificationControlsManager.Companion.ACTION_NOTIFICATION_CONTROLS_TAG_VALUE_SAVE
import com.example.util.simpletimetracker.feature_notification.activitySwitch.manager.NotificationControlsManager.Companion.ACTION_NOTIFICATION_CONTROLS_TAG_VALUE_UPDATE
import com.example.util.simpletimetracker.feature_notification.activitySwitch.manager.NotificationControlsManager.Companion.ACTION_NOTIFICATION_CONTROLS_TYPES_NEXT
import com.example.util.simpletimetracker.feature_notification.activitySwitch.manager.NotificationControlsManager.Companion.ACTION_NOTIFICATION_CONTROLS_TYPES_PREV
import com.example.util.simpletimetracker.feature_notification.activitySwitch.manager.NotificationControlsManager.Companion.ACTION_NOTIFICATION_CONTROLS_TYPE_CLICK
import com.example.util.simpletimetracker.feature_notification.activitySwitch.manager.NotificationControlsManager.Companion.ARGS_CONTROLS_FROM
import com.example.util.simpletimetracker.feature_notification.automaticBackup.controller.AutomaticBackupBroadcastController
import com.example.util.simpletimetracker.feature_notification.automaticExport.controller.AutomaticExportBroadcastController
import com.example.util.simpletimetracker.feature_notification.goalTime.controller.NotificationGoalTimeBroadcastController
import com.example.util.simpletimetracker.feature_notification.inactivity.controller.NotificationInactivityBroadcastController
import com.example.util.simpletimetracker.feature_notification.pomodoro.controller.NotificationPomodoroBroadcastController
import com.example.util.simpletimetracker.feature_notification.recordType.controller.NotificationTypeBroadcastController
import com.example.util.simpletimetracker.feature_notification.activitySwitch.manager.NotificationControlsManager.Companion.ARGS_EDITING_TAG_ID
import com.example.util.simpletimetracker.feature_notification.activitySwitch.manager.NotificationControlsManager.Companion.ARGS_EDITING_TAG_VALUE_INPUT
import com.example.util.simpletimetracker.feature_notification.activitySwitch.manager.NotificationControlsManager.Companion.ARGS_MULTIPLE_TAG_AVAILABLE
import com.example.util.simpletimetracker.feature_notification.activitySwitch.manager.NotificationControlsManager.Companion.ARGS_REQUIRED_VALUE_SELECTION_TAGS
import com.example.util.simpletimetracker.feature_notification.activitySwitch.manager.NotificationControlsManager.Companion.ARGS_SELECTED_TAGS
import com.example.util.simpletimetracker.feature_notification.activitySwitch.manager.NotificationControlsManager.Companion.ARGS_SELECTED_TYPE_ID
import com.example.util.simpletimetracker.feature_notification.activitySwitch.manager.NotificationControlsManager.Companion.ARGS_TAGS_SHIFT
import com.example.util.simpletimetracker.feature_notification.activitySwitch.manager.NotificationControlsManager.Companion.ARGS_CLICKED_TAG_ID
import com.example.util.simpletimetracker.feature_notification.activitySwitch.manager.NotificationControlsManager.Companion.ARGS_TYPES_SHIFT
import com.example.util.simpletimetracker.feature_notification.activitySwitch.manager.NotificationControlsManager.Companion.ARGS_TYPE_ID
import com.example.util.simpletimetracker.feature_notification.external.NotificationExternalBroadcastController
import com.example.util.simpletimetracker.feature_notification.recordType.manager.NotificationTypeManager.Companion.ACTION_NOTIFICATION_TYPE_CANCEL
import com.example.util.simpletimetracker.feature_notification.recordType.manager.NotificationTypeManager.Companion.ACTION_NOTIFICATION_TYPE_STOP
import com.example.util.simpletimetracker.feature_notification.scheduledReminder.controller.ScheduledReminderController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import javax.inject.Inject

@AndroidEntryPoint
class NotificationReceiver : BroadcastReceiver() {

    @Inject
    lateinit var typeController: NotificationTypeBroadcastController

    @Inject
    lateinit var inactivityController: NotificationInactivityBroadcastController

    @Inject
    lateinit var activityController: NotificationActivityBroadcastController

    @Inject
    lateinit var goalTimeController: NotificationGoalTimeBroadcastController

    @Inject
    lateinit var automaticBackupController: AutomaticBackupBroadcastController

    @Inject
    lateinit var automaticExportController: AutomaticExportBroadcastController

    @Inject
    lateinit var pomodoroController: NotificationPomodoroBroadcastController

    @Inject
    lateinit var externalController: NotificationExternalBroadcastController

    @Inject
    lateinit var scheduledReminderController: ScheduledReminderController

    override fun onReceive(context: Context?, intent: Intent?) {
        val action = intent?.action
        if (context == null || intent == null || action == null) return

        goAsync { handleIntent(intent, action) }
    }

    private suspend fun handleIntent(intent: Intent, action: String) {
        when (action) {
            ACTION_SCHEDULED_REMINDER -> {
                val reminderId = intent.getLongExtra(EXTRA_SCHEDULED_REMINDER_ID, 0L)
                val expectedTimestamp = intent.getLongExtra(EXTRA_SCHEDULED_REMINDER_EXPECTED_TIMESTAMP, 0)
                scheduledReminderController.onReminderFired(
                    reminderId = reminderId,
                    expectedOccurrenceTimestamp = expectedTimestamp,
                )
            }
            ACTION_INACTIVITY_REMINDER -> {
                inactivityController.onInactivityReminder()
            }
            ACTION_ACTIVITY_REMINDER -> {
                activityController.onActivityReminder()
            }
            ACTION_POMODORO_REMINDER -> {
                val cycleType = intent.getLongExtra(EXTRA_POMODORO_CYCLE_TYPE, 0)
                pomodoroController.onReminder(cycleType)
            }
            ACTION_GOAL_TIME_REMINDER_SESSION,
            ACTION_GOAL_TIME_REMINDER_CATEGORY_SESSION,
            ACTION_GOAL_TIME_REMINDER_TAG_SESSION,
            ACTION_GOAL_TIME_REMINDER_DAILY,
            ACTION_GOAL_TIME_REMINDER_CATEGORY_DAILY,
            ACTION_GOAL_TIME_REMINDER_TAG_DAILY,
            ACTION_GOAL_TIME_REMINDER_WEEKLY,
            ACTION_GOAL_TIME_REMINDER_CATEGORY_WEEKLY,
            ACTION_GOAL_TIME_REMINDER_TAG_WEEKLY,
            ACTION_GOAL_TIME_REMINDER_MONTHLY,
            ACTION_GOAL_TIME_REMINDER_CATEGORY_MONTHLY,
            ACTION_GOAL_TIME_REMINDER_TAG_MONTHLY,
            -> {
                val typeId = intent.getLongExtra(EXTRA_GOAL_TIME_TYPE_ID, 0)
                val categoryId = intent.getLongExtra(EXTRA_GOAL_TIME_CATEGORY_ID, 0)
                val tagId = intent.getLongExtra(EXTRA_GOAL_TIME_TAG_ID, 0)
                val idData = when {
                    typeId != 0L -> RecordTypeGoal.IdData.Type(typeId)
                    categoryId != 0L -> RecordTypeGoal.IdData.Category(categoryId)
                    else -> RecordTypeGoal.IdData.Tag(tagId)
                }
                val goalTimeType = when (action) {
                    ACTION_GOAL_TIME_REMINDER_SESSION,
                    ACTION_GOAL_TIME_REMINDER_CATEGORY_SESSION,
                    ACTION_GOAL_TIME_REMINDER_TAG_SESSION,
                    -> RecordTypeGoal.Range.Session
                    ACTION_GOAL_TIME_REMINDER_DAILY,
                    ACTION_GOAL_TIME_REMINDER_CATEGORY_DAILY,
                    ACTION_GOAL_TIME_REMINDER_TAG_DAILY,
                    -> RecordTypeGoal.Range.Daily
                    ACTION_GOAL_TIME_REMINDER_WEEKLY,
                    ACTION_GOAL_TIME_REMINDER_CATEGORY_WEEKLY,
                    ACTION_GOAL_TIME_REMINDER_TAG_WEEKLY,
                    -> RecordTypeGoal.Range.Weekly
                    ACTION_GOAL_TIME_REMINDER_MONTHLY,
                    ACTION_GOAL_TIME_REMINDER_CATEGORY_MONTHLY,
                    ACTION_GOAL_TIME_REMINDER_TAG_MONTHLY,
                    -> RecordTypeGoal.Range.Monthly
                    else -> RecordTypeGoal.Range.Session
                }
                goalTimeController.onGoalTimeReminder(idData, goalTimeType)
            }
            ACTION_GOAL_TIME_REMINDER_DAY_END,
            ACTION_GOAL_TIME_REMINDER_WEEK_END,
            ACTION_GOAL_TIME_REMINDER_MONTH_END,
            -> {
                goalTimeController.onRangeEndReminder()
            }
            ACTION_AUTOMATIC_BACKUP,
            ACTION_EXTERNAL_AUTOMATIC_BACKUP,
            -> {
                try {
                    automaticBackupController.onReminder()
                } finally {
                    automaticBackupController.onFinished()
                }
            }
            ACTION_AUTOMATIC_EXPORT,
            ACTION_EXTERNAL_AUTOMATIC_EXPORT,
            -> {
                try {
                    automaticExportController.onReminder()
                } finally {
                    automaticExportController.onFinished()
                }
            }
            ACTION_EXTERNAL_START_ACTIVITY -> {
                val name = intent.getStringExtra(EXTRA_ACTIVITY_NAME)
                val comment = intent.getStringExtra(EXTRA_RECORD_COMMENT)
                val tagNames = intent.getStringExtra(EXTRA_RECORD_TAG_NAME)
                    ?.splitTagNames().orEmpty()
                val timeStarted = intent.getStringExtra(EXTRA_RECORD_TIME_STARTED)
                externalController.onActionExternalActivityStart(
                    name = name,
                    comment = comment,
                    tagNames = tagNames,
                    timeStarted = timeStarted,
                )
            }
            ACTION_EXTERNAL_STOP_ACTIVITY -> {
                val name = intent.getStringExtra(EXTRA_ACTIVITY_NAME)
                val timeEnded = intent.getStringExtra(EXTRA_RECORD_TIME_ENDED)
                externalController.onActionExternalActivityStop(
                    name = name,
                    timeEnded = timeEnded,
                )
            }
            ACTION_EXTERNAL_STOP_ALL_ACTIVITIES -> {
                externalController.onActionExternalActivityStopAll()
            }
            ACTION_EXTERNAL_STOP_SHORTEST_ACTIVITY -> {
                externalController.onActionExternalActivityStopShortest()
            }
            ACTION_EXTERNAL_STOP_LONGEST_ACTIVITY -> {
                externalController.onActionExternalActivityStopLongest()
            }
            ACTION_EXTERNAL_RESTART_ACTIVITY -> {
                val comment = intent.getStringExtra(EXTRA_RECORD_COMMENT)
                val tagNames = intent.getStringExtra(EXTRA_RECORD_TAG_NAME)
                    ?.splitTagNames().orEmpty()
                externalController.onActionExternalActivityRestart(
                    comment = comment,
                    tagNames = tagNames,
                )
            }
            ACTION_EXTERNAL_ADD_RECORD -> {
                val name = intent.getStringExtra(EXTRA_ACTIVITY_NAME)
                val timeStarted = intent.getStringExtra(EXTRA_RECORD_TIME_STARTED)
                val timeEnded = intent.getStringExtra(EXTRA_RECORD_TIME_ENDED)
                val comment = intent.getStringExtra(EXTRA_RECORD_COMMENT)
                val tagNames = intent.getStringExtra(EXTRA_RECORD_TAG_NAME)
                    ?.splitTagNames().orEmpty()
                externalController.onActionExternalRecordAdd(
                    name = name,
                    timeStarted = timeStarted,
                    timeEnded = timeEnded,
                    comment = comment,
                    tagNames = tagNames,
                )
            }
            ACTION_EXTERNAL_CHANGE_RECORD -> {
                val findMode = intent.getStringExtra(EXTRA_FIND_RECORD_MODE)
                val name = intent.getStringExtra(EXTRA_FIND_RECORD_WITH_ACTIVITY_NAME)
                val comment = intent.getStringExtra(EXTRA_RECORD_COMMENT)
                val commentMode = intent.getStringExtra(EXTRA_RECORD_COMMENT_MODE)
                externalController.onActionExternalRecordChange(
                    findMode = findMode,
                    name = name,
                    comment = comment,
                    commentMode = commentMode,
                )
            }
            ACTION_EXTERNAL_CREATE_RECORD_TAG -> {
                val name = intent.getStringExtra(EXTRA_RECORD_TAG_NAME)
                val icon = intent.getStringExtra(EXTRA_RECORD_TYPE_ICON)
                externalController.onActionExternalRecordTagAdd(
                    name = name,
                    icon = icon,
                )
            }
            ACTION_NOTIFICATION_TYPE_STOP -> {
                val typeId = intent.getLongExtra(ARGS_TYPE_ID, 0)
                typeController.onActionActivityStop(typeId)
            }
            ACTION_NOTIFICATION_CONTROLS_STOP -> {
                val typeId = intent.getLongExtra(ARGS_TYPE_ID, 0)
                typeController.onActionActivityStop(typeId)
            }
            ACTION_NOTIFICATION_CONTROLS_TYPE_CLICK -> {
                val from = intent.getIntExtra(ARGS_CONTROLS_FROM, 0)
                val typeId = intent.getLongExtra(ARGS_TYPE_ID, 0)
                val selectedTypeId = intent.getLongExtra(ARGS_SELECTED_TYPE_ID, 0)
                val typesShift = intent.getIntExtra(ARGS_TYPES_SHIFT, 0)
                typeController.onActionTypeClick(
                    from = from,
                    typeId = typeId,
                    selectedTypeId = selectedTypeId,
                    typesShift = typesShift,
                )
            }
            ACTION_NOTIFICATION_CONTROLS_REPEAT -> {
                typeController.onActionRepeat()
            }
            ACTION_NOTIFICATION_CONTROLS_APPLY_TAGS -> {
                val from = intent.getIntExtra(ARGS_CONTROLS_FROM, 0)
                val typeId = intent.getLongExtra(ARGS_TYPE_ID, 0)
                val selectedTypeId = intent.getLongExtra(ARGS_SELECTED_TYPE_ID, 0)
                val selectedTags = intent.getSelectedTags()
                val typesShift = intent.getIntExtra(ARGS_TYPES_SHIFT, 0)
                typeController.onActionApplyTags(
                    from = from,
                    typeId = typeId,
                    selectedTypeId = selectedTypeId,
                    selectedTags = selectedTags,
                    typesShift = typesShift,
                )
            }
            ACTION_NOTIFICATION_CONTROLS_CLEAR_TAGS -> {
                val from = intent.getIntExtra(ARGS_CONTROLS_FROM, 0)
                val typeId = intent.getLongExtra(ARGS_TYPE_ID, 0)
                val selectedTypeId = intent.getLongExtra(ARGS_SELECTED_TYPE_ID, 0)
                val typesShift = intent.getIntExtra(ARGS_TYPES_SHIFT, 0)
                val tagsShift = intent.getIntExtra(ARGS_TAGS_SHIFT, 0)
                val isMultipleTagAvailable = intent.getBooleanExtra(ARGS_MULTIPLE_TAG_AVAILABLE, false)
                val requiredValueSelectionTagIds = intent.getRequiredValueSelectionTagIds()
                typeController.onActionClearTags(
                    from = from,
                    typeId = typeId,
                    selectedTypeId = selectedTypeId,
                    typesShift = typesShift,
                    tagsShift = tagsShift,
                    isMultipleTagAvailable = isMultipleTagAvailable,
                    requiredValueSelectionTagIds = requiredValueSelectionTagIds,
                )
            }
            ACTION_NOTIFICATION_CONTROLS_TYPES_PREV,
            ACTION_NOTIFICATION_CONTROLS_TYPES_NEXT,
            ACTION_NOTIFICATION_CONTROLS_TAGS_PREV,
            ACTION_NOTIFICATION_CONTROLS_TAGS_NEXT,
            ACTION_NOTIFICATION_CONTROLS_TAG_VALUE_BACK,
            ACTION_NOTIFICATION_CONTROLS_TAG_VALUE_UPDATE,
            ACTION_NOTIFICATION_CONTROLS_TAG_VALUE_REMOVE,
            -> {
                val from = intent.getIntExtra(ARGS_CONTROLS_FROM, 0)
                val typeId = intent.getLongExtra(ARGS_TYPE_ID, 0)
                val selectedTypeId = intent.getLongExtra(ARGS_SELECTED_TYPE_ID, 0)
                val typesShift = intent.getIntExtra(ARGS_TYPES_SHIFT, 0)
                val tagsShift = intent.getIntExtra(ARGS_TAGS_SHIFT, 0)
                val selectedTags = intent.getSelectedTags()
                val editingTagId = intent.getEditingTagId()
                val editingTagValueInput = intent.getEditingTagValueInput()
                val isMultipleTagAvailable = intent.getBooleanExtra(ARGS_MULTIPLE_TAG_AVAILABLE, false)
                val requiredValueSelectionTagIds = intent.getRequiredValueSelectionTagIds()
                typeController.onRequestUpdate(
                    from = from,
                    typeId = typeId,
                    selectedTypeId = selectedTypeId,
                    selectedTags = selectedTags,
                    editingTagId = editingTagId,
                    editingTagValueInput = editingTagValueInput,
                    typesShift = typesShift,
                    tagsShift = tagsShift,
                    isMultipleTagAvailable = isMultipleTagAvailable,
                    requiredValueSelectionTagIds = requiredValueSelectionTagIds,
                )
            }
            ACTION_NOTIFICATION_CONTROLS_TAG_CLICK -> {
                val from = intent.getIntExtra(ARGS_CONTROLS_FROM, 0)
                val typeId = intent.getLongExtra(ARGS_TYPE_ID, 0)
                val selectedTypeId = intent.getLongExtra(ARGS_SELECTED_TYPE_ID, 0)
                val typesShift = intent.getIntExtra(ARGS_TYPES_SHIFT, 0)
                val tagsShift = intent.getIntExtra(ARGS_TAGS_SHIFT, 0)
                val tagId = intent.getLongExtra(ARGS_CLICKED_TAG_ID, 0)
                val selectedTags = intent.getSelectedTags()
                val isMultipleTagAvailable = intent.getBooleanExtra(ARGS_MULTIPLE_TAG_AVAILABLE, false)
                val requiredValueSelectionTagIds = intent.getRequiredValueSelectionTagIds()
                typeController.onActionTagClick(
                    from = from,
                    typeId = typeId,
                    selectedTypeId = selectedTypeId,
                    tagId = tagId,
                    typesShift = typesShift,
                    tagsShift = tagsShift,
                    selectedTags = selectedTags,
                    isMultipleTagAvailable = isMultipleTagAvailable,
                    requiredValueSelectionTagIds = requiredValueSelectionTagIds,
                )
            }
            ACTION_NOTIFICATION_CONTROLS_TAG_VALUE_SAVE -> {
                val from = intent.getIntExtra(ARGS_CONTROLS_FROM, 0)
                val typeId = intent.getLongExtra(ARGS_TYPE_ID, 0)
                val selectedTypeId = intent.getLongExtra(ARGS_SELECTED_TYPE_ID, 0)
                val typesShift = intent.getIntExtra(ARGS_TYPES_SHIFT, 0)
                val tagsShift = intent.getIntExtra(ARGS_TAGS_SHIFT, 0)
                val selectedTags = intent.getSelectedTags()
                val editingTagId = intent.getEditingTagId() ?: return
                val editingTagValueInput = intent.getEditingTagValueInput()
                val isMultipleTagAvailable = intent.getBooleanExtra(ARGS_MULTIPLE_TAG_AVAILABLE, false)
                val requiredValueSelectionTagIds = intent.getRequiredValueSelectionTagIds()
                typeController.onActionTagValueSave(
                    from = from,
                    typeId = typeId,
                    selectedTypeId = selectedTypeId,
                    tagId = editingTagId,
                    tagValue = editingTagValueInput,
                    typesShift = typesShift,
                    tagsShift = tagsShift,
                    selectedTags = selectedTags,
                    isMultipleTagAvailable = isMultipleTagAvailable,
                    requiredValueSelectionTagIds = requiredValueSelectionTagIds,
                )
            }
            ACTION_NOTIFICATION_TYPE_CANCEL -> {
                val typeId = intent.getLongExtra(ARGS_TYPE_ID, 0)
                typeController.onTypeCancel(typeId)
            }
            ACTION_NOTIFICATION_SWITCH_CANCEL -> {
                typeController.onActivitySwitchCancel()
            }
            Intent.ACTION_BOOT_COMPLETED,
            ACTION_QUICK_BOOT_POWER_ON,
            ACTION_HTC_QUICK_BOOT_POWER_ON,
            -> supervisorScope {
                // TODO remove controllers?
                launch { inactivityController.onBootCompleted() }
                launch { activityController.onBootCompleted() }
                launch { goalTimeController.onBootCompleted() }
                launch { typeController.onBootCompleted() }
                launch { automaticBackupController.onBootCompleted() }
                launch { automaticExportController.onBootCompleted() }
                launch { pomodoroController.onBootCompleted() }
                launch { scheduledReminderController.onBootCompleted() }
            }
            AlarmManager.ACTION_SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED -> supervisorScope {
                launch { goalTimeController.onExactAlarmPermissionStateChanged() }
                launch { pomodoroController.onExactAlarmPermissionStateChanged() }
                launch { scheduledReminderController.onExactAlarmPermissionStateChanged() }
            }
            Intent.ACTION_MY_PACKAGE_REPLACED,
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_DATE_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED,
            -> {
                // TODO update tabs to recalculate record views and stats?
                scheduledReminderController.rescheduleAll()
            }
        }
    }

    private fun Intent.getSelectedTags(): List<RecordBase.Tag> {
        val raw = getStringExtra(ARGS_SELECTED_TAGS)
        if (raw.isNullOrEmpty()) return emptyList()
        return raw.split(';').mapNotNull { segment ->
            val parts = segment.split('=', limit = 2)
            val tagId = parts.getOrNull(0)
                ?.takeIf(String::isNotBlank)
                ?.toLongOrNull() ?: return@mapNotNull null
            val numericValue = parts.getOrNull(1)
                ?.takeIf(String::isNotBlank)
                ?.toDoubleOrNull()
            RecordBase.Tag(
                tagId = tagId,
                numericValue = numericValue,
            )
        }
    }

    private fun Intent.getEditingTagId(): Long? {
        if (!hasExtra(ARGS_EDITING_TAG_ID)) return null
        return getLongExtra(ARGS_EDITING_TAG_ID, 0L).takeIf { it != 0L }
    }

    private fun Intent.getEditingTagValueInput(): String? {
        if (!hasExtra(ARGS_EDITING_TAG_VALUE_INPUT)) return null
        return getStringExtra(ARGS_EDITING_TAG_VALUE_INPUT)
    }

    private fun Intent.getRequiredValueSelectionTagIds(): List<Long> {
        return getLongArrayExtra(ARGS_REQUIRED_VALUE_SELECTION_TAGS)?.toList().orEmpty()
    }

    private fun String.splitTagNames(): List<String> {
        return split(',').map(String::trim)
    }

    companion object {
        const val ACTION_INACTIVITY_REMINDER =
            "com.razeeman.util.simpletimetracker.ACTION_INACTIVITY_REMINDER"
        const val ACTION_ACTIVITY_REMINDER =
            "com.razeeman.util.simpletimetracker.ACTION_ACTIVITY_REMINDER"
        const val ACTION_GOAL_TIME_REMINDER_SESSION =
            "com.razeeman.util.simpletimetracker.ACTION_GOAL_TIME_REMINDER"
        const val ACTION_GOAL_TIME_REMINDER_CATEGORY_SESSION =
            "com.razeeman.util.simpletimetracker.ACTION_GOAL_TIME_REMINDER_CATEGORY"
        const val ACTION_GOAL_TIME_REMINDER_TAG_SESSION =
            "com.razeeman.util.simpletimetracker.ACTION_GOAL_TIME_REMINDER_TAG"
        const val ACTION_GOAL_TIME_REMINDER_DAILY =
            "com.razeeman.util.simpletimetracker.ACTION_GOAL_TIME_REMINDER_DAILY"
        const val ACTION_GOAL_TIME_REMINDER_CATEGORY_DAILY =
            "com.razeeman.util.simpletimetracker.ACTION_GOAL_TIME_REMINDER_CATEGORY_DAILY"
        const val ACTION_GOAL_TIME_REMINDER_TAG_DAILY =
            "com.razeeman.util.simpletimetracker.ACTION_GOAL_TIME_REMINDER_TAG_DAILY"
        const val ACTION_GOAL_TIME_REMINDER_WEEKLY =
            "com.razeeman.util.simpletimetracker.ACTION_GOAL_TIME_REMINDER_WEEKLY"
        const val ACTION_GOAL_TIME_REMINDER_CATEGORY_WEEKLY =
            "com.razeeman.util.simpletimetracker.ACTION_GOAL_TIME_REMINDER_CATEGORY_WEEKLY"
        const val ACTION_GOAL_TIME_REMINDER_TAG_WEEKLY =
            "com.razeeman.util.simpletimetracker.ACTION_GOAL_TIME_REMINDER_TAG_WEEKLY"
        const val ACTION_GOAL_TIME_REMINDER_MONTHLY =
            "com.razeeman.util.simpletimetracker.ACTION_GOAL_TIME_REMINDER_MONTHLY"
        const val ACTION_GOAL_TIME_REMINDER_CATEGORY_MONTHLY =
            "com.razeeman.util.simpletimetracker.ACTION_GOAL_TIME_REMINDER_CATEGORY_MONTHLY"
        const val ACTION_GOAL_TIME_REMINDER_TAG_MONTHLY =
            "com.razeeman.util.simpletimetracker.ACTION_GOAL_TIME_REMINDER_TAG_MONTHLY"
        const val ACTION_GOAL_TIME_REMINDER_DAY_END =
            "com.razeeman.util.simpletimetracker.ACTION_GOAL_TIME_REMINDER_DAY_END"
        const val ACTION_GOAL_TIME_REMINDER_WEEK_END =
            "com.razeeman.util.simpletimetracker.ACTION_GOAL_TIME_REMINDER_WEEK_END"
        const val ACTION_GOAL_TIME_REMINDER_MONTH_END =
            "com.razeeman.util.simpletimetracker.ACTION_GOAL_TIME_REMINDER_MONTH_END"
        const val ACTION_POMODORO_REMINDER =
            "com.razeeman.util.simpletimetracker.ACTION_POMODORO_REMINDER"
        const val ACTION_AUTOMATIC_BACKUP =
            "com.razeeman.util.simpletimetracker.ACTION_AUTOMATIC_BACKUP"
        const val ACTION_AUTOMATIC_EXPORT =
            "com.razeeman.util.simpletimetracker.ACTION_AUTOMATIC_EXPORT"
        const val ACTION_SCHEDULED_REMINDER =
            "com.razeeman.util.simpletimetracker.ACTION_SCHEDULED_REMINDER"

        const val ACTION_QUICK_BOOT_POWER_ON = "android.intent.action.QUICKBOOT_POWERON"
        const val ACTION_HTC_QUICK_BOOT_POWER_ON = "com.htc.intent.action.QUICKBOOT_POWERON"

        const val EXTRA_GOAL_TIME_TYPE_ID =
            "extra_goal_time_type_id"
        const val EXTRA_GOAL_TIME_CATEGORY_ID =
            "extra_goal_time_category_id"
        const val EXTRA_GOAL_TIME_TAG_ID =
            "extra_goal_time_tag_id"
        const val EXTRA_POMODORO_CYCLE_TYPE =
            "extra_pomodoro_cycle_type"
        const val EXTRA_SCHEDULED_REMINDER_ID =
            "extra_scheduled_reminder_id"
        const val EXTRA_SCHEDULED_REMINDER_EXPECTED_TIMESTAMP =
            "extra_scheduled_reminder_expected_timestamp"
    }
}