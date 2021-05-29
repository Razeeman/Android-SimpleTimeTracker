package com.example.util.simpletimetracker.core.adapter.statistics

import android.graphics.Color
import androidx.annotation.ColorInt
import androidx.core.view.ViewCompat
import com.example.util.simpletimetracker.core.R
import com.example.util.simpletimetracker.core.adapter.createRecyclerAdapterDelegate
import com.example.util.simpletimetracker.core.extension.setOnClick
import com.example.util.simpletimetracker.core.extension.visible
import com.example.util.simpletimetracker.core.view.TransitionNames
import kotlinx.android.synthetic.main.item_statistics_layout.view.dividerStatisticsPercent
import kotlinx.android.synthetic.main.item_statistics_layout.view.ivStatisticsItemIcon
import kotlinx.android.synthetic.main.item_statistics_layout.view.layoutStatisticsItem
import kotlinx.android.synthetic.main.item_statistics_layout.view.tvStatisticsItemDuration
import kotlinx.android.synthetic.main.item_statistics_layout.view.tvStatisticsItemName
import kotlinx.android.synthetic.main.item_statistics_layout.view.tvStatisticsItemPercent

fun createStatisticsAdapterDelegate(
    addTransitionNames: Boolean = false,
    onItemClick: ((StatisticsViewData, Map<Any, String>) -> Unit)
) = createRecyclerAdapterDelegate<StatisticsViewData>(
    R.layout.item_statistics_layout
) { itemView, item, _ ->

    with(itemView) {
        item as StatisticsViewData
        val transitionName = TransitionNames.STATISTICS_DETAIL + item.id

        layoutStatisticsItem.setCardBackgroundColor(item.color)
        tvStatisticsItemName.text = item.name
        tvStatisticsItemDuration.text = item.duration
        tvStatisticsItemPercent.text = item.percent
        normalizeLightness(item.color).let(dividerStatisticsPercent::setBackgroundColor)
        if (item is StatisticsViewData.Activity && item.iconId != null) {
            ivStatisticsItemIcon.visible = true
            ivStatisticsItemIcon.itemIcon = item.iconId
        } else {
            ivStatisticsItemIcon.visible = false
        }

        setOnClick { onItemClick(item, mapOf(this to transitionName)) }
        if (addTransitionNames) {
            ViewCompat.setTransitionName(this, transitionName)
        }
    }
}

/**
 * Lightens dark colors and darkens light colors.
 */
@ColorInt
private fun normalizeLightness(@ColorInt color: Int): Int {
    val colorNormalization = 0.05f
    return FloatArray(3).apply {
        Color.colorToHSV(color, this)
        // change value
        if (this[2] > 0.5f) {
            this[2] -= colorNormalization
        } else {
            this[2] += colorNormalization
        }
    }.let(Color::HSVToColor)
}
