package com.example.util.simpletimetracker.core.view

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.View
import androidx.annotation.ColorInt
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.example.util.simpletimetracker.core.R
import com.example.util.simpletimetracker.core.extension.visible
import com.example.util.simpletimetracker.core.viewData.RecordTypeIcon
import kotlinx.android.synthetic.main.statistics_view_layout.view.*

class StatisticsView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : CardView(
    context,
    attrs,
    defStyleAttr
) {

    var itemName: String = ""
        set(value) {
            field = value
            tvStatisticsItemName.text = itemName
        }

    var itemColor: Int = Color.BLACK
        set(value) {
            setCardBackgroundColor(value)
            setDividerColor()
            field = value
        }

    var itemIcon: RecordTypeIcon = RecordTypeIcon.Image(0)
        set(value) {
            ivStatisticsItemIcon.itemIcon = value
            field = value
        }

    var itemIconVisible: Boolean = false
        set(value) {
            ivStatisticsItemIcon.visible = value
            field = value
        }

    var itemDuration: String = ""
        set(value) {
            tvStatisticsItemDuration.text = value
            field = value
        }

    var itemPercent: String = ""
        set(value) {
            tvStatisticsItemPercent.text = value
            setDividerColor()
            field = value
        }

    init {
        View.inflate(context, R.layout.statistics_view_layout, this)
        initProps()
        initAttrs(context, attrs, defStyleAttr)
    }

    private fun initProps() {
        ContextCompat.getColor(context, R.color.black).let(::setCardBackgroundColor)
        radius = resources.getDimensionPixelOffset(R.dimen.record_type_card_corner_radius).toFloat()
        // TODO doesn't work here for some reason, need to set in the layout
        cardElevation = resources.getDimensionPixelOffset(R.dimen.record_type_card_elevation).toFloat()
        preventCornerOverlap = false
        useCompatPadding = true
    }

    private fun initAttrs(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int
    ) {
        context.obtainStyledAttributes(attrs, R.styleable.StatisticsView, defStyleAttr, 0)
            .run {
                if (hasValue(R.styleable.StatisticsView_itemName)) itemName =
                    getString(R.styleable.StatisticsView_itemName).orEmpty()

                if (hasValue(R.styleable.StatisticsView_itemColor)) itemColor =
                    getColor(R.styleable.StatisticsView_itemColor, Color.BLACK)

                if (hasValue(R.styleable.StatisticsView_itemIcon)) itemIcon =
                    getResourceId(R.styleable.StatisticsView_itemIcon, R.drawable.unknown)
                        .let(RecordTypeIcon::Image)

                if (hasValue(R.styleable.StatisticsView_itemEmoji)) itemIcon =
                    getString(R.styleable.StatisticsView_itemEmoji).orEmpty()
                        .let(RecordTypeIcon::Emoji)

                if (hasValue(R.styleable.StatisticsView_itemIconVisible)) itemIconVisible =
                    getBoolean(R.styleable.StatisticsView_itemIconVisible, false)

                if (hasValue(R.styleable.StatisticsView_itemDuration)) itemDuration =
                    getString(R.styleable.StatisticsView_itemDuration).orEmpty()

                if (hasValue(R.styleable.StatisticsView_itemPercent)) itemPercent =
                    getString(R.styleable.StatisticsView_itemPercent).orEmpty()

                recycle()
            }
    }

    private fun setDividerColor() {
        if (itemPercent.isNotEmpty()) {
            normalizeLightness(itemColor)
        } else {
            itemColor
        }.let(dividerStatisticsPercent::setBackgroundColor)
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
}