package com.example.util.simpletimetracker.feature_notification.recordType.customView

import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.example.util.simpletimetracker.feature_views.viewData.RecordTypeIcon
import com.example.util.simpletimetracker.feature_notification.R
import com.example.util.simpletimetracker.feature_notification.databinding.NotificationIconViewLayoutBinding

class NotificationIconView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(
    context,
    attrs,
    defStyleAttr
) {

    private val binding: NotificationIconViewLayoutBinding = NotificationIconViewLayoutBinding
        .inflate(LayoutInflater.from(context), this)

    init {
        context.obtainStyledAttributes(
            attrs, R.styleable.NotificationIconView, defStyleAttr, 0
        ).run {
            if (hasValue(R.styleable.NotificationIconView_itemColor)) itemColor =
                getColor(R.styleable.NotificationIconView_itemColor, Color.BLACK)

            if (hasValue(R.styleable.NotificationIconView_itemIcon)) itemIcon =
                getResourceId(R.styleable.NotificationIconView_itemIcon, R.drawable.unknown)
                    .let(RecordTypeIcon::Image)

            recycle()
        }
    }

    var itemColor: Int = 0
        set(value) {
            binding.ivNotificationIconBackground.background.colorFilter =
                PorterDuffColorFilter(value, PorterDuff.Mode.SRC_IN)
            field = value
        }

    var itemIcon: RecordTypeIcon = RecordTypeIcon.Image(0)
        set(value) {
            binding.ivNotificationIcon.itemIcon = value
            field = value
        }
}