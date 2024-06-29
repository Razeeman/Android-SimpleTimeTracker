package com.example.util.simpletimetracker.feature_views

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.annotation.ColorInt
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import com.example.util.simpletimetracker.feature_views.databinding.RecordRunningViewLayoutBinding
import com.example.util.simpletimetracker.feature_views.extension.getThemedAttr
import com.example.util.simpletimetracker.feature_views.extension.visible
import com.example.util.simpletimetracker.feature_views.viewData.RecordTypeIcon

class RunningRecordView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : ConstraintLayout(
    context,
    attrs,
    defStyleAttr,
) {

    val binding: RecordRunningViewLayoutBinding = RecordRunningViewLayoutBinding
        .inflate(LayoutInflater.from(context), this)

    var itemName: String = ""
        set(value) {
            field = value
            setItemName()
        }

    var itemTagName: String = ""
        set(value) {
            field = value
            setItemName()
        }

    var itemColor: Int = 0
        set(value) {
            field = value
            binding.cardRecordItemContainer.setCardBackgroundColor(value)
            setStripesColor(value)
            setNowIconColor(value)
            setPomodoroBackground()
        }

    var itemTagColor: Int = Color.WHITE
        set(value) {
            field = value
            setItemName()
        }

    var itemIcon: RecordTypeIcon = RecordTypeIcon.Image(0)
        set(value) {
            binding.ivRunningRecordItemIcon.itemIcon = value
            field = value
        }

    var itemTimeStarted: String = ""
        set(value) {
            binding.tvRunningRecordItemTimeStarted.text = value
            field = value
        }

    var itemTimer: String = ""
        set(value) {
            binding.tvRunningRecordItemTimer.text = value
            field = value
        }

    var itemTimerTotal: String = ""
        set(value) {
            binding.tvRunningRecordItemTimerTotal.text = value
            binding.tvRunningRecordItemTimerTotal.visible = value.isNotEmpty()
            field = value
        }

    var itemGoalTime: String = ""
        set(value) {
            binding.tvRunningRecordItemGoalTime.text = value
            binding.tvRunningRecordItemGoalTime.visible = value.isNotEmpty()
            field = value
        }

    var itemGoalTimeComplete: Boolean = false
        set(value) {
            binding.ivRunningRecordItemGoalTimeCheck.visible = value
            field = value
        }

    var itemComment: String = ""
        set(value) {
            binding.tvRunningRecordItemComment.text = value
            binding.tvRunningRecordItemComment.visible = value.isNotEmpty()
            field = value
        }

    var itemNowIconVisible: Boolean = false
        set(value) {
            binding.tvRunningRecordItemNow.visible = value
            field = value
        }

    var itemPomodoroIconVisible: Boolean = false
        set(value) {
            binding.groupRunningRecordItemPomodoro.visible = value
            field = value
        }

    var itemPomodoroIsRunning: Boolean = false
        set(value) {
            field = value
            setPomodoroBackground()
        }

    init {
        initAttrs(context, attrs, defStyleAttr)
    }

    private fun initAttrs(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int,
    ) {
        context.obtainStyledAttributes(attrs, R.styleable.RunningRecordView, defStyleAttr, 0)
            .run {
                if (hasValue(R.styleable.RunningRecordView_itemName)) {
                    itemName = getString(R.styleable.RunningRecordView_itemName).orEmpty()
                }

                if (hasValue(R.styleable.RunningRecordView_itemColor)) {
                    itemColor = getColor(R.styleable.RunningRecordView_itemColor, Color.BLACK)
                }

                if (hasValue(R.styleable.RunningRecordView_itemTagName)) {
                    itemTagName = getString(R.styleable.RunningRecordView_itemTagName).orEmpty()
                }

                if (hasValue(R.styleable.RunningRecordView_itemTagColor)) {
                    itemTagColor = getColor(R.styleable.RunningRecordView_itemTagColor, Color.WHITE)
                }

                if (hasValue(R.styleable.RunningRecordView_itemIcon)) {
                    itemIcon = getResourceId(R.styleable.RunningRecordView_itemIcon, R.drawable.unknown)
                        .let(RecordTypeIcon::Image)
                }

                if (hasValue(R.styleable.RunningRecordView_itemIconText)) {
                    itemIcon = getString(R.styleable.RunningRecordView_itemIconText).orEmpty()
                        .let(RecordTypeIcon::Text)
                }

                if (hasValue(R.styleable.RunningRecordView_itemTimeStarted)) {
                    itemTimeStarted = getString(R.styleable.RunningRecordView_itemTimeStarted).orEmpty()
                }

                if (hasValue(R.styleable.RunningRecordView_itemTimer)) {
                    itemTimer = getString(R.styleable.RunningRecordView_itemTimer).orEmpty()
                }

                if (hasValue(R.styleable.RunningRecordView_itemTimerDay)) {
                    itemTimerTotal = getString(R.styleable.RunningRecordView_itemTimerDay).orEmpty()
                }

                if (hasValue(R.styleable.RunningRecordView_itemGoalTime)) {
                    itemGoalTime = getString(R.styleable.RunningRecordView_itemGoalTime).orEmpty()
                }

                if (hasValue(R.styleable.RunningRecordView_itemComment)) {
                    itemComment = getString(R.styleable.RunningRecordView_itemComment).orEmpty()
                }

                if (hasValue(R.styleable.RunningRecordView_itemNowIconVisible)) {
                    itemNowIconVisible = getBoolean(R.styleable.RunningRecordView_itemNowIconVisible, false)
                }

                if (hasValue(R.styleable.RunningRecordView_itemPomodoroIconVisible)) {
                    itemPomodoroIconVisible = getBoolean(
                        R.styleable.RunningRecordView_itemPomodoroIconVisible, false,
                    )
                }

                if (hasValue(R.styleable.RunningRecordView_itemPomodoroIsRunning)) {
                    itemPomodoroIsRunning = getBoolean(
                        R.styleable.RunningRecordView_itemPomodoroIsRunning, false,
                    )
                }

                recycle()
            }
    }

    private fun setItemName() = with(binding) {
        if (itemTagName.isEmpty()) {
            tvRunningRecordItemName.text = itemName
        } else {
            val name = "$itemName - $itemTagName"
            val spannable = SpannableString(name)
            spannable.setSpan(
                ForegroundColorSpan(itemTagColor),
                itemName.length, name.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE,
            )
            tvRunningRecordItemName.text = spannable
        }
    }

    private fun setStripesColor(@ColorInt value: Int) {
        ColorUtils.normalizeLightness(value, factor = 0.03f)
            .also(binding.viewRecordItemStripeStart::setBackgroundColor)
            .also(binding.viewRecordItemStripeEnd::setBackgroundColor)
    }

    private fun setNowIconColor(@ColorInt value: Int) {
        ColorUtils.darkenColor(value)
            .let(ColorStateList::valueOf)
            .let { ViewCompat.setBackgroundTintList(binding.tvRunningRecordItemNow, it) }
    }

    private fun setPomodoroBackground() {
        val color = if (itemPomodoroIsRunning) {
            itemColor
        } else {
            context.getThemedAttr(R.attr.appInactiveColor)
        }
        binding.cardRunningRecordItemPomodoro.setCardBackgroundColor(color)
    }
}