package com.example.util.simpletimetracker.core.extension

/**
 * Adds item if it not in the list, otherwise removes it from the list.
 */
fun <T> MutableList<T>.addOrRemove(item: T) {
    if (item in this) remove(item) else add(item)
}