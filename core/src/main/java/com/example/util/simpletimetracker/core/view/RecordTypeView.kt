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

        context.obtainStyledAttributes(attrs, R.styleable.RecordTypeView, defStyleAttr, 0)
            .run {
                name = getString(R.styleable.RecordTypeView_name).orEmpty()
                color = getColor(R.styleable.RecordTypeView_color, Color.BLACK)
                icon = getResourceId(R.styleable.RecordTypeView_icon, R.drawable.unknown)
                recycle()
            }
    }

    var name: String = ""
        set(value) {
            tvRecordTypeItemName.text = value
            field = value
        }

    var color: Int = 0
        set(value) {
            layoutRecordTypeItem.setCardBackgroundColor(value)
            field = value
        }

    var icon: Int = 0
        set(value) {
            ivRecordTypeItemIcon.setBackgroundResource(value)
            field = value
        }
}