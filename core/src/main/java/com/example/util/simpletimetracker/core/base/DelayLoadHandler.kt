package com.example.util.simpletimetracker.core.base

import com.example.util.simpletimetracker.core.utils.DELAY_DATA_LOAD_MS
import kotlinx.coroutines.delay

interface DelayLoadHandler {

    var delayDataLoad: Boolean

    suspend fun delayLoad() {
        if (delayDataLoad) {
            // Delay data set on view to avoid screen open lag.
            delay(DELAY_DATA_LOAD_MS)
            delayDataLoad = false
        }
    }
}