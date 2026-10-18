package com.example.util.simpletimetracker.domain.extension

private const val MINUTE_IN_MILLIS = 60_000
private const val SECOND_IN_MILLIS = 1_000

fun Boolean?.orFalse(): Boolean = this ?: false

fun Boolean?.orTrue(): Boolean = this ?: true

fun Boolean.flip(): Boolean = !this

fun Long?.orZero(): Long = this ?: 0

fun Int?.orZero(): Int = this ?: 0

fun Float?.orZero(): Float = this ?: 0f

fun Double?.orZero(): Double = this ?: 0.0

fun <T> List<T>?.orEmpty(): List<T> = this ?: emptyList()

fun <T> List<T>.rotateLeft(n: Int): List<T> = drop(n) + take(n)

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

inline fun <reified T> Any.tryCast(): T? {
    return if (this is T) this else null
}

inline fun <T> T?.ifNull(newValue: () -> T): T {
    return this ?: newValue()
}

fun normalizeColorAngle(angleDegrees: Int): Int {
    return ((angleDegrees % 360) + 360) % 360
}
