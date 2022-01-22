package com.example.util.simpletimetracker.core.provider

import javax.inject.Inject

class CurrentTimestampProvider @Inject constructor() {

    fun get(): Long {
        return System.currentTimeMillis()
    }
}