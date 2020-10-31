package com.example.util.simpletimetracker.core.view.spinner

import android.content.Context
import android.content.res.Resources
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatSpinner
import com.example.util.simpletimetracker.core.R

class FullWidthSpinner @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.spinnerStyle,
    mode: Int = -1,
    popupTheme: Resources.Theme? = null
) : AppCompatSpinner(context, attrs, defStyleAttr, mode, popupTheme) {

    private var popupUnderSpinner: Boolean = false
    private var popupMarginTop: Int = 0

    init {
        context.obtainStyledAttributes(attrs, R.styleable.FullWidthSpinner, defStyleAttr, 0)
            .run {
                popupUnderSpinner =
                    getBoolean(R.styleable.FullWidthSpinner_popupUnderSpinner, false)
                popupMarginTop =
                    getDimensionPixelSize(R.styleable.FullWidthSpinner_popupUnderMarginTop, 0)
                recycle()
            }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        dropDownWidth = w
        if (popupUnderSpinner) {
            dropDownVerticalOffset = h + popupMarginTop
        }
    }
}
