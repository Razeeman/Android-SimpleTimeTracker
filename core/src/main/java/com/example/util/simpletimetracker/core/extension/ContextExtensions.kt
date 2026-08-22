package com.example.util.simpletimetracker.core.extension

import android.content.Context
import androidx.appcompat.app.AppCompatActivity

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