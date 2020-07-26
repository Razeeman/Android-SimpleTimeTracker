package com.example.util.simpletimetracker.feature_statistics_detail.customView

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import com.example.util.simpletimetracker.feature_statistics_detail.R
import kotlinx.android.synthetic.main.statistics_detail_card_view.view.*

class StatisticsDetailCardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(
    context,
    attrs,
    defStyleAttr
) {

    init {
        View.inflate(context, R.layout.statistics_detail_card_view, this)

        context.obtainStyledAttributes(
            attrs,
            R.styleable.StatisticsDetailCardView,
            defStyleAttr,
            0
        ).run {
            itemValue =
                getString(R.styleable.StatisticsDetailCardView_itemValue).orEmpty()
            itemDescription =
                getString(R.styleable.StatisticsDetailCardView_itemDescription).orEmpty()
            recycle()
        }
    }

    var itemValue: String = ""
        set(value) {
            tvStatisticsDetailCardValue.text = value
            field = value
        }

    var itemDescription: String = ""
        set(value) {
            tvStatisticsDetailCardDescription.text = value
            field = value
        }
}