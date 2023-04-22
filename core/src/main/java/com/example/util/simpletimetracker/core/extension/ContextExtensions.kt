package com.example.util.simpletimetracker.core.extension

import android.content.Context
import android.util.TypedValue
import androidx.appcompat.app.AppCompatActivity

fun Context.getThemedAttr(attrId: Int): Int {
    return TypedValue().apply {
        theme?.resolveAttribute(attrId, this, true)
    }.data
}

inline fun <reified T> Context.findListener(): T? {
    return when (this) {
        is T -> this
        is AppCompatActivity -> getAllFragments()
            .firstOrNull { it is T && it.isResumed }
            ?.let { it as? T }
        else -> null
    }
}