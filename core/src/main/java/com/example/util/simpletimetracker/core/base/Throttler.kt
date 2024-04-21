package com.example.util.simpletimetracker.core.base

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

interface Throttler : LifecycleOwner {

    var throttleJob: Job?

    fun throttle(
        destinationFunction: () -> Unit,
    ): () -> Unit = {
        throttler { destinationFunction() }
    }

    fun <T> throttle(
        destinationFunction: (T) -> Unit,
    ): (T) -> Unit = { param ->
        throttler { destinationFunction(param) }
    }

    fun <T, U> throttle(
        destinationFunction: (T, U) -> Any,
    ): (T, U) -> Unit = { param1, param2 ->
        throttler { destinationFunction(param1, param2) }
    }

    private fun throttler(block: () -> Unit) {
        if (throttleJob?.isCompleted != false) {
            throttleJob = lifecycleScope.launch {
                block()
                delay(THROTTLE_PERIOD_MS)
            }
        }
    }

    companion object {
        private const val THROTTLE_PERIOD_MS = 500L
    }
}