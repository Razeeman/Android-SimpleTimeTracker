package com.example.util.simpletimetracker.feature_notification.goalTime.manager

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.view.ContextThemeWrapper
import android.view.View
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.util.simpletimetracker.core.extension.getBitmapFromView
import com.example.util.simpletimetracker.domain.di.AppContext
import com.example.util.simpletimetracker.feature_notification.R
import com.example.util.simpletimetracker.feature_notification.recordType.customView.NotificationIconView
import com.example.util.simpletimetracker.navigation.Router
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationGoalTimeManager @Inject constructor(
    @AppContext private val context: Context,
    private val router: Router
) {

    private val notificationManager: NotificationManagerCompat =
        NotificationManagerCompat.from(context)
    private val iconView = NotificationIconView(
        ContextThemeWrapper(context, R.style.AppTheme)
    ).apply {
        val size = context.resources.getDimensionPixelSize(R.dimen.notification_icon_size)
        val specWidth = View.MeasureSpec.makeMeasureSpec(size, View.MeasureSpec.EXACTLY)
        val specHeight = View.MeasureSpec.makeMeasureSpec(size, View.MeasureSpec.EXACTLY)
        measure(specWidth, specHeight)
        layout(0, 0, measuredWidth, measuredHeight)
    }

    fun show(params: NotificationGoalTimeParams) {
        val notification: Notification = buildNotification(params)
        createAndroidNotificationChannel()
        notificationManager.notify(NOTIFICATION_TAG, params.typeId.toInt(), notification)
    }

    fun hide(typeId: Long) {
        notificationManager.cancel(NOTIFICATION_TAG, typeId.toInt())
    }

    private fun buildNotification(params: NotificationGoalTimeParams): Notification {
        val notificationLayout = prepareView(params)

        val startIntent = router.getMainStartIntent().apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }

        val contentIntent = PendingIntent.getActivity(
            context,
            0,
            startIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(context, NOTIFICATIONS_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(contentIntent)
            .setCustomContentView(notificationLayout)
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
    }

    private fun createAndroidNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATIONS_CHANNEL_ID,
                NOTIFICATIONS_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun prepareView(params: NotificationGoalTimeParams): RemoteViews {
        val iconBitmap = iconView.apply {
            itemIcon = params.icon
            itemColor = params.color
        }.getBitmapFromView()

        return RemoteViews(context.packageName, R.layout.notification_goal_time_layout).apply {
            setTextViewText(R.id.tvNotificationGoalTimeText, params.text)
            setTextViewText(R.id.tvNotificationGoalTimeDescription, params.description)
            setImageViewBitmap(R.id.ivNotificationGoalTimeIcon, iconBitmap)
        }
    }

    companion object {
        private const val NOTIFICATIONS_CHANNEL_ID = "GOAL_TIME"
        private const val NOTIFICATIONS_CHANNEL_NAME = "Goal time"

        private const val NOTIFICATION_TAG = "goal_time_tag"
    }
}