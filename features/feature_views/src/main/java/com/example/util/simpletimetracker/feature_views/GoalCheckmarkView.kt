package com.example.util.simpletimetracker.feature_views

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.core.view.isVisible
import com.example.util.simpletimetracker.feature_views.databinding.GoalCheckmarkViewLayoutBinding
import com.example.util.simpletimetracker.feature_views.extension.getThemedAttr
import com.example.util.simpletimetracker.feature_views.extension.layoutInflater
import androidx.core.content.withStyledAttributes

class GoalCheckmarkView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : FrameLayout(
    context,
    attrs,
    defStyleAttr,
) {

    private val binding = GoalCheckmarkViewLayoutBinding.inflate(layoutInflater, this)

    init {
        context
            .withStyledAttributes(
                attrs, R.styleable.GoalCheckmarkView, defStyleAttr, 0,
            ) {
                if (hasValue(R.styleable.GoalCheckmarkView_itemCheckState)) {
                    itemCheckState = getInt(
                        R.styleable.GoalCheckmarkView_itemCheckState,
                        CheckState.HIDDEN.value,
                    ).let(CheckState.Companion::fromValue)
                }

                if (hasValue(R.styleable.GoalCheckmarkView_itemIsFiltered)) {
                    itemIsFiltered = getBoolean(R.styleable.GoalCheckmarkView_itemIsFiltered, false)
                }
            }
    }

    var itemCheckState: CheckState = CheckState.HIDDEN
        set(value) {
            field = value
            setCheckmark()
        }

    var itemIsFiltered: Boolean = false
        set(value) {
            field = value
            setCheckmark()
        }

    private fun setCheckmark() = with(binding) {
        when (itemCheckState) {
            CheckState.HIDDEN -> {
                root.isVisible = false
                ivGoalCheckmarkItemCheckOutline.isVisible = false
                ivGoalCheckmarkItemCheckBorder.isVisible = false
                ivGoalCheckmarkItemCheck.isVisible = false
                ivGoalCheckmarkItemCheckFiltered.isVisible = false
            }
            CheckState.GOAL_NOT_REACHED -> {
                root.isVisible = true
                ivGoalCheckmarkItemCheckOutline.isVisible = true
                val color = ColorStateList.valueOf(context.getThemedAttr(R.attr.colorSecondary))
                ivGoalCheckmarkItemCheckOutline.imageTintList = color
                ivGoalCheckmarkItemCheckBorder.isVisible = true
                ivGoalCheckmarkItemCheck.isVisible = false
            }
            CheckState.GOAL_REACHED, CheckState.LIMIT_NOT_REACHED -> {
                root.isVisible = true
                ivGoalCheckmarkItemCheckOutline.isVisible = true
                val color = ColorStateList.valueOf(context.getThemedAttr(R.attr.appIconColor))
                ivGoalCheckmarkItemCheckOutline.imageTintList = color
                ivGoalCheckmarkItemCheckBorder.isVisible = false
                ivGoalCheckmarkItemCheck.isVisible = true
                ivGoalCheckmarkItemCheck.setImageResource(R.drawable.record_type_check_mark)
            }
            CheckState.LIMIT_REACHED -> {
                root.isVisible = true
                ivGoalCheckmarkItemCheckOutline.isVisible = true
                val color = ColorStateList.valueOf(context.getThemedAttr(R.attr.colorSecondary))
                ivGoalCheckmarkItemCheckOutline.imageTintList = color
                ivGoalCheckmarkItemCheckBorder.isVisible = false
                ivGoalCheckmarkItemCheck.isVisible = true
                ivGoalCheckmarkItemCheck.setImageResource(R.drawable.record_type_check_cross)
            }
        }

        if (itemCheckState != CheckState.HIDDEN) {
            ivGoalCheckmarkItemCheckFiltered.isVisible = itemIsFiltered
        }
    }

    enum class CheckState(val value: Int) {
        HIDDEN(value = 0),
        GOAL_NOT_REACHED(value = 1),
        GOAL_REACHED(value = 2),
        LIMIT_NOT_REACHED(value = 3),
        LIMIT_REACHED(value = 4),
        ;

        companion object {
            fun fromValue(value: Int): CheckState {
                return CheckState.entries.firstOrNull {
                    it.value == value
                } ?: HIDDEN
            }
        }
    }
}