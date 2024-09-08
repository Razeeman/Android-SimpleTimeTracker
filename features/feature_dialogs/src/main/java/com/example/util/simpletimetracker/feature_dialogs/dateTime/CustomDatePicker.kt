package com.example.util.simpletimetracker.feature_dialogs.dateTime

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.DatePicker

class CustomDatePicker @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : DatePicker(
    context,
    attrs,
) {

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        // Stop ScrollView from getting involved once you interact with the View
        if (ev.actionMasked == MotionEvent.ACTION_DOWN) {
            parent?.requestDisallowInterceptTouchEvent(true)
        }
        return false
    }
}