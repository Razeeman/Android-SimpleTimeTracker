package com.example.util.simpletimetracker.core.view

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import com.example.util.simpletimetracker.core.R
import com.example.util.simpletimetracker.core.extension.setMargins
import com.example.util.simpletimetracker.core.extension.visible
import com.example.util.simpletimetracker.core.viewData.RecordTypeIcon
import kotlinx.android.synthetic.main.record_type_view_vertical.view.constraintRecordTypeItem
import kotlinx.android.synthetic.main.record_type_view_vertical.view.ivRecordTypeItemIcon
import kotlinx.android.synthetic.main.record_type_view_vertical.view.tvRecordTypeItemEmoji
import kotlinx.android.synthetic.main.record_type_view_vertical.view.tvRecordTypeItemName

class RecordTypeView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : CardView(
    context,
    attrs,
    defStyleAttr
) {

    init {
        View.inflate(context, R.layout.record_type_view_layout, this)

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

                if (hasValue(R.styleable.RecordTypeView_itemIsRow)) {
                    itemIsRow = getBoolean(R.styleable.RecordTypeView_itemIsRow, false)
                }

                recycle()
            }
    }

    var itemName: String = ""
        set(value) {
            tvRecordTypeItemName.text = value
            field = value
        }

    var itemColor: Int = 0
        set(value) {
            setCardBackgroundColor(value)
            field = value
        }

    var itemIcon: RecordTypeIcon = RecordTypeIcon.Image(0)
        set(value) {
            when (value) {
                is RecordTypeIcon.Image -> setImageIcon(value.iconId)
                is RecordTypeIcon.Emoji -> setEmojiIcon(value.emojiText)
            }
            field = value
        }

    var itemIconColor: Int = 0
        set(value) {
            tvRecordTypeItemName.setTextColor(value)
            ViewCompat.setBackgroundTintList(ivRecordTypeItemIcon, ColorStateList.valueOf(value))
            tvRecordTypeItemEmoji.setTextColor(value)
            field = value
        }

    var itemIsRow: Boolean = false
        set(value) {
            changeConstraints(value)
            field = value
        }

    private fun setImageIcon(value: Int) {
        ivRecordTypeItemIcon.setBackgroundResource(value)
        ivRecordTypeItemIcon.tag = value
        ivRecordTypeItemIcon.visible = true
        tvRecordTypeItemEmoji.visible = false
    }

    private fun setEmojiIcon(value: String) {
        tvRecordTypeItemEmoji.text = value
        ivRecordTypeItemIcon.visibility = View.INVISIBLE
        tvRecordTypeItemEmoji.visible = true
    }

    private fun changeConstraints(isRow: Boolean) {
        if (isRow) {
            val setRow = ConstraintSet()
                .apply { clone(context, R.layout.record_type_view_horizontal) }
            setRow.applyTo(constraintRecordTypeItem)

            ivRecordTypeItemIcon.setMargins(start = 6)
            tvRecordTypeItemEmoji.setMargins(start = 6)
            tvRecordTypeItemName.gravity = Gravity.START or Gravity.CENTER_VERTICAL
            tvRecordTypeItemName.setMargins(top = 0, start = 8)
        } else {
            val setRow = ConstraintSet()
                .apply { clone(context, R.layout.record_type_view_vertical) }
            setRow.applyTo(constraintRecordTypeItem)

            ivRecordTypeItemIcon.setMargins(start = 0)
            tvRecordTypeItemEmoji.setMargins(start = 0)
            tvRecordTypeItemName.gravity = Gravity.CENTER
            tvRecordTypeItemName.setMargins(top = 4, start = 0)
        }
    }
}