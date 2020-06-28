package com.example.util.simpletimetracker.core.view

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import com.example.util.simpletimetracker.core.R
import kotlinx.android.synthetic.main.record_view_layout.view.*

class RecordView @JvmOverloads constructor(
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
        View.inflate(context, R.layout.record_view_layout, this)

        context.obtainStyledAttributes(attrs, R.styleable.RecordView, defStyleAttr, 0)
            .run {
                itemName = getString(R.styleable.RecordView_itemName).orEmpty()
                itemColor = getColor(R.styleable.RecordView_itemColor, Color.BLACK)
                itemIcon = getResourceId(R.styleable.RecordView_itemIcon, R.drawable.unknown)
                itemTimeStarted = getString(R.styleable.RecordView_itemTimeStarted).orEmpty()
                itemTimeEnded = getString(R.styleable.RecordView_itemTimeEnded).orEmpty()
                itemDuration = getString(R.styleable.RecordView_itemDuration).orEmpty()
                recycle()
            }
    }

    var itemName: String = ""
        set(value) {
            tvRecordItemName.text = value
            field = value
        }

    var itemColor: Int = 0
        set(value) {
            layoutRecordItem.setCardBackgroundColor(value)
            field = value
        }

    var itemIcon: Int = 0
        set(value) {
            ivRecordItemIcon.setBackgroundResource(value)
            ivRecordItemIcon.tag = value
            field = value
        }

    var itemTimeStarted: String = ""
        set(value) {
            tvRecordItemTimeStarted.text = value
            field = value
        }

    var itemTimeEnded: String = ""
        set(value) {
            tvRecordItemTimeFinished.text = value
            field = value
        }

    var itemDuration: String = ""
        set(value) {
            tvRecordItemDuration.text = value
            field = value
        }
}