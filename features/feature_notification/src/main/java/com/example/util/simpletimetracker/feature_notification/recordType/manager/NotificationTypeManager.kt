package com.example.util.simpletimetracker.feature_notification.recordType.manager

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.os.SystemClock
import android.view.ContextThemeWrapper
import android.view.View
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.util.simpletimetracker.core.utils.PendingIntents
import com.example.util.simpletimetracker.domain.extension.orFalse
import com.example.util.simpletimetracker.feature_notification.R
import com.example.util.simpletimetracker.feature_notification.activitySwitch.manager.NotificationControlsManager
import com.example.util.simpletimetracker.feature_notification.activitySwitch.manager.NotificationControlsManager.Companion.ARGS_TYPE_ID
import com.example.util.simpletimetracker.feature_notification.recevier.NotificationReceiver
import com.example.util.simpletimetracker.feature_notification.recordType.customView.NotificationIconView
import com.example.util.simpletimetracker.feature_views.extension.getBitmapFromView
import com.example.util.simpletimetracker.feature_views.extension.measureExactly
import com.example.util.simpletimetracker.feature_views.viewData.RecordTypeIcon
import com.example.util.simpletimetracker.navigation.Router
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationTypeManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val controlsManager: NotificationControlsManager,
    private val router: Router,
) {

    private val notificationManager: NotificationManagerCompat =
        NotificationManagerCompat.from(context)
    private val iconView =
        NotificationIconView(ContextThemeWrapper(context, R.style.AppTheme))
    private val iconSize by lazy {
        context.resources.getDimensionPixelSize(R.dimen.notification_icon_size)
    }

    fun show(params: NotificationTypeParams) {
        val notification: Notification = buildNotification(params)
        createAndroidNotificationChannel()
        notificationManager.notify(params.id.toInt(), notification)
    }

    fun hide(id: Int) {
        notificationManager.cancel(id)
    }

    private fun buildNotification(params: NotificationTypeParams): Notification {
        val startIntent = router.getMainStartIntent().apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }

        val contentIntent = PendingIntent.getActivity(
            context,
            0,
            startIntent,
            PendingIntents.getFlags(),
        )
        val stopIntent = getStopIntent(
            context = context,
            requestCode = params.id.toInt(),
            recordTypeId = params.id,
        )

        val builder = NotificationCompat.Builder(context, NOTIFICATIONS_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(contentIntent)
            .setOngoing(true)
            .setAutoCancel(false)
            .setCustomContentView(prepareView(params, isBig = false))
            .setCustomBigContentView(prepareView(params, isBig = true))
            .addAction(0, params.stopButton, stopIntent)
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setPriority(NotificationCompat.PRIORITY_LOW) // no sound
        return builder.build().apply {
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
        params: NotificationTypeParams,
        isBig: Boolean,
    ): RemoteViews {
        return RemoteViews(context.packageName, R.layout.notification_record_layout).apply {
            setTextViewText(R.id.tvNotificationText, params.text)
            setTextViewText(R.id.tvNotificationTimeStarted, params.timeStarted)
            setImageViewBitmap(R.id.ivNotificationIcon, getIconBitmap(params.icon, params.color))

            val base = SystemClock.elapsedRealtime() - (System.currentTimeMillis() - params.startedTimeStamp)
            setChronometer(R.id.timerNotification, base, null, true)

            if (params.totalDuration != null) {
                val baseTotal = SystemClock.elapsedRealtime() - params.totalDuration
                setChronometer(R.id.timerNotificationTotal, baseTotal, "(%s)", true)
                setViewVisibility(R.id.timerNotificationTotal, View.VISIBLE)
            } else {
                setViewVisibility(R.id.timerNotificationTotal, View.GONE)
            }

            if (params.goalTime.isNotEmpty() && isBig) {
                // TODO show only closest goal time, count down to it?
                setTextViewText(R.id.tvNotificationGoalTime, params.goalTime)
                setViewVisibility(R.id.tvNotificationGoalTime, View.VISIBLE)
            } else {
                setViewVisibility(R.id.tvNotificationGoalTime, View.GONE)
            }

            controlsManager.getControlsView(
                from = NotificationControlsManager.From.ActivityNotification(params.id),
                controls = params.controls,
                isBig = isBig,
            )?.let {
                addView(R.id.containerNotificationMainContent, it)
            }
        }
    }

    private fun getStopIntent(
        context: Context,
        requestCode: Int,
        recordTypeId: Long,
    ): PendingIntent {
        val intent = Intent(context, NotificationReceiver::class.java)
        intent.action = ACTION_NOTIFICATION_TYPE_STOP
        intent.putExtra(ARGS_TYPE_ID, recordTypeId)
        return PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntents.getFlags(),
        )
    }

    private fun getIconBitmap(
        icon: RecordTypeIcon,
        color: Int,
        isChecked: Boolean? = null,
        isComplete: Boolean = false,
    ): Bitmap = synchronized(iconView) {
        return iconView.apply {
            itemIcon = icon
            itemColor = color
            itemWithCheck = isChecked != null
            itemIsChecked = isChecked.orFalse()
            itemIsComplete = isComplete
            measureExactly(iconSize)
        }.getBitmapFromView()
    }

    companion object {
        const val ACTION_NOTIFICATION_TYPE_STOP =
            "com.example.util.simpletimetracker.feature_notification.recordType.onStop"

        private const val NOTIFICATIONS_CHANNEL_ID = "NOTIFICATIONS"
        private const val NOTIFICATIONS_CHANNEL_NAME = "Notifications"
    }
}