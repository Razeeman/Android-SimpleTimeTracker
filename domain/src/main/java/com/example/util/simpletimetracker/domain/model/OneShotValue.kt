package com.example.util.simpletimetracker.domain.model

data class OneShotValue<T>(
    private var value: T?,
) {

    fun getValue(): T? {
        return value.also { value = null }
    }
}