package com.example.util.simpletimetracker.core.view

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.View
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.example.util.simpletimetracker.core.R
import kotlinx.android.synthetic.main.record_view_layout.view.ivRecordItemIcon
import kotlinx.android.synthetic.main.record_view_layout.view.tvRecordItemDuration
import kotlinx.android.synthetic.main.record_view_layout.view.tvRecordItemName
import kotlinx.android.synthetic.main.record_view_layout.view.tvRecordItemTimeFinished
import kotlinx.android.synthetic.main.record_view_layout.view.tvRecordItemTimeStarted

class RecordView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : CardView(
    context,
    attrs,
    defStyleAttr
) {

    init {
        View.inflate(context, R.layout.record_view_layout, this)

        ContextCompat.getColor(context, R.color.black).let(::setCardBackgroundColor)
        radius = resources.getDimensionPixelOffset(R.dimen.record_type_card_corner_radius).toFloat()
        // TODO doesn't work here for some reason, need to set in the layout
        cardElevation = resources.getDimensionPixelOffset(R.dimen.record_type_card_elevation).toFloat()
        preventCornerOverlap = false
        useCompatPadding = true

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
            setCardBackgroundColor(value)
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