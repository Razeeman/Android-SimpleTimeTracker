package com.example.util.simpletimetracker.feature_notification.recevier

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.util.simpletimetracker.feature_notification.di.NotificationComponentProvider
import com.example.util.simpletimetracker.feature_notification.goalTime.controller.NotificationGoalTimeBroadcastController
import com.example.util.simpletimetracker.feature_notification.inactivity.controller.NotificationInactivityBroadcastController
import com.example.util.simpletimetracker.feature_notification.recordType.controller.NotificationTypeBroadcastController
import javax.inject.Inject

class NotificationReceiver : BroadcastReceiver() {

    @Inject
    lateinit var typeController: NotificationTypeBroadcastController

    @Inject
    lateinit var inactivityController: NotificationInactivityBroadcastController

    @Inject
    lateinit var goalTimeController: NotificationGoalTimeBroadcastController

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null || intent.action == null) return

        (context.applicationContext as? NotificationComponentProvider)
            ?.notificationComponent
            ?.inject(this)

        when (intent.action) {
            ACTION_INACTIVITY_REMINDER -> {
                inactivityController.onInactivityReminder()
            }
            ACTION_GOAL_TIME_REMINDER -> {
                val typeId = intent.getLongExtra(EXTRA_GOAL_TIME_TYPE_ID, 0)
                goalTimeController.onGoalTimeReminder(typeId)
            }
            Intent.ACTION_BOOT_COMPLETED -> {
                onBootCompleted()
            }
        }
    }

    private fun onBootCompleted() {
        inactivityController.onBootCompleted()
        goalTimeController.onBootCompleted()
        typeController.onBootCompleted()
    }

    companion object {
        const val ACTION_INACTIVITY_REMINDER =
            "com.example.util.simpletimetracker.ACTION_INACTIVITY_REMINDER"
        const val ACTION_GOAL_TIME_REMINDER =
            "com.example.util.simpletimetracker.ACTION_GOAL_TIME_REMINDER"
        const val EXTRA_GOAL_TIME_TYPE_ID =
            "extra_goal_time_type_id"
    }
}