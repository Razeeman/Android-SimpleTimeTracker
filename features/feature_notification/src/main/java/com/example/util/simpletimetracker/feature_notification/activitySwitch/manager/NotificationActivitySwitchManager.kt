package com.example.util.simpletimetracker.feature_notification.activitySwitch.manager

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.view.ContextThemeWrapper
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.util.simpletimetracker.core.extension.allowVmViolations
import com.example.util.simpletimetracker.feature_views.extension.getBitmapFromView
import com.example.util.simpletimetracker.feature_views.extension.measureExactly
import com.example.util.simpletimetracker.core.mapper.ColorMapper
import com.example.util.simpletimetracker.core.utils.PendingIntents
import com.example.util.simpletimetracker.feature_views.viewData.RecordTypeIcon
import com.example.util.simpletimetracker.feature_notification.R
import com.example.util.simpletimetracker.feature_notification.recordType.customView.NotificationIconView
import com.example.util.simpletimetracker.navigation.Router
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationActivitySwitchManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val colorMapper: ColorMapper,
    private val router: Router,
    private val controlsManager: NotificationControlsManager,
) {

    private val notificationManager: NotificationManagerCompat =
        NotificationManagerCompat.from(context)
    private val iconView = allowVmViolations {
        NotificationIconView(ContextThemeWrapper(context, R.style.AppTheme))
    }
    private val iconSize by lazy {
        context.resources.getDimensionPixelSize(R.dimen.notification_icon_size)
    }

    fun show(params: NotificationActivitySwitchParams) {
        val notification: Notification = buildNotification(params)
        createAndroidNotificationChannel()
        notificationManager.notify(NOTIFICATION_TAG, NOTIFICATION_ID, notification)
    }

    fun hide() {
        notificationManager.cancel(NOTIFICATION_TAG, NOTIFICATION_ID)
    }

    private fun buildNotification(params: NotificationActivitySwitchParams): Notification {
        val startIntent = router.getMainStartIntent().apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }

        val contentIntent = PendingIntent.getActivity(
            context,
            0,
            startIntent,
            PendingIntents.getFlags(),
        )

        // TODO fix default duration type click when show tags is enabled,
        //  no animation / indication.
        // TODO reshow in dismiss to keep sticky on API 34? setDeleteIntent()
        return NotificationCompat.Builder(context, NOTIFICATIONS_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(contentIntent)
            .setOngoing(true)
            .setAutoCancel(false)
            .setCustomContentView(prepareView(params, isBig = false))
            .setCustomBigContentView(prepareView(params, isBig = true))
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setPriority(NotificationCompat.PRIORITY_LOW) // no sound
            .build().apply {
                flags = flags or Notification.FLAG_NO_CLEAR
            }
    }

    private fun createAndroidNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATIONS_CHANNEL_ID,
                NOTIFICATIONS_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW, // no sound
            )
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun prepareView(
        params: NotificationActivitySwitchParams,
        isBig: Boolean,
    ): RemoteViews {
        val iconBitmap = iconView.apply {
            itemIcon = RecordTypeIcon.Image(R.drawable.app_ic_launcher_monochrome)
            itemColor = colorMapper.toUntrackedColor(params.isDarkTheme)
            measureExactly(iconSize)
        }.getBitmapFromView()

        return RemoteViews(context.packageName, R.layout.notification_activity_switch_layout).apply {
            setImageViewBitmap(R.id.ivNotificationActivitySwitchIcon, iconBitmap)
            setTextViewText(R.id.tvNotificationActivitySwitchText, params.title)

            controlsManager.getControlsView(
                from = NotificationControlsManager.From.ActivitySwitch,
                controls = params.controls,
                isBig = isBig,
            )?.let {
                addView(R.id.containerNotificationMainContent, it)
            }
        }
    }

    companion object {
        private const val NOTIFICATIONS_CHANNEL_ID = "ACTIVITY_SWITCH"
        private const val NOTIFICATIONS_CHANNEL_NAME = "Activity Switch"

        private const val NOTIFICATION_TAG = "activity_switch_tag"
        private const val NOTIFICATION_ID = 0
    }
}