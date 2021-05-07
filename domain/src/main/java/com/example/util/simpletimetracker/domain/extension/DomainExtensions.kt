package com.example.util.simpletimetracker.domain.extension

fun Boolean?.orFalse(): Boolean = this ?: false

fun Boolean?.orTrue(): Boolean = this ?: true

fun Boolean.flip(): Boolean = !this

fun Long?.orZero(): Long = this ?: 0

fun Int?.orZero(): Int = this ?: 0

fun Float?.orZero(): Float = this ?: 0f

fun <T> List<T>?.orEmpty(): List<T> = this ?: emptyList()

fun <T> List<T>.rotateLeft(n: Int): List<T> = drop(n) + take(n)