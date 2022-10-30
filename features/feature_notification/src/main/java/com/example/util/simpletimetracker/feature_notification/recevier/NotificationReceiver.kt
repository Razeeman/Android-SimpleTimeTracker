package com.example.util.simpletimetracker.feature_notification.recevier

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.util.simpletimetracker.core.utils.ACTION_START_ACTIVITY
import com.example.util.simpletimetracker.core.utils.ACTION_STOP_ACTIVITY
import com.example.util.simpletimetracker.core.utils.EXTRA_ACTIVITY_NAME
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

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null || intent.action == null) return

        when (intent.action) {
            ACTION_INACTIVITY_REMINDER -> {
                inactivityController.onInactivityReminder()
            }
            ACTION_GOAL_TIME_REMINDER -> {
                val typeId = intent.getLongExtra(EXTRA_GOAL_TIME_TYPE_ID, 0)
                goalTimeController.onGoalTimeReminder(typeId)
            }
            ACTION_START_ACTIVITY -> {
                val name = intent.getStringExtra(EXTRA_ACTIVITY_NAME)
                typeController.onActionActivityStart(name)
            }
            ACTION_STOP_ACTIVITY -> {
                val name = intent.getStringExtra(EXTRA_ACTIVITY_NAME)
                typeController.onActionActivityStop(name)
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
            "com.razeeman.util.simpletimetracker.ACTION_INACTIVITY_REMINDER"
        const val ACTION_GOAL_TIME_REMINDER =
            "com.razeeman.util.simpletimetracker.ACTION_GOAL_TIME_REMINDER"
        const val EXTRA_GOAL_TIME_TYPE_ID =
            "extra_goal_time_type_id"
    }
}