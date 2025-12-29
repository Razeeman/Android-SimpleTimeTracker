package com.example.util.simpletimetracker.feature_notification.recevier

import android.app.AlarmManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.util.simpletimetracker.core.extension.goAsync
import com.example.util.simpletimetracker.core.utils.ACTION_EXTERNAL_ADD_RECORD
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
import com.example.util.simpletimetracker.domain.recordType.model.RecordTypeGoal
import com.example.util.simpletimetracker.feature_notification.activity.controller.NotificationActivityBroadcastController
import com.example.util.simpletimetracker.feature_notification.activitySwitch.manager.NotificationActivitySwitchManager.Companion.ACTION_NOTIFICATION_SWITCH_CANCEL
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
import com.example.util.simpletimetracker.feature_notification.activitySwitch.manager.NotificationControlsManager.Companion.ARGS_SELECTED_TYPE_ID
import com.example.util.simpletimetracker.feature_notification.activitySwitch.manager.NotificationControlsManager.Companion.ARGS_TAGS_SHIFT
import com.example.util.simpletimetracker.feature_notification.activitySwitch.manager.NotificationControlsManager.Companion.ARGS_TAG_ID
import com.example.util.simpletimetracker.feature_notification.activitySwitch.manager.NotificationControlsManager.Companion.ARGS_TAG_VALUE
import com.example.util.simpletimetracker.feature_notification.activitySwitch.manager.NotificationControlsManager.Companion.ARGS_TYPES_SHIFT
import com.example.util.simpletimetracker.feature_notification.activitySwitch.manager.NotificationControlsManager.Companion.ARGS_TYPE_ID
import com.example.util.simpletimetracker.feature_notification.external.NotificationExternalBroadcastController
import com.example.util.simpletimetracker.feature_notification.recordType.manager.NotificationTypeManager.Companion.ACTION_NOTIFICATION_TYPE_CANCEL
import com.example.util.simpletimetracker.feature_notification.recordType.manager.NotificationTypeManager.Companion.ACTION_NOTIFICATION_TYPE_STOP
import dagger.hilt.android.AndroidEntryPoint
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

    override fun onReceive(context: Context?, intent: Intent?) {
        val action = intent?.action
        if (context == null || intent == null || action == null) return

        when (action) {
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
            ACTION_GOAL_TIME_REMINDER_DAILY,
            ACTION_GOAL_TIME_REMINDER_CATEGORY_DAILY,
            ACTION_GOAL_TIME_REMINDER_WEEKLY,
            ACTION_GOAL_TIME_REMINDER_CATEGORY_WEEKLY,
            ACTION_GOAL_TIME_REMINDER_MONTHLY,
            ACTION_GOAL_TIME_REMINDER_CATEGORY_MONTHLY,
            -> {
                val typeId = intent.getLongExtra(EXTRA_GOAL_TIME_TYPE_ID, 0)
                val categoryId = intent.getLongExtra(EXTRA_GOAL_TIME_CATEGORY_ID, 0)
                val idData = if (typeId != 0L) {
                    RecordTypeGoal.IdData.Type(typeId)
                } else {
                    RecordTypeGoal.IdData.Category(categoryId)
                }
                val goalTimeType = when (action) {
                    ACTION_GOAL_TIME_REMINDER_SESSION,
                    ACTION_GOAL_TIME_REMINDER_CATEGORY_SESSION,
                    -> RecordTypeGoal.Range.Session
                    ACTION_GOAL_TIME_REMINDER_DAILY,
                    ACTION_GOAL_TIME_REMINDER_CATEGORY_DAILY,
                    -> RecordTypeGoal.Range.Daily
                    ACTION_GOAL_TIME_REMINDER_WEEKLY,
                    ACTION_GOAL_TIME_REMINDER_CATEGORY_WEEKLY,
                    -> RecordTypeGoal.Range.Weekly
                    ACTION_GOAL_TIME_REMINDER_MONTHLY,
                    ACTION_GOAL_TIME_REMINDER_CATEGORY_MONTHLY,
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
            ACTION_AUTOMATIC_BACKUP -> goAsync(
                finally = { automaticBackupController.onFinished() },
                block = { automaticBackupController.onReminder() },
            )
            ACTION_AUTOMATIC_EXPORT -> goAsync(
                finally = { automaticExportController.onFinished() },
                block = { automaticExportController.onReminder() },
            )
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
                val tagId = intent.getLongExtra(ARGS_TAG_ID, 0)
                val tagValue = intent.getStringExtra(ARGS_TAG_VALUE)
                val typesShift = intent.getIntExtra(ARGS_TYPES_SHIFT, 0)
                val tagsShift = intent.getIntExtra(ARGS_TAGS_SHIFT, 0)
                typeController.onRequestUpdate(
                    from = from,
                    typeId = typeId,
                    selectedTypeId = selectedTypeId,
                    selectedTagId = tagId,
                    selectedTagValue = tagValue,
                    typesShift = typesShift,
                    tagsShift = tagsShift,
                )
            }
            ACTION_NOTIFICATION_CONTROLS_TAG_CLICK -> {
                val from = intent.getIntExtra(ARGS_CONTROLS_FROM, 0)
                val typeId = intent.getLongExtra(ARGS_TYPE_ID, 0)
                val selectedTypeId = intent.getLongExtra(ARGS_SELECTED_TYPE_ID, 0)
                val tagId = intent.getLongExtra(ARGS_TAG_ID, 0)
                val typesShift = intent.getIntExtra(ARGS_TYPES_SHIFT, 0)
                typeController.onActionTagClick(
                    from = from,
                    typeId = typeId,
                    selectedTypeId = selectedTypeId,
                    tagId = tagId,
                    typesShift = typesShift,
                )
            }
            ACTION_NOTIFICATION_CONTROLS_TAG_VALUE_SAVE -> {
                val from = intent.getIntExtra(ARGS_CONTROLS_FROM, 0)
                val typeId = intent.getLongExtra(ARGS_TYPE_ID, 0)
                val selectedTypeId = intent.getLongExtra(ARGS_SELECTED_TYPE_ID, 0)
                val tagId = intent.getLongExtra(ARGS_TAG_ID, 0)
                val tagValue = intent.getStringExtra(ARGS_TAG_VALUE)
                val typesShift = intent.getIntExtra(ARGS_TYPES_SHIFT, 0)
                typeController.onActionTagValueSave(
                    from = from,
                    typeId = typeId,
                    selectedTypeId = selectedTypeId,
                    tagId = tagId,
                    tagValue = tagValue,
                    typesShift = typesShift,
                )
            }
            ACTION_NOTIFICATION_TYPE_CANCEL -> {
                val typeId = intent.getLongExtra(ARGS_TYPE_ID, 0)
                typeController.onTypeCancel(typeId)
            }
            ACTION_NOTIFICATION_SWITCH_CANCEL -> {
                typeController.onActivitySwitchCancel()
            }
            Intent.ACTION_BOOT_COMPLETED -> {
                onBootCompleted()
            }
            AlarmManager.ACTION_SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED -> {
                goalTimeController.onExactAlarmPermissionStateChanged()
                pomodoroController.onExactAlarmPermissionStateChanged()
            }
        }
    }

    private fun onBootCompleted() {
        inactivityController.onBootCompleted()
        activityController.onBootCompleted()
        goalTimeController.onBootCompleted()
        typeController.onBootCompleted()
        automaticBackupController.onBootCompleted()
        automaticExportController.onBootCompleted()
        pomodoroController.onBootCompleted()
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
        const val ACTION_GOAL_TIME_REMINDER_DAILY =
            "com.razeeman.util.simpletimetracker.ACTION_GOAL_TIME_REMINDER_DAILY"
        const val ACTION_GOAL_TIME_REMINDER_CATEGORY_DAILY =
            "com.razeeman.util.simpletimetracker.ACTION_GOAL_TIME_REMINDER_CATEGORY_DAILY"
        const val ACTION_GOAL_TIME_REMINDER_WEEKLY =
            "com.razeeman.util.simpletimetracker.ACTION_GOAL_TIME_REMINDER_WEEKLY"
        const val ACTION_GOAL_TIME_REMINDER_CATEGORY_WEEKLY =
            "com.razeeman.util.simpletimetracker.ACTION_GOAL_TIME_REMINDER_CATEGORY_WEEKLY"
        const val ACTION_GOAL_TIME_REMINDER_MONTHLY =
            "com.razeeman.util.simpletimetracker.ACTION_GOAL_TIME_REMINDER_MONTHLY"
        const val ACTION_GOAL_TIME_REMINDER_CATEGORY_MONTHLY =
            "com.razeeman.util.simpletimetracker.ACTION_GOAL_TIME_REMINDER_CATEGORY_MONTHLY"
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

        const val EXTRA_GOAL_TIME_TYPE_ID =
            "extra_goal_time_type_id"
        const val EXTRA_GOAL_TIME_CATEGORY_ID =
            "extra_goal_time_category_id"
        const val EXTRA_POMODORO_CYCLE_TYPE =
            "extra_pomodoro_cycle_type"
    }
}