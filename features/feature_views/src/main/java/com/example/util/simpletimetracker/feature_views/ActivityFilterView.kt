package com.example.util.simpletimetracker.feature_views

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.example.util.simpletimetracker.feature_views.databinding.ActivityFilterViewLayoutBinding
import com.example.util.simpletimetracker.feature_views.extension.dpToPx

class ActivityFilterView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : CardView(
    context,
    attrs,
    defStyleAttr
) {

    private val binding: ActivityFilterViewLayoutBinding = ActivityFilterViewLayoutBinding
        .inflate(LayoutInflater.from(context), this)

    init {
        ContextCompat.getColor(context, R.color.black).let(::setCardBackgroundColor)
        radius = 100.dpToPx().toFloat()
        // TODO doesn't work here for some reason, need to set in the layout
        cardElevation = resources.getDimensionPixelOffset(R.dimen.record_type_card_elevation)
            .toFloat()
        preventCornerOverlap = false
        useCompatPadding = true

        context.obtainStyledAttributes(attrs, R.styleable.ActivityFilterView, defStyleAttr, 0)
            .run {
                if (hasValue(R.styleable.ActivityFilterView_itemName)) itemName =
                    getString(R.styleable.ActivityFilterView_itemName).orEmpty()

                if (hasValue(R.styleable.ActivityFilterView_itemColor)) itemColor =
                    getColor(R.styleable.ActivityFilterView_itemColor, Color.BLACK)

                recycle()
            }
    }

    var itemName: String = ""
        set(value) {
            binding.tvActivityFilterItemName.text = value
            field = value
        }

    var itemColor: Int = 0
        set(value) {
            setCardBackgroundColor(value)
            field = value
        }
}