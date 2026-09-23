package com.example.util.simpletimetracker.feature_notification.recevier

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
import com.example.util.simpletimetracker.feature_notification.automaticBackup.controller.AutomaticBackupBroadcastController
import com.example.util.simpletimetracker.feature_notification.automaticExport.controller.AutomaticExportBroadcastController
import com.example.util.simpletimetracker.feature_notification.external.NotificationExternalBroadcastController
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ExternalNotificationReceiver : BroadcastReceiver() {

    @Inject
    lateinit var externalController: NotificationExternalBroadcastController

    @Inject
    lateinit var automaticBackupController: AutomaticBackupBroadcastController

    @Inject
    lateinit var automaticExportController: AutomaticExportBroadcastController

    override fun onReceive(context: Context?, intent: Intent?) {
        val action = intent?.action ?: return
        if (action !in EXTERNAL_ACTIONS) return
        goAsync { handleIntent(intent, action) }
    }

    private suspend fun handleIntent(intent: Intent, action: String) {
        when (action) {
            ACTION_EXTERNAL_AUTOMATIC_BACKUP -> {
                try {
                    automaticBackupController.onReminder()
                } finally {
                    automaticBackupController.onFinished()
                }
            }
            ACTION_EXTERNAL_AUTOMATIC_EXPORT -> {
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
        }
    }

    private fun String.splitTagNames(): List<String> {
        return split(',').map(String::trim)
    }

    private companion object {
        val EXTERNAL_ACTIONS = setOf(
            ACTION_EXTERNAL_START_ACTIVITY,
            ACTION_EXTERNAL_STOP_ACTIVITY,
            ACTION_EXTERNAL_STOP_ALL_ACTIVITIES,
            ACTION_EXTERNAL_STOP_SHORTEST_ACTIVITY,
            ACTION_EXTERNAL_STOP_LONGEST_ACTIVITY,
            ACTION_EXTERNAL_RESTART_ACTIVITY,
            ACTION_EXTERNAL_ADD_RECORD,
            ACTION_EXTERNAL_CHANGE_RECORD,
            ACTION_EXTERNAL_CREATE_RECORD_TAG,
            ACTION_EXTERNAL_AUTOMATIC_BACKUP,
            ACTION_EXTERNAL_AUTOMATIC_EXPORT,
        )
    }
}
