package com.example.util.simpletimetracker.core.view

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import com.example.util.simpletimetracker.core.R
import kotlinx.android.synthetic.main.record_type_view_layout.view.*

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

        val defaultPadding = resources.getDimensionPixelOffset(R.dimen.record_type_card_margin)
        context.obtainStyledAttributes(attrs, R.styleable.RecordTypeView, defStyleAttr, 0)
            .run {
                itemName = getString(R.styleable.RecordTypeView_itemName).orEmpty()
                itemColor = getColor(R.styleable.RecordTypeView_itemColor, Color.BLACK)
                itemIcon = getResourceId(R.styleable.RecordTypeView_itemIcon, R.drawable.unknown)
                itemAlpha = getFloat(R.styleable.RecordTypeView_itemAlpha, 1f)
                itemPadding = getDimensionPixelSize(
                    R.styleable.RecordTypeView_itemPadding, defaultPadding
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
            field = value
        }

    var itemAlpha: Float = 1f
        set(value) {
            layoutRecordTypeItem.alpha = value
            field = value
        }

    var itemPadding: Int = 0
        set(value) {
            layoutRecordTypeItem.apply {
                layoutParams = layoutParams.apply {
                    this as LayoutParams
                    setMargins(value, value, value, value)
                }
            }
            field = value
        }
}