package com.example.util.simpletimetracker.core.extension

import android.content.Context
import android.util.TypedValue
import androidx.appcompat.app.AppCompatActivity

inline fun <reified T> Context.findListener(): T? {
    return when (this) {
        is T -> this
        is AppCompatActivity -> getAllFragments()
            .firstOrNull { it is T && it.isResumed }
            ?.let { it as? T }
        else -> null
    }
}