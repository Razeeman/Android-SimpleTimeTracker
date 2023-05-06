package com.example.util.simpletimetracker.feature_widget.statistics.customView

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.example.util.simpletimetracker.feature_views.extension.visible
import com.example.util.simpletimetracker.feature_views.pieChart.PiePortion
import com.example.util.simpletimetracker.feature_widget.R
import com.example.util.simpletimetracker.feature_widget.databinding.WidgetStatisticsChartViewLayoutBinding

class WidgetStatisticsChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : CardView(
    context,
    attrs,
    defStyleAttr
) {

    private val binding = WidgetStatisticsChartViewLayoutBinding
        .inflate(LayoutInflater.from(context), this)

    init {
        ContextCompat.getColor(context, R.color.widget_universal_background_color).let(::setCardBackgroundColor)
        radius = context.resources.getDimensionPixelOffset(R.dimen.widget_universal_corner_radius).toFloat()
        cardElevation = 0f
        preventCornerOverlap = false
        useCompatPadding = false
    }

    fun setSegments(data: List<PiePortion>, total: String) = with(binding) {
        if (data.isEmpty()) {
            switchDataVisibility(hasData = false)
        } else {
            switchDataVisibility(hasData = true)
            chartWidgetStatistics.setSegments(
                data = data,
                animateOpen = false,
            )
            tvWidgetStatisticsTotal.text = total
        }
        invalidate()
    }

    private fun switchDataVisibility(hasData: Boolean) = with(binding) {
        groupWidgetStatisticsDataState.visible = hasData
        groupWidgetStatisticsEmptyState.visible = !hasData
    }
}