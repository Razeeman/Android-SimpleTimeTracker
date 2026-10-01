package com.example.util.simpletimetracker.feature_notification.recordType.customView

import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.util.AttributeSet
import android.widget.RelativeLayout
import androidx.core.view.isVisible
import com.example.util.simpletimetracker.feature_notification.R
import com.example.util.simpletimetracker.feature_notification.databinding.NotificationIconViewLayoutBinding
import com.example.util.simpletimetracker.feature_views.GoalCheckmarkView.CheckState
import com.example.util.simpletimetracker.feature_views.extension.layoutInflater
import com.example.util.simpletimetracker.feature_views.viewData.RecordTypeIcon
import androidx.core.content.withStyledAttributes

class NotificationIconView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : RelativeLayout(
    context,
    attrs,
    defStyleAttr,
) {

    private val binding = NotificationIconViewLayoutBinding.inflate(layoutInflater, this)

    init {
        context.withStyledAttributes(
            attrs, R.styleable.NotificationIconView, defStyleAttr, 0,
        ) {
            if (hasValue(R.styleable.NotificationIconView_itemColor)) {
                itemColor = getColor(R.styleable.NotificationIconView_itemColor, Color.BLACK)
            }

            if (hasValue(R.styleable.NotificationIconView_itemIcon)) {
                itemIcon = getResourceId(R.styleable.NotificationIconView_itemIcon, R.drawable.unknown)
                    .let(RecordTypeIcon::Image)
            }

            if (hasValue(R.styleable.NotificationIconView_itemIsComplete)) {
                itemIsComplete = getBoolean(R.styleable.NotificationIconView_itemIsComplete, false)
            }
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

    var itemCheckStates: List<CheckState> = emptyList()
        set(value) {
            val visibleStates = value.filterNot { it == CheckState.HIDDEN }.take(CHECKMARK_COUNT)
            getCheckmarkViews().forEachIndexed { index, checkmark ->
                checkmark.itemCheckState = visibleStates.getOrElse(index) { CheckState.HIDDEN }
            }
            field = visibleStates
        }

    var itemIsComplete: Boolean = false
        set(value) {
            binding.viewNotificationIconComplete.isVisible = value
            field = value
        }

    private fun getCheckmarkViews() = listOf(
        binding.viewNotificationIconGoalCheckmark,
        binding.viewNotificationIconLimitCheckmark,
    )

    private companion object {
        const val CHECKMARK_COUNT = 2
    }
}