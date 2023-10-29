package com.example.util.simpletimetracker.feature_views.extension

import android.content.Context
import android.util.TypedValue

fun Context.getThemedAttr(attrId: Int): Int {
    return TypedValue().apply {
        theme?.resolveAttribute(attrId, this, true)
    }.data
}
