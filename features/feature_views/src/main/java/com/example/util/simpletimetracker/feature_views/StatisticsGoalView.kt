package com.example.util.simpletimetracker.feature_views

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.widget.RelativeLayout
import com.example.util.simpletimetracker.feature_views.ColorUtils.normalizeLightness
import com.example.util.simpletimetracker.feature_views.GoalCheckmarkView.CheckState
import com.example.util.simpletimetracker.feature_views.databinding.StatisticsGoalViewLayoutBinding
import com.example.util.simpletimetracker.feature_views.extension.layoutInflater
import com.example.util.simpletimetracker.feature_views.extension.setTextOptional
import com.example.util.simpletimetracker.feature_views.extension.visible
import com.example.util.simpletimetracker.feature_views.viewData.RecordTypeIcon

class StatisticsGoalView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : RelativeLayout(
    context,
    attrs,
    defStyleAttr,
) {

    private val binding = StatisticsGoalViewLayoutBinding.inflate(layoutInflater, this)

    var itemName: String = ""
        set(value) {
            field = value
            binding.tvStatisticsGoalItemName.text = itemName
        }

    var itemColor: Int = Color.BLACK
        set(value) {
            field = value
            binding.containerStatisticsGoalItem.setCardBackgroundColor(value)
            setDividerColor()
        }

    var itemEstimate: String? = null
        set(value) {
            field = value
            binding.tvStatisticsGoalItemEstimate.setTextOptional(value)
        }

    var itemIcon: RecordTypeIcon = RecordTypeIcon.Image(0)
        set(value) {
            binding.ivStatisticsGoalItemIcon.itemIcon = value
            field = value
        }

    var itemIconVisible: Boolean = false
        set(value) {
            binding.ivStatisticsGoalItemIcon.visible = value
            field = value
        }

    var itemGoalCurrent: String = ""
        set(value) {
            binding.tvStatisticsGoalItemCurrent.text = value
            field = value
        }

    var itemGoal: String = ""
        set(value) {
            binding.tvStatisticsGoalItemGoal.text = value
            field = value
        }

    var itemGoalPercent: String = ""
        set(value) {
            field = value
            binding.tvStatisticsGoalItemPercent.text = value
        }

    var itemGoalState: CheckState = CheckState.HIDDEN
        set(value) {
            field = value
            binding.ivStatisticsGoalItemCheck.itemCheckState = value
        }

    init {
        initAttrs(context, attrs, defStyleAttr)
    }

    private fun initAttrs(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int,
    ) {
        context.obtainStyledAttributes(attrs, R.styleable.StatisticsGoalView, defStyleAttr, 0)
            .run {
                if (hasValue(R.styleable.StatisticsGoalView_itemName)) {
                    itemName = getString(R.styleable.StatisticsGoalView_itemName).orEmpty()
                }

                if (hasValue(R.styleable.StatisticsGoalView_itemColor)) {
                    itemColor = getColor(R.styleable.StatisticsGoalView_itemColor, Color.BLACK)
                }

                if (hasValue(R.styleable.StatisticsGoalView_itemIcon)) {
                    itemIcon = getResourceId(R.styleable.StatisticsGoalView_itemIcon, R.drawable.unknown)
                        .let(RecordTypeIcon::Image)
                }

                if (hasValue(R.styleable.StatisticsGoalView_itemIconText)) {
                    itemIcon = getString(R.styleable.StatisticsGoalView_itemIconText).orEmpty()
                        .let(RecordTypeIcon::Text)
                }

                if (hasValue(R.styleable.StatisticsGoalView_itemIconVisible)) {
                    itemIconVisible = getBoolean(R.styleable.StatisticsGoalView_itemIconVisible, false)
                }

                if (hasValue(R.styleable.StatisticsGoalView_itemGoalCurrent)) {
                    itemGoalCurrent = getString(R.styleable.StatisticsGoalView_itemGoalCurrent).orEmpty()
                }

                if (hasValue(R.styleable.StatisticsGoalView_itemGoal)) {
                    itemGoal = getString(R.styleable.StatisticsGoalView_itemGoal).orEmpty()
                }

                if (hasValue(R.styleable.StatisticsGoalView_itemGoalPercent)) {
                    itemGoalPercent = getString(R.styleable.StatisticsGoalView_itemGoalPercent).orEmpty()
                }

                if (hasValue(R.styleable.StatisticsGoalView_itemCheckState)) {
                    itemGoalState = getInt(
                        R.styleable.StatisticsGoalView_itemCheckState,
                        CheckState.HIDDEN.value,
                    ).let(CheckState.Companion::fromValue)
                }

                recycle()
            }
    }

    private fun setDividerColor() {
        normalizeLightness(itemColor)
            .let(binding.dividerStatisticsGoalPercent::setBackgroundColor)
    }
}