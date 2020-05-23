package com.example.util.simpletimetracker.feature_statistics.adapter

import android.graphics.Color
import android.view.ViewGroup
import androidx.annotation.ColorInt
import com.example.util.simpletimetracker.core.adapter.BaseRecyclerAdapterDelegate
import com.example.util.simpletimetracker.core.adapter.BaseRecyclerViewHolder
import com.example.util.simpletimetracker.core.adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_statistics.R
import com.example.util.simpletimetracker.feature_statistics.viewData.StatisticsViewData
import kotlinx.android.synthetic.main.item_statistics_layout.view.*

class StatisticsAdapterDelegate() : BaseRecyclerAdapterDelegate() {

    override fun onCreateViewHolder(parent: ViewGroup): BaseRecyclerViewHolder =
        RunningRecordsViewHolder(parent)

    inner class RunningRecordsViewHolder(parent: ViewGroup) :
        BaseRecyclerViewHolder(parent, R.layout.item_statistics_layout) {

        override fun bind(item: ViewHolderType) = with(itemView) {
            item as StatisticsViewData

            layoutStatisticsItem.setCardBackgroundColor(item.color)
            ivStatisticsItemIcon.setBackgroundResource(item.iconId)
            tvStatisticsItemName.text = item.name
            tvStatisticsItemDuration.text = item.duration
            tvStatisticsItemPercent.text = item.percent
            normalizeLightness(item.color)
                .let(dividerStatisticsPercent::setBackgroundColor)
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