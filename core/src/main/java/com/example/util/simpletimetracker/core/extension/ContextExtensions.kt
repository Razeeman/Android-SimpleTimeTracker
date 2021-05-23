package com.example.util.simpletimetracker.core.extension

import android.content.Context
import android.util.TypedValue

fun Context.getThemedAttr(attrId: Int): Int {
    return TypedValue().apply {
        theme?.resolveAttribute(attrId, this, true)
    }.data
}