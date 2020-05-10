package com.example.util.simpletimetracker.core.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
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

    // TODO add attr

    init {
        // TODO Merge layout?
        View.inflate(context, R.layout.record_type_view_layout, this)
    }

    // TODO replace with vars
    fun setName(name: String) {
        tvRecordTypeItemName.text = name
    }

    fun setColor(@ColorInt color: Int) {
        layoutRecordTypeItem.setCardBackgroundColor(color)
    }

    fun setIcon(@DrawableRes icon: Int) {
        ivRecordTypeItemIcon.setBackgroundResource(icon)
    }
}