package com.example.util.simpletimetracker.domain.model

sealed interface Optional<out T> {

    object Empty : Optional<Nothing>

    data class Value<T>(val value: T) : Optional<T>

    companion object {
        fun <T> valueOf(value: T): Optional<T> {
            return Value(value)
        }
    }
}

fun <T> Optional<T>.getOrNull(): T? {
    return (this as? Optional.Value<T>)?.value
}