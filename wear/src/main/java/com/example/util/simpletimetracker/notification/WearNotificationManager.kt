/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.example.util.simpletimetracker.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.SystemClock
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.wear.ongoing.OngoingActivity
import androidx.wear.ongoing.Status
import com.example.util.simpletimetracker.R
import com.example.util.simpletimetracker.data.WearDataRepo
import com.example.util.simpletimetracker.data.WearIconMapper
import com.example.util.simpletimetracker.data.WearPermissionRepo
import com.example.util.simpletimetracker.domain.model.WearActivity
import com.example.util.simpletimetracker.domain.model.WearActivityIcon
import com.example.util.simpletimetracker.domain.model.WearCurrentActivity
import com.example.util.simpletimetracker.utils.getMainStartIntent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class WearNotificationManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val wearDataRepo: WearDataRepo,
    private val wearIconMapper: WearIconMapper,
    private val wearPermissionRepo: WearPermissionRepo,
) {
    private val notificationManager: NotificationManagerCompat =
        NotificationManagerCompat.from(context)

    suspend fun updateNotifications() {
        val currentActivity = wearDataRepo.loadCurrentActivities(forceReload = false)
            .getOrNull()
            .orEmpty()
            .maxByOrNull { it.startedAt }
        val activity = wearDataRepo.loadActivities(forceReload = false)
            .getOrNull()
            .orEmpty()
            .firstOrNull { it.id == currentActivity?.id }

        hide()
        if (currentActivity != null && activity != null) {
            show(currentActivity, activity)
        }
    }

    private fun hide() {
        notificationManager.cancelAll()
    }

    private fun show(
        currentActivity: WearCurrentActivity,
        activity: WearActivity,
    ) {
        if (!wearPermissionRepo.checkPostNotificationsPermission()) return

        // TODO can pass bitmap but emulator crashing.
        val icon = when (
            val activityIcon = wearIconMapper.mapIcon(activity.icon)
        ) {
            is WearActivityIcon.Image -> activityIcon.iconId
            is WearActivityIcon.Text -> R.drawable.app_ic_launcher_monochrome
        }
        val notificationBuilder = NotificationCompat
            .Builder(context, NOTIFICATIONS_CHANNEL_ID)
            .setSmallIcon(icon)
            .setOngoing(true)

        val statusTemplate = "#$TEMPLATE_PART_TIME# #$TEMPLATE_PART_TYPE#"
        val timeStarted = currentActivity.startedAt
        val runStartTime = SystemClock.elapsedRealtime() - (System.currentTimeMillis() - timeStarted)

        val ongoingActivityStatus = Status.Builder()
            .addTemplate(statusTemplate)
            .addPart(TEMPLATE_PART_TYPE, Status.TextPart(activity.name))
            .addPart(TEMPLATE_PART_TIME, Status.StopwatchPart(runStartTime))
            .build()

        val ongoingActivity = OngoingActivity
            .Builder(context, NOTIFICATION_ID, notificationBuilder)
            .setStaticIcon(icon)
            .setTouchIntent(getMainStartIntent(context))
            .setStatus(ongoingActivityStatus)
            .build()

        ongoingActivity.apply(context)
        createAndroidNotificationChannel()
        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())
    }

    private fun createAndroidNotificationChannel() {
        val channel = NotificationChannel(
            NOTIFICATIONS_CHANNEL_ID,
            NOTIFICATIONS_CHANNEL_NAME,
            NotificationManager.IMPORTANCE_LOW, // no sound
        )
        notificationManager.createNotificationChannel(channel)
    }

    companion object {
        private const val NOTIFICATIONS_CHANNEL_ID = "ACTIVITY"
        private const val NOTIFICATIONS_CHANNEL_NAME = "Activity"
        private const val NOTIFICATION_ID = 0

        private const val TEMPLATE_PART_TYPE = "type"
        private const val TEMPLATE_PART_TIME = "time"
    }
}