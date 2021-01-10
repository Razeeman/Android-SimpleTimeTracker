package com.example.util.simpletimetracker.feature_notification.recordType.manager

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Build
import android.view.ContextThemeWrapper
import android.view.View
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.util.simpletimetracker.domain.di.AppContext
import com.example.util.simpletimetracker.feature_notification.R
import com.example.util.simpletimetracker.feature_notification.recordType.customView.NotificationIconView
import com.example.util.simpletimetracker.navigation.Router
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationTypeManager @Inject constructor(
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

    fun show(params: NotificationTypeParams) {
        val notification: Notification = buildNotification(params)
        createAndroidNotificationChannel()
        notificationManager.notify(params.id, notification)
    }

    fun hide(id: Int) {
        notificationManager.cancel(id)
    }

    private fun buildNotification(params: NotificationTypeParams): Notification {
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

        val builder = NotificationCompat.Builder(context, NOTIFICATIONS_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(contentIntent)
            .setOngoing(true)
            .setAutoCancel(false)
            .setCustomContentView(notificationLayout)
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setPriority(NotificationCompat.PRIORITY_LOW) // no sound
            .setUsesChronometer(true)
            .setWhen(params.startedTimeStamp)
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

    private fun prepareView(params: NotificationTypeParams): RemoteViews {
        val iconBitmap = iconView.apply {
            itemIcon = params.icon
            itemColor = params.color
        }.let(::getBitmapFromView)

        return RemoteViews(context.packageName, R.layout.notification_layout).apply {
            setTextViewText(R.id.tvNotificationText, params.text)
            setTextViewText(R.id.tvNotificationDescription, params.description)
            setImageViewBitmap(R.id.ivNotificationIcon, iconBitmap)
        }
    }

    private fun getBitmapFromView(view: View): Bitmap {
        return Bitmap.createBitmap(
            view.measuredWidth,
            view.measuredHeight,
            Bitmap.Config.ARGB_8888
        ).also {
            view.draw(Canvas(it))
        }
    }

    companion object {
        private const val NOTIFICATIONS_CHANNEL_ID = "NOTIFICATIONS"
        private const val NOTIFICATIONS_CHANNEL_NAME = "Notifications"
    }
}