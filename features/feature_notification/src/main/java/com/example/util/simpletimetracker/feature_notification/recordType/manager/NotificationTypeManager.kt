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
import com.example.util.simpletimetracker.feature_notification.R
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
    private val router: Router
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
        val stopIntent = getPendingSelfIntent(
            context = context,
            action = ACTION_NOTIFICATION_STOP,
            recordTypeId = params.id
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
                NotificationManager.IMPORTANCE_LOW // no sound
            )
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun prepareView(
        params: NotificationTypeParams,
        isBig: Boolean,
    ): RemoteViews {
        return RemoteViews(context.packageName, R.layout.notification_record_layout).apply {
            setViewVisibility(R.id.containerNotificationControls, if (isBig) View.VISIBLE else View.GONE)
            setTextViewText(R.id.tvNotificationText, params.text)
            setTextViewText(R.id.tvNotificationTimeStarted, params.timeStarted)
            setTextViewText(R.id.tvNotificationGoalTime, params.goalTime)
            setImageViewBitmap(R.id.ivNotificationIcon, getIconBitmap(params.icon, params.color))
            val base = SystemClock.elapsedRealtime() - (System.currentTimeMillis() - params.startedTimeStamp)
            setChronometer(R.id.timerNotification, base, null, true)

            addTypesControlIcon(
                icon = params.controlIconPrev,
                color = params.controlIconColor,
                intent = getPendingSelfIntent(
                    context = context,
                    action = ACTION_NOTIFICATION_PREV,
                    recordTypeId = params.id,
                    recordTypesShift = (params.typesShift - TYPES_LIST_SIZE).coerceAtLeast(0),
                )
            )
            params.types.drop(params.typesShift).take(TYPES_LIST_SIZE).forEach {
                addTypesControlIcon(
                    icon = it.icon,
                    color = it.color,
                    intent = getPendingSelfIntent(
                        context = context,
                        action = if (it.id == params.id) ACTION_NOTIFICATION_STOP else ACTION_NOTIFICATION_START,
                        recordTypeId = it.id
                    )
                )
            }
            addTypesControlIcon(
                icon = params.controlIconNext,
                color = params.controlIconColor,
                intent = getPendingSelfIntent(
                    context = context,
                    action = ACTION_NOTIFICATION_NEXT,
                    recordTypeId = params.id,
                    recordTypesShift = (params.typesShift + TYPES_LIST_SIZE)
                        .takeUnless { it >= params.types.size }
                        ?: params.typesShift
                )
            )
        }
    }

    private fun RemoteViews.addTypesControlIcon(
        icon: RecordTypeIcon,
        color: Int,
        intent: PendingIntent,
    ) {
        RemoteViews(context.packageName, R.layout.notification_container_layout)
            .apply {
                setImageViewBitmap(R.id.ivNotificationContainer, getIconBitmap(icon, color))
                setOnClickPendingIntent(R.id.btnNotificationContainer, intent)
            }
            .let {
                addView(R.id.containerNotificationControls, it)
            }
    }

    private fun getPendingSelfIntent(
        context: Context,
        action: String,
        recordTypeId: Long,
        recordTypesShift: Int? = null
    ): PendingIntent {
        val intent = Intent(context, NotificationReceiver::class.java)
        intent.action = action
        intent.putExtra(ARGS_RECORD_TYPE_ID, recordTypeId)
        recordTypesShift?.let { intent.putExtra(ARGS_RECORD_TYPES_SHIFT, it) }
        return PendingIntent.getBroadcast(context, recordTypeId.toInt(), intent, PendingIntents.getFlags())
    }

    private fun getIconBitmap(
        icon: RecordTypeIcon,
        color: Int
    ): Bitmap {
        return iconView.apply {
            itemIcon = icon
            itemColor = color
            measureExactly(iconSize)
        }.getBitmapFromView()
    }

    companion object {
        const val ACTION_NOTIFICATION_STOP =
            "com.example.util.simpletimetracker.feature_notification.recordType.onStopClick"
        const val ACTION_NOTIFICATION_START =
            "com.example.util.simpletimetracker.feature_notification.recordType.onStartClick"
        const val ACTION_NOTIFICATION_PREV =
            "com.example.util.simpletimetracker.feature_notification.recordType.onPrevClick"
        const val ACTION_NOTIFICATION_NEXT =
            "com.example.util.simpletimetracker.feature_notification.recordType.onNextClick"

        const val ARGS_RECORD_TYPE_ID = "recordTypeId"
        const val ARGS_RECORD_TYPES_SHIFT = "recordTypesShift"

        private const val NOTIFICATIONS_CHANNEL_ID = "NOTIFICATIONS"
        private const val NOTIFICATIONS_CHANNEL_NAME = "Notifications"

        private const val TYPES_LIST_SIZE = 7
    }
}