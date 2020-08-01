package com.example.util.simpletimetracker.feature_statistics_detail.customView

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import androidx.core.content.res.ResourcesCompat
import com.example.util.simpletimetracker.feature_statistics_detail.R
import com.example.util.simpletimetracker.feature_statistics_detail.adapter.StatisticsDetailCardAdapter
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailCardViewData
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxItemDecoration
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import kotlinx.android.synthetic.main.statistics_detail_card_view.view.rvStatisticsDetailCard

class StatisticsDetailCardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(
    context,
    attrs,
    defStyleAttr
) {

    var items: List<StatisticsDetailCardViewData> = emptyList()
        set(value) {
            typesAdapter.replace(value)
            field = value
        }

    private var itemsCount: Int
    private val typesAdapter: StatisticsDetailCardAdapter by lazy { StatisticsDetailCardAdapter() }

    init {
        View.inflate(context, R.layout.statistics_detail_card_view, this)

        val dividerDrawable = ResourcesCompat.getDrawable(resources, R.drawable.divider_drawable, context.theme)
        val itemDecoration = FlexboxItemDecoration(context).apply {
            setDrawable(dividerDrawable)
        }

        rvStatisticsDetailCard.apply {
            layoutManager = FlexboxLayoutManager(context).apply {
                flexDirection = FlexDirection.ROW
                justifyContent = JustifyContent.CENTER
                flexWrap = FlexWrap.NOWRAP
                addItemDecoration(itemDecoration)
            }
            adapter = typesAdapter
        }

        context.obtainStyledAttributes(
            attrs,
            R.styleable.StatisticsDetailCardView,
            defStyleAttr,
            0
        ).run {
            itemsCount = getInt(R.styleable.StatisticsDetailCardView_itemCount, DEFAULT_ITEM_COUNT)
            recycle()
        }

        if (isInEditMode) {
            (1..itemsCount)
                .map { StatisticsDetailCardViewData("$DEFAULT_TITLE$it", "$DEFAULT_SUBTITLE$it") }
                .let { items = it }
        }
    }

    companion object {
        private const val DEFAULT_ITEM_COUNT = 2
        private const val DEFAULT_TITLE = "Title"
        private const val DEFAULT_SUBTITLE = "Subtitle"
    }
}