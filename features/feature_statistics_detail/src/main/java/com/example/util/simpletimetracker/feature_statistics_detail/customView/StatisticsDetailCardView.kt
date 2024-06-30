package com.example.util.simpletimetracker.feature_statistics_detail.customView

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.updatePadding
import com.example.util.simpletimetracker.domain.model.Coordinates
import com.example.util.simpletimetracker.feature_base_adapter.BaseRecyclerAdapter
import com.example.util.simpletimetracker.feature_views.extension.dpToPx
import com.example.util.simpletimetracker.feature_statistics_detail.R
import com.example.util.simpletimetracker.feature_statistics_detail.adapter.createStatisticsDetailCardInternalAdapterDelegate
import com.example.util.simpletimetracker.feature_statistics_detail.databinding.StatisticsDetailCardViewBinding
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailCardInternalViewData
import com.example.util.simpletimetracker.feature_views.extension.visible
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxItemDecoration
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent

class StatisticsDetailCardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : FrameLayout(
    context,
    attrs,
    defStyleAttr,
) {

    var listener: (StatisticsDetailCardInternalViewData.ClickableType, Coordinates) -> Unit = { _, _ -> }
    var itemsDescription: String = ""
        set(value) {
            binding.tvStatisticsDetailCardDescription.text = value
            binding.tvStatisticsDetailCardDescription.visible = value.isNotEmpty()
            field = value
        }
    var items: List<StatisticsDetailCardInternalViewData> = emptyList()
        set(value) {
            typesAdapter.replace(value)
            field = value
        }

    private val binding: StatisticsDetailCardViewBinding = StatisticsDetailCardViewBinding
        .inflate(LayoutInflater.from(context), this, true)

    private var itemsCount: Int
    private val typesAdapter: BaseRecyclerAdapter by lazy {
        BaseRecyclerAdapter(
            createStatisticsDetailCardInternalAdapterDelegate(
                onItemClick = ::onItemClick,
            ),
        )
    }

    init {
        updatePadding(bottom = 2.dpToPx())
        clipToPadding = false

        var dividerDrawable = ResourcesCompat.getDrawable(resources, R.drawable.divider_drawable, context.theme)
        runCatching {
            context.theme.obtainStyledAttributes(intArrayOf(R.attr.appDivider)).run {
                dividerDrawable = getDrawable(0)
                recycle()
            }
        }
        val itemDecoration = FlexboxItemDecoration(context).apply {
            setDrawable(dividerDrawable)
        }

        context.obtainStyledAttributes(
            attrs,
            R.styleable.StatisticsDetailCardView,
            defStyleAttr,
            0,
        ).run {
            itemsCount = getInt(
                R.styleable.StatisticsDetailCardView_itemCount, DEFAULT_ITEM_COUNT,
            )
            itemsDescription = getString(
                R.styleable.StatisticsDetailCardView_itemDescription,
            ).orEmpty()
            recycle()
        }

        binding.rvStatisticsDetailCard.apply {
            layoutManager = FlexboxLayoutManager(context).apply {
                flexDirection = FlexDirection.ROW
                justifyContent = JustifyContent.CENTER
                flexWrap = FlexWrap.NOWRAP
                addItemDecoration(itemDecoration)
            }
            adapter = typesAdapter
        }

        if (isInEditMode) {
            (1..itemsCount)
                .map {
                    StatisticsDetailCardInternalViewData(
                        value = "$DEFAULT_TITLE$it",
                        valueChange = StatisticsDetailCardInternalViewData.ValueChange.None,
                        secondValue = "",
                        description = "$DEFAULT_SUBTITLE$it",
                    )
                }
                .let { items = it }
        }
    }

    private fun onItemClick(
        type: StatisticsDetailCardInternalViewData.ClickableType,
        coordinates: Coordinates,
    ) {
        listener(type, coordinates)
    }

    companion object {
        private const val DEFAULT_ITEM_COUNT = 2
        private const val DEFAULT_TITLE = "Title"
        private const val DEFAULT_SUBTITLE = "Subtitle"
    }
}