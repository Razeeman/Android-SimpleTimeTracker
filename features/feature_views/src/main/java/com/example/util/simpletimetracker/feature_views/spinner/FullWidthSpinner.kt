package com.example.util.simpletimetracker.feature_views.spinner

import android.content.Context
import android.content.res.Resources
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatSpinner
import com.example.util.simpletimetracker.feature_views.R

class FullWidthSpinner @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.spinnerStyle,
    mode: Int = -1,
    popupTheme: Resources.Theme? = null,
) : AppCompatSpinner(context, attrs, defStyleAttr, mode, popupTheme) {

    private var popupUnderSpinner: Boolean = false
    private var popupMarginTop: Int = 0
    private var processSameItemSelection: Boolean = true

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

    override fun setSelection(position: Int) {
        processSelection(position)
        super.setSelection(position)
    }

    override fun setSelection(position: Int, animate: Boolean) {
        processSelection(position)
        super.setSelection(position, animate)
    }

    fun setProcessSameItemSelection(enabled: Boolean) {
        processSameItemSelection = enabled
    }

    private fun processSelection(position: Int) {
        if (!processSameItemSelection) return

        val sameSelected = position == selectedItemPosition
        val listener = onItemSelectedListener
        if (sameSelected && listener != null) {
            // Spinner does not call the OnItemSelectedListener if the same item is selected, so do it manually now
            listener.onItemSelected(this, selectedView, position, selectedItemId)
        }
    }
}
