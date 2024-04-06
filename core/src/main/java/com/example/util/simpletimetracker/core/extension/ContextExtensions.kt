package com.example.util.simpletimetracker.core.extension

import android.content.Context
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

inline fun <reified T> Context.findListeners(): List<T> {
    val listeners = mutableListOf<T>()
    when (this) {
        is T -> {
            listeners.add(this as T)
        }
        is AppCompatActivity -> {
            this.getAllFragments()
                .filterIsInstance<T>()
                .let(listeners::addAll)
        }
    }
    return listeners
}