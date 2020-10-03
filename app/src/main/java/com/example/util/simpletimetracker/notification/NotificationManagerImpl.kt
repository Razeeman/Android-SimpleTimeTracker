package com.example.util.simpletimetracker.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.widget.RemoteViews
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat
import com.example.util.simpletimetracker.R
import com.example.util.simpletimetracker.core.manager.NotificationParams
import com.example.util.simpletimetracker.domain.di.AppContext
import com.example.util.simpletimetracker.ui.MainActivity
import javax.inject.Inject

class NotificationManagerImpl @Inject constructor(
    @AppContext private val context: Context
) : com.example.util.simpletimetracker.core.manager.NotificationManager {

    private val notificationManager: NotificationManagerCompat =
        NotificationManagerCompat.from(context)

    override fun show(params: NotificationParams) {
        val notification: Notification = buildNotification(params)
        createAndroidNotificationChannel()
        notificationManager.notify(params.id, notification)
    }

    override fun hide(id: Int) {
        notificationManager.cancel(id)
    }

    private fun buildNotification(params: NotificationParams): Notification {
        val notificationLayout = RemoteViews(context.packageName, R.layout.notification_layout)
        notificationLayout.setTextViewText(R.id.tvNotificationName, params.text)

        // TODO add runCatching everywhere
        val icon = getVectorDrawable(params.icon)
        val iconBackground = ContextCompat.getDrawable(context, R.drawable.circle_drawable)
            ?.apply { colorFilter = PorterDuffColorFilter(params.color, PorterDuff.Mode.SRC_IN) }
            ?.let { getBitmap(it) }

        notificationLayout.setImageViewBitmap(R.id.ivNotificationIcon, icon)
        notificationLayout.setImageViewBitmap(R.id.ivNotificationIconBackground, iconBackground)

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

    // TODO move to utils and from pie chart?
    private fun getVectorDrawable(iconId: Int): Bitmap? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            (AppCompatResources.getDrawable(context, iconId) as? BitmapDrawable)?.apply {
                colorFilter = PorterDuffColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN)
                return bitmap
            }
        } else {
            VectorDrawableCompat.create(context.resources, iconId, context.theme)?.apply {
                setTintList(ColorStateList.valueOf(Color.WHITE))
                return getBitmap(this)
            }
        }

        return null
    }

    private fun getBitmap(drawable: Drawable): Bitmap {
        val bitmap = Bitmap.createBitmap(
            drawable.intrinsicWidth,
            drawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }

    companion object {
        private const val REMINDERS_CHANNEL_ID = "REMINDERS"
        private const val REMINDERS_CHANNEL_NAME = "Notifications"
    }
}