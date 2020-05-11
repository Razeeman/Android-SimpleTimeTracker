package com.example.util.simpletimetracker.domain.extension

fun Boolean?.orFalse() = this ?: false

fun Boolean?.orTrue() = this ?: true

fun Boolean.flip() = !this

fun Long?.orZero() = this ?: 0