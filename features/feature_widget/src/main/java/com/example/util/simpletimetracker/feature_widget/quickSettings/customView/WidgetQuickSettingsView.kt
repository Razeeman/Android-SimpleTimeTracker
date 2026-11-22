package com.example.util.simpletimetracker.feature_widget.quickSettings.customView

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.util.AttributeSet
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.example.util.simpletimetracker.feature_views.extension.layoutInflater
import com.example.util.simpletimetracker.feature_widget.R
import com.example.util.simpletimetracker.feature_widget.databinding.WidgetQuickSettingsViewLayoutBinding
import androidx.core.content.withStyledAttributes

class WidgetQuickSettingsView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : CardView(
    context,
    attrs,
    defStyleAttr,
) {

    private val binding = WidgetQuickSettingsViewLayoutBinding.inflate(layoutInflater, this)

    init {
        ContextCompat.getColor(context, R.color.widget_universal_background_color).let(::setCardBackgroundColor)
        radius = resources.getDimensionPixelOffset(R.dimen.widget_universal_corner_radius).toFloat()
        cardElevation = 0f
        preventCornerOverlap = false
        useCompatPadding = false

        context
            .withStyledAttributes(attrs, R.styleable.WidgetQuickSettingsView, defStyleAttr, 0) {
                if (hasValue(R.styleable.WidgetQuickSettingsView_itemName)) {
                    itemName = getString(R.styleable.WidgetQuickSettingsView_itemName).orEmpty()
                }

                if (hasValue(R.styleable.WidgetQuickSettingsView_itemColor)) {
                    itemColor = getColor(R.styleable.WidgetQuickSettingsView_itemColor, Color.BLACK)
                }

                if (hasValue(R.styleable.WidgetQuickSettingsView_itemTextColor)) {
                    itemTextColor = getColor(R.styleable.WidgetQuickSettingsView_itemTextColor, Color.WHITE)
                }
            }
    }

    var itemName: String = ""
        set(value) {
            binding.tvWidgetQuickSettingsName.text = value
            field = value
        }

    var itemColor: Int = 0
        set(value) {
            setCardBackgroundColor(value)
            field = value
        }

    var itemTextColor: Int = 0
        set(value) {
            binding.tvWidgetQuickSettingsName.setTextColor(value)
            binding.ivWidgetQuickSettingsIcon.imageTintList = ColorStateList.valueOf(value)
            field = value
        }
}