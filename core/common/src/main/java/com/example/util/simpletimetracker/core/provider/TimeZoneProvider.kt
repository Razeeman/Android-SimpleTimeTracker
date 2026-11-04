package com.example.util.simpletimetracker.core.provider

import java.util.TimeZone
import javax.inject.Inject

open class TimeZoneProvider @Inject constructor() {

    open fun get(): TimeZone {
        return TimeZone.getDefault()
    }
}
