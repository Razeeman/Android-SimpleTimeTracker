package com.example.util.simpletimetracker.core.view

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.ViewCompat
import com.example.util.simpletimetracker.core.R
import com.example.util.simpletimetracker.core.extension.setMargins
import kotlinx.android.synthetic.main.record_type_view_layout.view.*
import kotlinx.android.synthetic.main.record_type_view_vertical.view.*

class RecordTypeView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(
    context,
    attrs,
    defStyleAttr
) {

    init {
        // TODO Merge layout?
        View.inflate(context, R.layout.record_type_view_layout, this)

        context.obtainStyledAttributes(attrs, R.styleable.RecordTypeView, defStyleAttr, 0)
            .run {
                // TODO check hasValue first to avoid double work
                itemName = getString(
                    R.styleable.RecordTypeView_itemName
                ).orEmpty()
                itemColor = getColor(
                    R.styleable.RecordTypeView_itemColor, Color.BLACK
                )
                itemIcon = getResourceId(
                    R.styleable.RecordTypeView_itemIcon, R.drawable.unknown
                )
                if (hasValue(R.styleable.RecordTypeView_itemIconColor)) {
                    itemIconColor = getColor(
                        R.styleable.RecordTypeView_itemIconColor, Color.WHITE
                    )
                }
                itemAlpha = getFloat(
                    R.styleable.RecordTypeView_itemAlpha, 1f
                )
                itemUseCompatPadding = getBoolean(
                    R.styleable.RecordTypeView_itemUseCompatPadding, true
                )
                itemIsRow = getBoolean(
                    R.styleable.RecordTypeView_itemIsRow, false
                )
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
            layoutRecordTypeItem.setCardBackgroundColor(value)
            field = value
        }

    var itemIcon: Int = 0
        set(value) {
            ivRecordTypeItemIcon.setBackgroundResource(value)
            ivRecordTypeItemIcon.tag = value
            field = value
        }

    var itemIconColor: Int = 0
        set(value) {
            tvRecordTypeItemName.setTextColor(value)
            ViewCompat.setBackgroundTintList(ivRecordTypeItemIcon, ColorStateList.valueOf(value))
            field = value
        }

    var itemAlpha: Float = 1f
        set(value) {
            layoutRecordTypeItem.alpha = value
            field = value
        }

    var itemUseCompatPadding: Boolean = true
        set(value) {
            layoutRecordTypeItem.useCompatPadding = value
            field = value
        }

    var itemIsRow: Boolean = false
        set(value) {
            changeConstraints(value)
            field = value
        }

    private fun changeConstraints(isRow: Boolean) {
        if (isRow) {
            val setRow = ConstraintSet().apply { clone(context, R.layout.record_type_view_horizontal) }
            setRow.applyTo(constraintRecordTypeItem)

            ivRecordTypeItemIcon.setMargins(start = 6)
            tvRecordTypeItemName.gravity = Gravity.START or Gravity.CENTER_VERTICAL
            tvRecordTypeItemName.setMargins(top = 0, start = 8)
        } else {
            val setRow = ConstraintSet().apply { clone(context, R.layout.record_type_view_vertical) }
            setRow.applyTo(constraintRecordTypeItem)

            ivRecordTypeItemIcon.setMargins(start = 0)
            tvRecordTypeItemName.gravity = Gravity.CENTER
            tvRecordTypeItemName.setMargins(top = 4, start = 0)
        }
    }
}