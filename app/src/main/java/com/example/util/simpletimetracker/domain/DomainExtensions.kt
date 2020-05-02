package com.example.util.simpletimetracker.domain

fun Boolean?.orFalse() = this ?: false

fun Boolean?.orTrue() = this ?: true