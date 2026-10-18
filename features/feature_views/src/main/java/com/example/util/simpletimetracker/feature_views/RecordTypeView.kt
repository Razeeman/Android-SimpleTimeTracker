package com.example.util.simpletimetracker.feature_views

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.Gravity
import android.widget.RelativeLayout
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.isVisible
import com.example.util.simpletimetracker.feature_views.GoalCheckmarkView.CheckState
import com.example.util.simpletimetracker.feature_views.databinding.RecordTypeViewLayoutBinding
import com.example.util.simpletimetracker.feature_views.drawable.AngleGradientDrawable
import com.example.util.simpletimetracker.feature_views.extension.animateAlpha
import com.example.util.simpletimetracker.feature_views.extension.layoutInflater
import com.example.util.simpletimetracker.feature_views.extension.setMargins
import com.example.util.simpletimetracker.feature_views.viewData.RecordTypeIcon
import androidx.core.content.withStyledAttributes
import com.example.util.simpletimetracker.feature_views.extension.toData
import com.example.util.simpletimetracker.feature_views.viewData.ColorStyle

class RecordTypeView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : RelativeLayout(
    context,
    attrs,
    defStyleAttr,
) {

    private val binding = RecordTypeViewLayoutBinding.inflate(layoutInflater, this)
    private val gradientDrawable by lazy { AngleGradientDrawable() }

    init {
        context
            .withStyledAttributes(
                set = attrs,
                attrs = R.styleable.RecordTypeView,
                defStyleAttr = defStyleAttr,
                defStyleRes = 0,
            ) {
                if (hasValue(R.styleable.RecordTypeView_itemName)) {
                    itemName = getString(R.styleable.RecordTypeView_itemName).orEmpty()
                }

                if (hasValue(R.styleable.RecordTypeView_itemColor)) {
                    val color = getColor(R.styleable.RecordTypeView_itemColor, Color.BLACK)
                    val end = getColor(R.styleable.RecordTypeView_itemGradientEndColor, color)
                    val isGradient = hasValue(R.styleable.RecordTypeView_itemGradientEndColor)
                    itemColor = if (isGradient) {
                        ColorStyle.LinearGradient(start = color, end = end, angleDegrees = 45)
                    } else {
                        ColorStyle.Solid(color)
                    }
                }

                if (hasValue(R.styleable.RecordTypeView_itemIcon)) {
                    val data = getResourceId(R.styleable.RecordTypeView_itemIcon, R.drawable.unknown)
                    itemIcon = RecordTypeIcon.Image(data)
                }

                if (hasValue(R.styleable.RecordTypeView_itemIconText)) {
                    val data = getString(R.styleable.RecordTypeView_itemIconText).orEmpty()
                    itemIcon = RecordTypeIcon.Text(data)
                }

                if (hasValue(R.styleable.RecordTypeView_itemIconColor)) {
                    itemIconColor = getColor(R.styleable.RecordTypeView_itemIconColor, Color.WHITE)
                }

                if (hasValue(R.styleable.RecordTypeView_itemIconAlpha)) {
                    itemIconAlpha = getFloat(R.styleable.RecordTypeView_itemIconAlpha, 1.0f)
                }

                if (hasValue(R.styleable.RecordTypeView_itemIsRow)) {
                    itemIsRow = getBoolean(R.styleable.RecordTypeView_itemIsRow, false)
                }

                if (hasValue(R.styleable.RecordTypeView_itemIsComplete)) {
                    itemIsComplete = getBoolean(R.styleable.RecordTypeView_itemIsComplete, false)
                }

                if (hasValue(R.styleable.RecordTypeView_itemCompleteIsAnimated)) {
                    itemCompleteIsAnimated = getBoolean(R.styleable.RecordTypeView_itemCompleteIsAnimated, false)
                }
            }
    }

    var itemName: String = ""
        set(value) {
            binding.container.tvRecordTypeItemName.text = value
            field = value
        }

    var itemColor: ColorStyle = ColorStyle.Solid(0)
        set(value) {
            field = value
            when (value) {
                is ColorStyle.Solid -> {
                    binding.container.constraintRecordTypeItem.background = null
                    binding.containerRecordTypeItem.setCardBackgroundColor(value.color)
                }
                is ColorStyle.LinearGradient -> {
                    gradientDrawable.setGradient(
                        data = value.toData(),
                        // TODO GRADIENT only used for the grid widget?
                        cornerRadius = binding.containerRecordTypeItem.radius,
                    )
                    binding.container.constraintRecordTypeItem.background = gradientDrawable
                    binding.containerRecordTypeItem.setCardBackgroundColor(Color.TRANSPARENT)
                }
            }
        }

    var itemIcon: RecordTypeIcon = RecordTypeIcon.Image(0)
        set(value) {
            binding.container.ivRecordTypeItemIcon.itemIcon = value
            field = value
        }

    var itemIconColor: Int = 0
        set(value) {
            binding.container.tvRecordTypeItemName.setTextColor(value)
            binding.container.ivRecordTypeItemIcon.itemIconColor = value
            field = value
        }

    var itemIconAlpha: Float = 1.0f
        set(value) {
            binding.container.ivRecordTypeItemIcon.itemIconAlpha = value
            field = value
        }

    var itemIsRow: Boolean = false
        set(value) {
            changeConstraints(value)
            field = value
        }

    var itemCheckStates: List<CheckState> = emptyList()
        set(value) {
            val visibleStates = value.filterNot { it == CheckState.HIDDEN }.take(CHECKMARK_COUNT)
            getCheckmarkOutlines().forEachIndexed { index, checkmark ->
                checkmark.itemCheckState = visibleStates.getOrElse(index) { CheckState.HIDDEN }
            }
            field = visibleStates
        }

    var itemCompleteIsAnimated: Boolean = false

    var itemIsComplete: Boolean = false
        set(value) {
            field = value
            setCompleteVisibility()
        }

    fun getContainer(): CardView {
        return binding.containerRecordTypeItem
    }

    fun getCheckmarkOutlines(): List<GoalCheckmarkView> {
        return listOf(
            binding.viewRecordTypeItemGoalCheckmark,
            binding.viewRecordTypeItemLimitCheckmark,
        )
    }

    private fun changeConstraints(isRow: Boolean) = with(binding.container) {
        if (isRow) {
            val setRow = ConstraintSet()
                .apply { clone(context, R.layout.record_type_view_horizontal) }
            setRow.applyTo(constraintRecordTypeItem)

            ivRecordTypeItemIcon.setMargins(start = 6)
            tvRecordTypeItemName.gravity = Gravity.START or Gravity.CENTER_VERTICAL
            tvRecordTypeItemName.setMargins(top = 0, start = 8)
        } else {
            val setRow = ConstraintSet()
                .apply { clone(context, R.layout.record_type_view_vertical) }
            setRow.applyTo(constraintRecordTypeItem)

            ivRecordTypeItemIcon.setMargins(start = 0)
            tvRecordTypeItemName.gravity = Gravity.CENTER
            tvRecordTypeItemName.setMargins(top = 4, start = 0)
        }
    }

    private fun setCompleteVisibility() {
        if (itemCompleteIsAnimated) {
            binding.viewRecordTypeItemComplete.isVisible = true
            binding.viewRecordTypeItemComplete.animateAlpha(itemIsComplete, 200)
        } else {
            binding.viewRecordTypeItemComplete.alpha = 1f
            binding.viewRecordTypeItemComplete.isVisible = itemIsComplete
        }
    }

    private companion object {
        const val CHECKMARK_COUNT = 2
    }
}
