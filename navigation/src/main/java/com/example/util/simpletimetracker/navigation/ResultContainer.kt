package com.example.util.simpletimetracker.navigation

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ResultContainer @Inject constructor() {
    private val listeners: MutableMap<String, ResultListener> = mutableMapOf()

    fun setResultListener(key: String, listener: ResultListener) {
        listeners[key] = listener
    }

    fun sendResult(key: String, data: Any?) {
        listeners.remove(key)?.onResult(data)
    }
}