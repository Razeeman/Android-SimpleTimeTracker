package com.example.util.simpletimetracker.core.utils
import androidx.test.espresso.idling.CountingIdlingResource

object CountingIdlingResourceProvider {

    private const val RESOURCE = "GLOBAL"

    @JvmField val countingIdlingResource = CountingIdlingResource(RESOURCE)

    fun increment() {
        countingIdlingResource.increment()
    }

    fun decrement() {
        if (!countingIdlingResource.isIdleNow) {
            countingIdlingResource.decrement()
        }
    }
}