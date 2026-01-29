package com.example.util.simpletimetracker.core.utils

class SuspendedValue<T>(val getter: suspend () -> T) {

    private var value: T? = null

    suspend fun get(): T {
        return value ?: getter.invoke().also { value = it }
    }

    fun set(value: T) {
        this.value = value
    }
}