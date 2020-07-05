package com.example.util.simpletimetracker.core.view

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import com.example.util.simpletimetracker.core.R
import kotlinx.android.synthetic.main.record_running_view_layout.view.*

class RunningRecordView @JvmOverloads constructor(
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
        View.inflate(context, R.layout.record_running_view_layout, this)

        context.obtainStyledAttributes(attrs, R.styleable.RunningRecordView, defStyleAttr, 0)
            .run {
                itemName = getString(R.styleable.RunningRecordView_itemName).orEmpty()
                itemColor = getColor(R.styleable.RunningRecordView_itemColor, Color.BLACK)
                itemIcon = getResourceId(R.styleable.RunningRecordView_itemIcon, R.drawable.unknown)
                itemTimeStarted = getString(R.styleable.RunningRecordView_itemTimeStarted).orEmpty()
                itemTimer = getString(R.styleable.RunningRecordView_itemTimer).orEmpty()
                recycle()
            }
    }

    var itemName: String = ""
        set(value) {
            tvRunningRecordItemName.text = value
            field = value
        }

    var itemColor: Int = 0
        set(value) {
            layoutRunningRecordItem.setCardBackgroundColor(value)
            field = value
        }

    var itemIcon: Int = 0
        set(value) {
            ivRunningRecordItemIcon.setBackgroundResource(value)
            ivRunningRecordItemIcon.tag = value
            field = value
        }

    var itemTimeStarted: String = ""
        set(value) {
            tvRunningRecordItemTimeStarted.text = value
            field = value
        }

    var itemTimer: String = ""
        set(value) {
            tvRunningRecordItemTimer.text = value
            field = value
        }
}