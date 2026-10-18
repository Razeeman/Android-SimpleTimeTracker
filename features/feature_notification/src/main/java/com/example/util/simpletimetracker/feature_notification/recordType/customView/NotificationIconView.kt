package com.example.util.simpletimetracker.feature_notification.recordType.customView

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.widget.RelativeLayout
import androidx.core.view.isVisible
import com.example.util.simpletimetracker.feature_notification.R
import com.example.util.simpletimetracker.feature_notification.databinding.NotificationIconViewLayoutBinding
import com.example.util.simpletimetracker.feature_views.GoalCheckmarkView.CheckState
import com.example.util.simpletimetracker.feature_views.drawable.AngleGradientDrawable
import com.example.util.simpletimetracker.feature_views.extension.layoutInflater
import com.example.util.simpletimetracker.feature_views.extension.toData
import com.example.util.simpletimetracker.feature_views.viewData.ColorStyle
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
    private val solidDrawable = GradientDrawable().apply { shape = GradientDrawable.OVAL }
    private val gradientDrawable by lazy { AngleGradientDrawable() }

    init {
        context.withStyledAttributes(
            attrs, R.styleable.NotificationIconView, defStyleAttr, 0,
        ) {
            if (hasValue(R.styleable.NotificationIconView_itemColor)) {
                itemColor = getColor(R.styleable.NotificationIconView_itemColor, Color.BLACK)
                    .let(ColorStyle::Solid)
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

    var itemColor: ColorStyle = ColorStyle.Solid(0)
        set(value) {
            binding.ivNotificationIconBackground.background = when (value) {
                is ColorStyle.Solid -> solidDrawable.apply {
                    setColor(value.color)
                }
                is ColorStyle.LinearGradient -> gradientDrawable.apply {
                    setGradient(value.toData(), minOf(width, height) / 2f)
                }
            }
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

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        val color = itemColor as? ColorStyle.LinearGradient ?: return
        gradientDrawable.setGradient(
            data = color.toData(),
            cornerRadius = minOf(w, h) / 2f,
        )
    }

    private fun getCheckmarkViews() = listOf(
        binding.viewNotificationIconGoalCheckmark,
        binding.viewNotificationIconLimitCheckmark,
    )

    private companion object {
        const val CHECKMARK_COUNT = 2
    }
}