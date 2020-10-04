package com.example.util.simpletimetracker.notification

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
import com.example.util.simpletimetracker.R
import com.example.util.simpletimetracker.core.extension.dpToPx
import com.example.util.simpletimetracker.core.manager.NotificationParams
import com.example.util.simpletimetracker.domain.di.AppContext
import com.example.util.simpletimetracker.ui.MainActivity
import javax.inject.Inject

class NotificationManagerImpl @Inject constructor(
    @AppContext private val context: Context
) : com.example.util.simpletimetracker.core.manager.NotificationManager {

    private val notificationManager: NotificationManagerCompat =
        NotificationManagerCompat.from(context)
    private val iconView = NotificationIconView(
        ContextThemeWrapper(context, R.style.AppTheme)
    ).apply {
        val specWidth = View.MeasureSpec.makeMeasureSpec(34.dpToPx(), View.MeasureSpec.EXACTLY)
        val specHeight = View.MeasureSpec.makeMeasureSpec(34.dpToPx(), View.MeasureSpec.EXACTLY)
        measure(specWidth, specHeight)
        layout(0, 0, measuredWidth, measuredHeight)
    }

    override fun show(params: NotificationParams) {
        val notification: Notification = buildNotification(params)
        createAndroidNotificationChannel()
        notificationManager.notify(params.id, notification)
    }

    override fun hide(id: Int) {
        notificationManager.cancel(id)
    }

    private fun buildNotification(params: NotificationParams): Notification {
        val notificationLayout = prepareView(params)

        val startIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val contentIntent = PendingIntent.getActivity(
            context,
            0,
            startIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        val builder = NotificationCompat.Builder(context, REMINDERS_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(contentIntent)
            .setOngoing(true)
            .setCustomContentView(notificationLayout)
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
        return builder.build()
    }

    private fun createAndroidNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                REMINDERS_CHANNEL_ID,
                REMINDERS_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun prepareView(params: NotificationParams): RemoteViews {
        val iconBitmap = iconView.apply {
            itemIcon = params.icon
            itemColor = params.color
        }.let(::getBitmapFromView)

        return RemoteViews(context.packageName, R.layout.notification_layout).apply {
            setTextViewText(R.id.tvNotificationName, params.text)
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
        private const val REMINDERS_CHANNEL_ID = "REMINDERS"
        private const val REMINDERS_CHANNEL_NAME = "Notifications"
    }
}