package com.example.util.simpletimetracker.feature_notification.recevier

import android.app.AlarmManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.util.simpletimetracker.core.extension.goAsync
import com.example.util.simpletimetracker.core.utils.ACTION_START_ACTIVITY
import com.example.util.simpletimetracker.core.utils.ACTION_STOP_ACTIVITY
import com.example.util.simpletimetracker.core.utils.EXTRA_ACTIVITY_NAME
import com.example.util.simpletimetracker.core.utils.EXTRA_RECORD_COMMENT
import com.example.util.simpletimetracker.core.utils.EXTRA_RECORD_TAG_NAME
import com.example.util.simpletimetracker.domain.model.GoalTimeType
import com.example.util.simpletimetracker.feature_notification.automaticBackup.controller.AutomaticBackupBroadcastController
import com.example.util.simpletimetracker.feature_notification.goalTime.controller.NotificationGoalTimeBroadcastController
import com.example.util.simpletimetracker.feature_notification.inactivity.controller.NotificationInactivityBroadcastController
import com.example.util.simpletimetracker.feature_notification.recordType.controller.NotificationTypeBroadcastController
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class NotificationReceiver : BroadcastReceiver() {

    @Inject
    lateinit var typeController: NotificationTypeBroadcastController

    @Inject
    lateinit var inactivityController: NotificationInactivityBroadcastController

    @Inject
    lateinit var goalTimeController: NotificationGoalTimeBroadcastController

    @Inject
    lateinit var automaticBackupController: AutomaticBackupBroadcastController

    override fun onReceive(context: Context?, intent: Intent?) {
        val action = intent?.action
        if (context == null || intent == null || action == null) return

        when (action) {
            ACTION_INACTIVITY_REMINDER -> {
                inactivityController.onInactivityReminder()
            }
            ACTION_GOAL_TIME_REMINDER_SESSION,
            ACTION_GOAL_TIME_REMINDER_DAILY,
            ACTION_GOAL_TIME_REMINDER_WEEKLY,
            ACTION_GOAL_TIME_REMINDER_MONTHLY,
            -> {
                val typeId = intent.getLongExtra(EXTRA_GOAL_TIME_TYPE_ID, 0)
                val goalTimeType = when (action) {
                    ACTION_GOAL_TIME_REMINDER_SESSION -> GoalTimeType.Session
                    ACTION_GOAL_TIME_REMINDER_DAILY -> GoalTimeType.Day
                    ACTION_GOAL_TIME_REMINDER_WEEKLY -> GoalTimeType.Week
                    ACTION_GOAL_TIME_REMINDER_MONTHLY -> GoalTimeType.Month
                    else -> GoalTimeType.Session
                }
                goalTimeController.onGoalTimeReminder(typeId, goalTimeType)
            }
            ACTION_GOAL_TIME_REMINDER_DAY_END,
            ACTION_GOAL_TIME_REMINDER_WEEK_END,
            ACTION_GOAL_TIME_REMINDER_MONTH_END,
            -> {
                goalTimeController.onRangeEndReminder()
            }
            ACTION_AUTOMATIC_BACKUP -> goAsync() {
                automaticBackupController.onReminder()
            }
            ACTION_START_ACTIVITY -> {
                val name = intent.getStringExtra(EXTRA_ACTIVITY_NAME)
                val comment = intent.getStringExtra(EXTRA_RECORD_COMMENT)
                val tagName = intent.getStringExtra(EXTRA_RECORD_TAG_NAME)
                typeController.onActionActivityStart(
                    name = name,
                    comment = comment,
                    tagName = tagName
                )
            }
            ACTION_STOP_ACTIVITY -> {
                val name = intent.getStringExtra(EXTRA_ACTIVITY_NAME)
                typeController.onActionActivityStop(name)
            }
            Intent.ACTION_BOOT_COMPLETED -> {
                onBootCompleted()
            }
            AlarmManager.ACTION_SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED -> {
                goalTimeController.onExactAlarmPermissionStateChanged()
            }
        }
    }

    private fun onBootCompleted() {
        inactivityController.onBootCompleted()
        goalTimeController.onBootCompleted()
        typeController.onBootCompleted()
        automaticBackupController.onBootCompleted()
    }

    companion object {
        const val ACTION_INACTIVITY_REMINDER =
            "com.razeeman.util.simpletimetracker.ACTION_INACTIVITY_REMINDER"
        const val ACTION_GOAL_TIME_REMINDER_SESSION =
            "com.razeeman.util.simpletimetracker.ACTION_GOAL_TIME_REMINDER"
        const val ACTION_GOAL_TIME_REMINDER_DAILY =
            "com.razeeman.util.simpletimetracker.ACTION_GOAL_TIME_REMINDER_DAILY"
        const val ACTION_GOAL_TIME_REMINDER_WEEKLY =
            "com.razeeman.util.simpletimetracker.ACTION_GOAL_TIME_REMINDER_WEEKLY"
        const val ACTION_GOAL_TIME_REMINDER_MONTHLY =
            "com.razeeman.util.simpletimetracker.ACTION_GOAL_TIME_REMINDER_MONTHLY"
        const val ACTION_GOAL_TIME_REMINDER_DAY_END =
            "com.razeeman.util.simpletimetracker.ACTION_GOAL_TIME_REMINDER_DAY_END"
        const val ACTION_GOAL_TIME_REMINDER_WEEK_END =
            "com.razeeman.util.simpletimetracker.ACTION_GOAL_TIME_REMINDER_WEEK_END"
        const val ACTION_GOAL_TIME_REMINDER_MONTH_END =
            "com.razeeman.util.simpletimetracker.ACTION_GOAL_TIME_REMINDER_MONTH_END"
        const val ACTION_AUTOMATIC_BACKUP =
            "com.razeeman.util.simpletimetracker.ACTION_AUTOMATIC_BACKUP"

        const val EXTRA_GOAL_TIME_TYPE_ID =
            "extra_goal_time_type_id"
    }
}