package com.example.util.simpletimetracker.feature_widget.universal.customView

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.example.util.simpletimetracker.feature_views.ColorUtils
import com.example.util.simpletimetracker.feature_widget.R
import com.example.util.simpletimetracker.feature_widget.databinding.WidgetUniversalViewLayoutBinding

class WidgetUniversalView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : CardView(
    context,
    attrs,
    defStyleAttr,
) {

    private val binding: WidgetUniversalViewLayoutBinding = WidgetUniversalViewLayoutBinding
        .inflate(LayoutInflater.from(context), this)

    init {
        ContextCompat.getColor(context, R.color.widget_universal_background_color).let(::setCardBackgroundColor)
        radius = context.resources.getDimensionPixelOffset(R.dimen.widget_universal_corner_radius).toFloat()
        cardElevation = 0f
        preventCornerOverlap = false
        useCompatPadding = false
    }

    fun setData(viewData: WidgetUniversalViewData) = with(binding) {
        iconsWidgetUniversal.setIconColor(viewData.iconColor)
        iconsWidgetUniversal.setData(viewData.data)
        ColorUtils.changeAlpha(
            color = ContextCompat.getColor(context, R.color.widget_universal_background_color),
            alpha = viewData.backgroundAlpha,
        ).let(::setCardBackgroundColor)
        invalidate()
    }
}