package com.example.util.simpletimetracker.core.view

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.View
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.example.util.simpletimetracker.core.R
import com.example.util.simpletimetracker.core.extension.visible
import kotlinx.android.synthetic.main.record_running_view_layout.view.*

class RunningRecordView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : CardView(
    context,
    attrs,
    defStyleAttr
) {

    init {
        View.inflate(context, R.layout.record_running_view_layout, this)

        ContextCompat.getColor(context, R.color.black).let(::setCardBackgroundColor)
        radius = resources.getDimensionPixelOffset(R.dimen.record_type_card_corner_radius).toFloat()
        // TODO doesn't work here for some reason, need to set in the layout
        cardElevation = resources.getDimensionPixelOffset(R.dimen.record_type_card_elevation).toFloat()
        preventCornerOverlap = false
        useCompatPadding = true

        context.obtainStyledAttributes(attrs, R.styleable.RunningRecordView, defStyleAttr, 0)
            .run {
                if (hasValue(R.styleable.RunningRecordView_itemName)) itemName =
                    getString(R.styleable.RunningRecordView_itemName).orEmpty()

                if (hasValue(R.styleable.RunningRecordView_itemColor)) itemColor =
                    getColor(R.styleable.RunningRecordView_itemColor, Color.BLACK)

                if (hasValue(R.styleable.RunningRecordView_itemIcon)) itemIcon =
                    getResourceId(R.styleable.RunningRecordView_itemIcon, R.drawable.unknown)

                if (hasValue(R.styleable.RunningRecordView_itemTimeStarted)) itemTimeStarted =
                    getString(R.styleable.RunningRecordView_itemTimeStarted).orEmpty()

                if (hasValue(R.styleable.RunningRecordView_itemTimer)) itemTimer =
                    getString(R.styleable.RunningRecordView_itemTimer).orEmpty()

                if (hasValue(R.styleable.RunningRecordView_itemGoalTime)) itemGoalTime =
                    getString(R.styleable.RunningRecordView_itemGoalTime).orEmpty()

                if (hasValue(R.styleable.RunningRecordView_itemComment)) itemComment =
                    getString(R.styleable.RunningRecordView_itemComment).orEmpty()

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
            setCardBackgroundColor(value)
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

    var itemGoalTime: String = ""
        set(value) {
            tvRunningRecordItemGoalTime.text = value
            tvRunningRecordItemGoalTime.visible = value.isNotEmpty()
            field = value
        }

    var itemComment: String = ""
        set(value) {
            tvRunningRecordItemComment.text = value
            tvRunningRecordItemComment.visible = value.isNotEmpty()
            field = value
        }
}