package com.example.util.simpletimetracker.core.utils

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner

fun getLifecycleObserverAdapter(
    onCreate: (() -> Unit)? = null,
    onStart: (() -> Unit)? = null,
    onResume: (() -> Unit)? = null,
    onPause: (() -> Unit)? = null,
    onStop: (() -> Unit)? = null,
    onDestroy: (() -> Unit)? = null,
) = object : DefaultLifecycleObserver {

    override fun onCreate(owner: LifecycleOwner) {
        onCreate?.invoke()
    }

    override fun onStart(owner: LifecycleOwner) {
        onStart?.invoke()
    }

    override fun onResume(owner: LifecycleOwner) {
        onResume?.invoke()
    }

    override fun onPause(owner: LifecycleOwner) {
        onPause?.invoke()
    }

    override fun onStop(owner: LifecycleOwner) {
        onStop?.invoke()
    }

    override fun onDestroy(owner: LifecycleOwner) {
        onDestroy?.invoke()
    }
}