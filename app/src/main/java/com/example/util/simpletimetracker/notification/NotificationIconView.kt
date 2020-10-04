package com.example.util.simpletimetracker.notification

import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import com.example.util.simpletimetracker.R
import kotlinx.android.synthetic.main.notification_icon_view_layout.view.*

class NotificationIconView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(
    context,
    attrs,
    defStyleAttr
) {

    init {
        View.inflate(context, R.layout.notification_icon_view_layout, this)

        context.obtainStyledAttributes(attrs, R.styleable.NotificationIconView, defStyleAttr, 0)
            .run {
                itemColor =
                    getColor(R.styleable.NotificationIconView_itemColor, Color.BLACK)
                itemIcon =
                    getResourceId(R.styleable.NotificationIconView_itemIcon, R.drawable.unknown)
                recycle()
            }
    }

    var itemColor: Int = 0
        set(value) {
            ivNotificationIconBackground.background.colorFilter =
                PorterDuffColorFilter(value, PorterDuff.Mode.SRC_IN)
            field = value
        }

    var itemIcon: Int = 0
        set(value) {
            ivNotificationIcon.setBackgroundResource(value)
            field = value
        }
}