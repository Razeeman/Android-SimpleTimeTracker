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
                name = getString(R.styleable.RecordView_name).orEmpty()
                color = getColor(R.styleable.RecordView_color, Color.BLACK)
                icon = getResourceId(R.styleable.RecordView_icon, R.drawable.unknown)
                timeStarted = getString(R.styleable.RecordView_timeStarted).orEmpty()
                timeEnded = getString(R.styleable.RecordView_timeEnded).orEmpty()
                duration = getString(R.styleable.RecordView_duration).orEmpty()
                recycle()
            }
    }

    var name: String = ""
        set(value) {
            tvRecordItemName.text = value
            field = value
        }

    var color: Int = 0
        set(value) {
            layoutRecordItem.setCardBackgroundColor(value)
            field = value
        }

    var icon: Int = 0
        set(value) {
            ivRecordItemIcon.setBackgroundResource(value)
            field = value
        }

    var timeStarted: String = ""
        set(value) {
            tvRecordItemTimeStarted.text = value
            field = value
        }

    var timeEnded: String = ""
        set(value) {
            tvRecordItemTimeFinished.text = value
            field = value
        }

    var duration: String = ""
        set(value) {
            tvRecordItemDuration.text = value
            field = value
        }
}