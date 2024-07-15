package com.example.util.simpletimetracker.feature_notification.recevier

import android.app.AlarmManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.util.simpletimetracker.core.extension.goAsync
import com.example.util.simpletimetracker.core.utils.ACTION_RESTART_ACTIVITY
import com.example.util.simpletimetracker.core.utils.ACTION_START_ACTIVITY
import com.example.util.simpletimetracker.core.utils.ACTION_STOP_ACTIVITY
import com.example.util.simpletimetracker.core.utils.ACTION_STOP_ALL_ACTIVITIES
import com.example.util.simpletimetracker.core.utils.ACTION_STOP_LONGEST_ACTIVITY
import com.example.util.simpletimetracker.core.utils.ACTION_STOP_SHORTEST_ACTIVITY
import com.example.util.simpletimetracker.core.utils.EXTRA_ACTIVITY_NAME
import com.example.util.simpletimetracker.core.utils.EXTRA_RECORD_COMMENT
import com.example.util.simpletimetracker.core.utils.EXTRA_RECORD_TAG_NAME
import com.example.util.simpletimetracker.domain.model.RecordTypeGoal
import com.example.util.simpletimetracker.feature_notification.activity.controller.NotificationActivityBroadcastController
import com.example.util.simpletimetracker.feature_notification.automaticBackup.controller.AutomaticBackupBroadcastController
import com.example.util.simpletimetracker.feature_notification.automaticExport.controller.AutomaticExportBroadcastController
import com.example.util.simpletimetracker.feature_notification.goalTime.controller.NotificationGoalTimeBroadcastController
import com.example.util.simpletimetracker.feature_notification.inactivity.controller.NotificationInactivityBroadcastController
import com.example.util.simpletimetracker.feature_notification.pomodoro.controller.NotificationPomodoroBroadcastController
import com.example.util.simpletimetracker.feature_notification.recordType.controller.NotificationTypeBroadcastController
import com.example.util.simpletimetracker.feature_notification.recordType.manager.NotificationTypeManager.Companion.ACTION_NOTIFICATION_STOP
import com.example.util.simpletimetracker.feature_notification.recordType.manager.NotificationTypeManager.Companion.ACTION_NOTIFICATION_TAGS_NEXT
import com.example.util.simpletimetracker.feature_notification.recordType.manager.NotificationTypeManager.Companion.ACTION_NOTIFICATION_TAGS_PREV
import com.example.util.simpletimetracker.feature_notification.recordType.manager.NotificationTypeManager.Companion.ACTION_NOTIFICATION_TAG_CLICK
import com.example.util.simpletimetracker.feature_notification.recordType.manager.NotificationTypeManager.Companion.ACTION_NOTIFICATION_TYPES_NEXT
import com.example.util.simpletimetracker.feature_notification.recordType.manager.NotificationTypeManager.Companion.ACTION_NOTIFICATION_TYPES_PREV
import com.example.util.simpletimetracker.feature_notification.recordType.manager.NotificationTypeManager.Companion.ACTION_NOTIFICATION_TYPE_CLICK
import com.example.util.simpletimetracker.feature_notification.recordType.manager.NotificationTypeManager.Companion.ARGS_SELECTED_TYPE_ID
import com.example.util.simpletimetracker.feature_notification.recordType.manager.NotificationTypeManager.Companion.ARGS_TAGS_SHIFT
import com.example.util.simpletimetracker.feature_notification.recordType.manager.NotificationTypeManager.Companion.ARGS_TAG_ID
import com.example.util.simpletimetracker.feature_notification.recordType.manager.NotificationTypeManager.Companion.ARGS_TYPES_SHIFT
import com.example.util.simpletimetracker.feature_notification.recordType.manager.NotificationTypeManager.Companion.ARGS_TYPE_ID
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
            ACTION_START_ACTIVITY -> {
                val name = intent.getStringExtra(EXTRA_ACTIVITY_NAME)
                val comment = intent.getStringExtra(EXTRA_RECORD_COMMENT)
                val tagName = intent.getStringExtra(EXTRA_RECORD_TAG_NAME)
                typeController.onActionActivityStart(
                    name = name,
                    comment = comment,
                    tagName = tagName,
                )
            }
            ACTION_STOP_ACTIVITY -> {
                val name = intent.getStringExtra(EXTRA_ACTIVITY_NAME)
                typeController.onActionActivityStop(name)
            }
            ACTION_STOP_ALL_ACTIVITIES -> {
                typeController.onActionActivityStopAll()
            }
            ACTION_STOP_SHORTEST_ACTIVITY -> {
                typeController.onActionActivityStopShortest()
            }
            ACTION_STOP_LONGEST_ACTIVITY -> {
                typeController.onActionActivityStopLongest()
            }
            ACTION_RESTART_ACTIVITY -> {
                val comment = intent.getStringExtra(EXTRA_RECORD_COMMENT)
                val tagName = intent.getStringExtra(EXTRA_RECORD_TAG_NAME)
                typeController.onActionActivityRestart(
                    comment = comment,
                    tagName = tagName,
                )
            }
            ACTION_NOTIFICATION_STOP -> {
                val typeId = intent.getLongExtra(ARGS_TYPE_ID, 0)
                typeController.onActionActivityStop(typeId)
            }
            ACTION_NOTIFICATION_TYPE_CLICK -> {
                val typeId = intent.getLongExtra(ARGS_TYPE_ID, 0)
                val selectedTypeId = intent.getLongExtra(ARGS_SELECTED_TYPE_ID, 0)
                val typesShift = intent.getIntExtra(ARGS_TYPES_SHIFT, 0)
                typeController.onActionTypeClick(
                    typeId = typeId,
                    selectedTypeId = selectedTypeId,
                    typesShift = typesShift,
                )
            }
            ACTION_NOTIFICATION_TYPES_PREV,
            ACTION_NOTIFICATION_TYPES_NEXT,
            ACTION_NOTIFICATION_TAGS_PREV,
            ACTION_NOTIFICATION_TAGS_NEXT,
            -> {
                val typeId = intent.getLongExtra(ARGS_TYPE_ID, 0)
                val selectedTypeId = intent.getLongExtra(ARGS_SELECTED_TYPE_ID, 0)
                val typesShift = intent.getIntExtra(ARGS_TYPES_SHIFT, 0)
                val tagsShift = intent.getIntExtra(ARGS_TAGS_SHIFT, 0)
                typeController.onRequestUpdate(
                    typeId = typeId,
                    selectedTypeId = selectedTypeId,
                    typesShift = typesShift,
                    tagsShift = tagsShift,
                )
            }
            ACTION_NOTIFICATION_TAG_CLICK -> {
                val typeId = intent.getLongExtra(ARGS_TYPE_ID, 0)
                val selectedTypeId = intent.getLongExtra(ARGS_SELECTED_TYPE_ID, 0)
                val tagId = intent.getLongExtra(ARGS_TAG_ID, 0)
                val typesShift = intent.getIntExtra(ARGS_TYPES_SHIFT, 0)
                typeController.onActionTagClick(
                    typeId = typeId,
                    selectedTypeId = selectedTypeId,
                    tagId = tagId,
                    typesShift = typesShift,
                )
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