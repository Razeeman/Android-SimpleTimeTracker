package com.example.util.simpletimetracker.feature_dialogs.dateTime

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.TimePicker

class CustomTimePicker @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : TimePicker(
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