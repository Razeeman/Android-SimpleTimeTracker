package com.example.util.simpletimetracker.feature_base_adapter.extensions

import android.content.Context
import android.util.TypedValue
import androidx.appcompat.app.AppCompatActivity

fun Context.getThemedAttr(attrId: Int): Int {
    return TypedValue().apply {
        theme?.resolveAttribute(attrId, this, true)
    }.data
}
