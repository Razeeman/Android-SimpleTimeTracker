package com.example.util.simpletimetracker.domain.model

sealed interface ResultContainer<out T> {
    object Undefined : ResultContainer<Nothing>
    data class Defined<out T>(val value: T) : ResultContainer<T>

    fun getValueOrNull(): T? {
        return (this as? Defined)?.value
    }
}