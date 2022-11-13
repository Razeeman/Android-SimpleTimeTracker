package com.example.util.simpletimetracker.feature_views

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.example.util.simpletimetracker.feature_views.databinding.CategoryViewLayoutBinding
import com.example.util.simpletimetracker.feature_views.extension.visible
import com.example.util.simpletimetracker.feature_views.viewData.RecordTypeIcon

class CategoryView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : CardView(
    context,
    attrs,
    defStyleAttr
) {

    private val binding: CategoryViewLayoutBinding = CategoryViewLayoutBinding
        .inflate(LayoutInflater.from(context), this)

    init {
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

                if (hasValue(R.styleable.CategoryView_itemIcon)) itemIcon =
                    getResourceId(R.styleable.CategoryView_itemIcon, R.drawable.unknown)
                        .let(RecordTypeIcon::Image)

                if (hasValue(R.styleable.CategoryView_itemIconText)) itemIcon =
                    getString(R.styleable.CategoryView_itemIconText).orEmpty()
                        .let(RecordTypeIcon::Text)

                if (hasValue(R.styleable.CategoryView_itemIconColor)) {
                    itemIconColor = getColor(R.styleable.CategoryView_itemIconColor, Color.WHITE)
                }

                if (hasValue(R.styleable.CategoryView_itemIconAlpha)) {
                    itemIconAlpha = getFloat(R.styleable.CategoryView_itemIconAlpha, 1.0f)
                }

                if (hasValue(R.styleable.CategoryView_itemIconVisible)) itemIconVisible =
                    getBoolean(R.styleable.CategoryView_itemIconVisible, false)

                recycle()
            }
    }

    var itemName: String = ""
        set(value) {
            binding.tvCategoryItemName.text = value
            field = value
        }

    var itemColor: Int = 0
        set(value) {
            setCardBackgroundColor(value)
            field = value
        }

    var itemIcon: RecordTypeIcon = RecordTypeIcon.Image(0)
        set(value) {
            binding.ivCategoryItemIcon.itemIcon = value
            field = value
        }

    var itemIconColor: Int = 0
        set(value) {
            binding.tvCategoryItemName.setTextColor(value)
            binding.ivCategoryItemIcon.itemIconColor = value
            field = value
        }

    var itemIconAlpha: Float = 1.0f
        set(value) {
            binding.ivCategoryItemIcon.itemIconAlpha = value
            field = value
        }

    var itemIconVisible: Boolean = false
        set(value) {
            binding.ivCategoryItemIcon.visible = value
            field = value
        }
}