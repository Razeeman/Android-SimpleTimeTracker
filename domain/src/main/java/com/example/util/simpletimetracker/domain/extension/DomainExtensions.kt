package com.example.util.simpletimetracker.domain.extension

import com.example.util.simpletimetracker.domain.model.Range
import com.example.util.simpletimetracker.domain.model.RecordBase
import com.example.util.simpletimetracker.domain.model.RecordTag

private const val MINUTE_IN_MILLIS = 60_000
private const val SECOND_IN_MILLIS = 1_000

fun Boolean?.orFalse(): Boolean = this ?: false

fun Boolean?.orTrue(): Boolean = this ?: true

fun Boolean.flip(): Boolean = !this

fun Long?.orZero(): Long = this ?: 0

fun Int?.orZero(): Int = this ?: 0

fun Float?.orZero(): Float = this ?: 0f

fun <T> List<T>?.orEmpty(): List<T> = this ?: emptyList()

fun <T> List<T>.rotateLeft(n: Int): List<T> = drop(n) + take(n)

fun List<RecordTag>.getFullName(): String =
    this.joinToString(separator = ", ") { it.name }

fun RecordBase.toRange(): Range {
    return Range(timeStarted = timeStarted, timeEnded = timeEnded)
}

fun Long.dropSeconds(): Long {
    return this / MINUTE_IN_MILLIS * MINUTE_IN_MILLIS
}

fun Long.dropMillis(): Long {
    return this / SECOND_IN_MILLIS * SECOND_IN_MILLIS
}

fun Long.toDuration(): String {
    return this.toString().padDuration()
}

fun String.padDuration(): String {
    return this.padStart(2, '0')
}

fun String.insertStringAtIndex(string: String, index: Int): String {
    return StringBuilder(this).apply { insert(index, string) }.toString()
}

fun CharSequence.indexesOf(pat: String): List<Int> =
    pat.toRegex()
        .findAll(this)
        .map { it.range.first }
        .toList()

@Suppress("UNCHECKED_CAST")
fun <T> Any.tryCast(): T? {
    return this as? T
}