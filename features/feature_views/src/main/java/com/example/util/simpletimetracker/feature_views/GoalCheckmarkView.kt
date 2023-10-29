package com.example.util.simpletimetracker.feature_views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.core.view.isVisible
import com.example.util.simpletimetracker.feature_views.databinding.GoalCheckmarkViewLayoutBinding

class GoalCheckmarkView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : FrameLayout(
    context,
    attrs,
    defStyleAttr,
) {

    private val binding: GoalCheckmarkViewLayoutBinding = GoalCheckmarkViewLayoutBinding
        .inflate(LayoutInflater.from(context), this)

    init {

        context.obtainStyledAttributes(attrs, R.styleable.GoalCheckmarkView, defStyleAttr, 0)
            .run {
                if (hasValue(R.styleable.GoalCheckmarkView_itemWithCheck)) {
                    itemWithCheck = getBoolean(R.styleable.GoalCheckmarkView_itemWithCheck, false)
                }

                if (hasValue(R.styleable.GoalCheckmarkView_itemIsChecked)) {
                    itemIsChecked = getBoolean(R.styleable.GoalCheckmarkView_itemIsChecked, false)
                }

                recycle()
            }
    }

    var itemWithCheck: Boolean = false
        set(value) {
            field = value
            setCheckmark()
        }

    var itemIsChecked: Boolean = false
        set(value) {
            field = value
            setCheckmark()
        }

    private fun setCheckmark() = with(binding) {
        if (itemWithCheck) {
            ivGoalCheckmarkItemCheckOutline.isVisible = true
            ivGoalCheckmarkItemCheck.isVisible = itemIsChecked
        } else {
            ivGoalCheckmarkItemCheckOutline.isVisible = false
            ivGoalCheckmarkItemCheck.isVisible = false
        }
    }
}