package com.example.util.simpletimetracker.core.view

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.View
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.example.util.simpletimetracker.core.R
import kotlinx.android.synthetic.main.category_view_layout.view.*

class CategoryView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : CardView(
    context,
    attrs,
    defStyleAttr
) {

    init {
        View.inflate(context, R.layout.category_view_layout, this)

        ContextCompat.getColor(context, R.color.black).let(::setCardBackgroundColor)
        radius = resources.getDimensionPixelOffset(R.dimen.record_type_card_corner_radius)
            .toFloat()
        // TODO doesn't work here for some reason, need to set in the layout
        cardElevation = resources.getDimensionPixelOffset(R.dimen.record_type_card_elevation)
            .toFloat()
        preventCornerOverlap = false
        useCompatPadding = true

        context.obtainStyledAttributes(attrs, R.styleable.CategoryView, defStyleAttr, 0)
            .run {
                if (hasValue(R.styleable.CategoryView_itemName)) itemName =
                    getString(R.styleable.CategoryView_itemName).orEmpty()

                if (hasValue(R.styleable.CategoryView_itemColor)) itemColor =
                    getColor(R.styleable.CategoryView_itemColor, Color.BLACK)

                recycle()
            }
    }

    var itemName: String = ""
        set(value) {
            tvCategoryItemName.text = value
            field = value
        }

    var itemColor: Int = 0
        set(value) {
            setCardBackgroundColor(value)
            field = value
        }
}