package com.example.util.simpletimetracker.feature_views

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import com.example.util.simpletimetracker.feature_views.databinding.RecordTypeViewLayoutBinding
import com.example.util.simpletimetracker.feature_views.extension.setMargins
import com.example.util.simpletimetracker.feature_views.viewData.RecordTypeIcon

class RecordTypeView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : CardView(
    context,
    attrs,
    defStyleAttr
) {

    private val binding: RecordTypeViewLayoutBinding = RecordTypeViewLayoutBinding
        .inflate(LayoutInflater.from(context), this)

    init {
        ContextCompat.getColor(context, R.color.black).let(::setCardBackgroundColor)
        radius = resources.getDimensionPixelOffset(
            R.dimen.record_type_card_corner_radius
        ).toFloat()
        // TODO doesn't work here for some reason, need to set in the layout
        cardElevation = resources.getDimensionPixelOffset(
            R.dimen.record_type_card_elevation
        ).toFloat()
        preventCornerOverlap = false
        useCompatPadding = true

        context.obtainStyledAttributes(attrs, R.styleable.RecordTypeView, defStyleAttr, 0)
            .run {
                if (hasValue(R.styleable.RecordTypeView_itemName)) {
                    itemName = getString(R.styleable.RecordTypeView_itemName).orEmpty()
                }

                if (hasValue(R.styleable.RecordTypeView_itemColor)) {
                    itemColor = getColor(R.styleable.RecordTypeView_itemColor, Color.BLACK)
                }

                if (hasValue(R.styleable.RecordTypeView_itemIcon)) {
                    val data = getResourceId(R.styleable.RecordTypeView_itemIcon, R.drawable.unknown)
                    itemIcon = RecordTypeIcon.Image(data)
                }

                if (hasValue(R.styleable.RecordTypeView_itemEmoji)) {
                    val data = getString(R.styleable.RecordTypeView_itemEmoji).orEmpty()
                    itemIcon = RecordTypeIcon.Emoji(data)
                }

                if (hasValue(R.styleable.RecordTypeView_itemIconColor)) {
                    itemIconColor = getColor(R.styleable.RecordTypeView_itemIconColor, Color.WHITE)
                }

                if (hasValue(R.styleable.RecordTypeView_itemIconAlpha)) {
                    itemIconAlpha = getFloat(R.styleable.RecordTypeView_itemIconAlpha, 1.0f)
                }

                if (hasValue(R.styleable.RecordTypeView_itemIsRow)) {
                    itemIsRow = getBoolean(R.styleable.RecordTypeView_itemIsRow, false)
                }

                recycle()
            }
    }

    var itemName: String = ""
        set(value) {
            binding.container.tvRecordTypeItemName.text = value
            field = value
        }

    var itemColor: Int = 0
        set(value) {
            setCardBackgroundColor(value)
            field = value
        }

    var itemIcon: RecordTypeIcon = RecordTypeIcon.Image(0)
        set(value) {
            binding.container.ivRecordTypeItemIcon.itemIcon = value
            field = value
        }

    var itemIconColor: Int = 0
        set(value) {
            binding.container.tvRecordTypeItemName.setTextColor(value)
            binding.container.ivRecordTypeItemIcon.itemIconColor = value
            field = value
        }

    var itemIconAlpha: Float = 1.0f
        set(value) {
            binding.container.ivRecordTypeItemIcon.itemIconAlpha = value
            field = value
        }

    var itemIsRow: Boolean = false
        set(value) {
            changeConstraints(value)
            field = value
        }

    private fun changeConstraints(isRow: Boolean) = with(binding.container) {
        if (isRow) {
            val setRow = ConstraintSet()
                .apply { clone(context, R.layout.record_type_view_horizontal) }
            setRow.applyTo(constraintRecordTypeItem)

            ivRecordTypeItemIcon.setMargins(start = 6)
            tvRecordTypeItemName.gravity = Gravity.START or Gravity.CENTER_VERTICAL
            tvRecordTypeItemName.setMargins(top = 0, start = 8)
        } else {
            val setRow = ConstraintSet()
                .apply { clone(context, R.layout.record_type_view_vertical) }
            setRow.applyTo(constraintRecordTypeItem)

            ivRecordTypeItemIcon.setMargins(start = 0)
            tvRecordTypeItemName.gravity = Gravity.CENTER
            tvRecordTypeItemName.setMargins(top = 4, start = 0)
        }
    }
}