package com.example.util.simpletimetracker.feature_statistics.adapter

import android.graphics.Color
import android.view.ViewGroup
import androidx.annotation.ColorInt
import androidx.core.view.ViewCompat
import com.example.util.simpletimetracker.core.adapter.BaseRecyclerAdapterDelegate
import com.example.util.simpletimetracker.core.adapter.BaseRecyclerViewHolder
import com.example.util.simpletimetracker.core.adapter.ViewHolderType
import com.example.util.simpletimetracker.core.extension.setOnClick
import com.example.util.simpletimetracker.core.extension.visible
import com.example.util.simpletimetracker.core.view.TransitionNames
import com.example.util.simpletimetracker.feature_statistics.R
import com.example.util.simpletimetracker.feature_statistics.viewData.StatisticsViewData
import kotlinx.android.synthetic.main.item_statistics_layout.view.*

class StatisticsAdapterDelegate(
    private val onItemClick: ((StatisticsViewData, Map<Any, String>) -> Unit)
) : BaseRecyclerAdapterDelegate() {

    override fun onCreateViewHolder(parent: ViewGroup): BaseRecyclerViewHolder =
        StatisticsViewHolder(parent)

    inner class StatisticsViewHolder(parent: ViewGroup) :
        BaseRecyclerViewHolder(parent, R.layout.item_statistics_layout) {

        override fun bind(
            item: ViewHolderType,
            payloads: List<Any>
        ) = with(itemView) {
            item as StatisticsViewData
            val transitionName = TransitionNames.STATISTICS_DETAIL + item.id

            layoutStatisticsItem.setCardBackgroundColor(item.color)
            tvStatisticsItemName.text = item.name
            tvStatisticsItemDuration.text = item.duration
            tvStatisticsItemPercent.text = item.percent
            normalizeLightness(item.color).let(dividerStatisticsPercent::setBackgroundColor)
            if (item is StatisticsViewData.StatisticsActivityViewData) {
                ivStatisticsItemIcon.visible = true
                ivStatisticsItemIcon.setBackgroundResource(item.iconId)
                ivStatisticsItemIcon.tag = item.iconId
            } else {
                ivStatisticsItemIcon.visible = false
            }

            setOnClick { onItemClick(item, mapOf(this to transitionName)) }
            ViewCompat.setTransitionName(this, transitionName)
        }
    }

    /**
     * Lightens dark colors and darkens light colors.
     */
    @ColorInt
    private fun normalizeLightness(@ColorInt color: Int): Int {
        return FloatArray(3).apply {
            Color.colorToHSV(color, this)
            if (this[2] > 0.5f) {
                this[2] -= COLOR_NORMALIZATION
            } else {
                this[2] += COLOR_NORMALIZATION
            }
        }.let(Color::HSVToColor)
    }

    companion object {
        private const val COLOR_NORMALIZATION = 0.05f
    }
}