package com.example.util.simpletimetracker.feature_widget.universal.customView

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.example.util.simpletimetracker.feature_widget.R
import kotlinx.android.synthetic.main.widget_universal_view_layout.view.iconsWidgetUniversal

class WidgetUniversalView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : CardView(
    context,
    attrs,
    defStyleAttr
) {

    init {
        View.inflate(context, R.layout.widget_universal_view_layout, this)

        ContextCompat.getColor(context, R.color.widget_universal_background_color).let(::setCardBackgroundColor)
        radius = context.resources.getDimensionPixelOffset(R.dimen.widget_universal_corner_radius).toFloat()
        cardElevation = 0f
        preventCornerOverlap = false
        useCompatPadding = false
    }

    fun setData(viewData: WidgetUniversalViewData) {
        iconsWidgetUniversal.setIconColor(viewData.iconColor)
        iconsWidgetUniversal.setData(viewData.data)
        invalidate()
    }
}