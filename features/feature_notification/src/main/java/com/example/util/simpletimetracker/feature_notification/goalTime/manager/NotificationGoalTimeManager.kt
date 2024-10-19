package com.example.util.simpletimetracker.feature_notification.goalTime.manager

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.view.ContextThemeWrapper
import android.view.View
import android.widget.RemoteViews
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.util.simpletimetracker.core.extension.allowVmViolations
import com.example.util.simpletimetracker.core.utils.PendingIntents
import com.example.util.simpletimetracker.domain.model.RecordTypeGoal
import com.example.util.simpletimetracker.feature_notification.R
import com.example.util.simpletimetracker.feature_notification.recordType.customView.NotificationIconView
import com.example.util.simpletimetracker.feature_views.extension.getBitmapFromView
import com.example.util.simpletimetracker.feature_views.extension.measureExactly
import com.example.util.simpletimetracker.navigation.Router
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationGoalTimeManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val router: Router,
) {

    private val notificationManager: NotificationManagerCompat =
        NotificationManagerCompat.from(context)
    private val iconView = allowVmViolations {
        NotificationIconView(ContextThemeWrapper(context, R.style.AppTheme))
    }
    private val iconSize by lazy {
        context.resources.getDimensionPixelSize(R.dimen.notification_icon_size)
    }
    private val checkView = allowVmViolations {
        AppCompatImageView(ContextThemeWrapper(context, R.style.AppTheme))
    }.apply {
        val size = context.resources.getDimensionPixelSize(R.dimen.notification_icon_half_size)
        val specWidth = View.MeasureSpec.makeMeasureSpec(size, View.MeasureSpec.EXACTLY)
        val specHeight = View.MeasureSpec.makeMeasureSpec(size, View.MeasureSpec.EXACTLY)
        measure(specWidth, specHeight)
        layout(0, 0, measuredWidth, measuredHeight)
    }

    fun show(params: NotificationGoalTimeParams) {
        val notification: Notification = buildNotification(params)
        createAndroidNotificationChannel()
        notificationManager.notify(
            getNotificationTag(params.goalRange),
            params.idData.value.toInt(),
            notification,
        )
    }

    fun hide(
        idData: RecordTypeGoal.IdData,
        goalRange: RecordTypeGoal.Range,
    ) {
        notificationManager.cancel(getNotificationTag(goalRange), idData.value.toInt())
    }

    private fun getNotificationTag(goalRange: RecordTypeGoal.Range): String {
        return NOTIFICATION_TAG + when (goalRange) {
            is RecordTypeGoal.Range.Session -> "" // back support for previous versions, keep same tag
            is RecordTypeGoal.Range.Daily -> "day"
            is RecordTypeGoal.Range.Weekly -> "week"
            is RecordTypeGoal.Range.Monthly -> "month"
        }
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
            PendingIntents.getFlags(),
        )

        return NotificationCompat.Builder(context, NOTIFICATIONS_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(contentIntent)
            .setCustomContentView(notificationLayout)
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
            .build()
    }

    private fun createAndroidNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATIONS_CHANNEL_ID,
                NOTIFICATIONS_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT,
            )
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun prepareView(params: NotificationGoalTimeParams): RemoteViews {
        val iconBitmap = synchronized(iconView) {
            iconView.apply {
                itemIcon = params.icon
                itemColor = params.color
                measureExactly(iconSize)
            }.getBitmapFromView()
        }
        val checkBitmap = synchronized(checkView) {
            checkView.apply {
                setBackgroundResource(R.drawable.spinner_check_mark)
            }.getBitmapFromView()
        }

        return RemoteViews(context.packageName, R.layout.notification_goal_time_layout).apply {
            setTextViewText(R.id.tvNotificationGoalTimeText, params.text)
            setTextViewText(R.id.tvNotificationGoalTimeDescription, params.description)
            setImageViewBitmap(R.id.ivNotificationGoalTimeIcon, iconBitmap)
            setImageViewBitmap(R.id.ivNotificationGoalTimeCheck, checkBitmap)
        }
    }

    companion object {
        private const val NOTIFICATIONS_CHANNEL_ID = "GOAL_TIME"
        private const val NOTIFICATIONS_CHANNEL_NAME = "Goal time"

        private const val NOTIFICATION_TAG = "goal_time_tag"
    }
}