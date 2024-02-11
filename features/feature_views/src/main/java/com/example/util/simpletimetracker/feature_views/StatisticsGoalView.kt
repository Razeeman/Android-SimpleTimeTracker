package com.example.util.simpletimetracker.feature_views

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.example.util.simpletimetracker.feature_views.ColorUtils.normalizeLightness
import com.example.util.simpletimetracker.feature_views.databinding.StatisticsGoalViewLayoutBinding
import com.example.util.simpletimetracker.feature_views.extension.visible
import com.example.util.simpletimetracker.feature_views.viewData.RecordTypeIcon

class StatisticsGoalView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : CardView(
    context,
    attrs,
    defStyleAttr,
) {

    private val binding: StatisticsGoalViewLayoutBinding = StatisticsGoalViewLayoutBinding
        .inflate(LayoutInflater.from(context), this)

    var itemName: String = ""
        set(value) {
            field = value
            binding.tvStatisticsGoalItemName.text = itemName
        }

    var itemColor: Int = Color.BLACK
        set(value) {
            field = value
            setCardBackgroundColor(value)
            setDividerColor()
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
            setGoalPercentVisibility()
        }

    var itemGoalTimeComplete: Boolean = false
        set(value) {
            field = value
            binding.ivStatisticsGoalItemCheck.visible = value
            setGoalPercentVisibility()
        }

    init {
        initProps()
        initAttrs(context, attrs, defStyleAttr)
    }

    private fun initProps() {
        ContextCompat.getColor(context, R.color.black).let(::setCardBackgroundColor)
        radius = resources.getDimensionPixelOffset(R.dimen.record_type_card_corner_radius).toFloat()
        // TODO doesn't work here for some reason, need to set in the layout
        cardElevation = resources.getDimensionPixelOffset(R.dimen.record_type_card_elevation).toFloat()
        preventCornerOverlap = false
        useCompatPadding = true
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

                if (hasValue(R.styleable.StatisticsGoalView_itemGoalTimeComplete)) {
                    itemGoalTimeComplete = getBoolean(R.styleable.StatisticsGoalView_itemGoalTimeComplete, false)
                }

                recycle()
            }
    }

    private fun setDividerColor() {
        normalizeLightness(itemColor)
            .let(binding.dividerStatisticsGoalPercent::setBackgroundColor)
    }

    private fun setGoalPercentVisibility() {
        binding.ivStatisticsGoalItemCheck.visible = itemGoalTimeComplete
        binding.tvStatisticsGoalItemPercent.visibility = when {
            itemGoalTimeComplete -> View.INVISIBLE
            itemGoalPercent.isEmpty() -> View.GONE
            else -> View.VISIBLE
        }
    }
}