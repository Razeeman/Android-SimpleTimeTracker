package com.example.util.simpletimetracker.core.extension

fun <T> MutableList<T>.addOrRemove(item: T) {
    if (item in this) remove(item) else add(item)
}