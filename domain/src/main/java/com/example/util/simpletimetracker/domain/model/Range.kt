package com.example.util.simpletimetracker.domain.model

data class Range(
    val timeStarted: Long,
    val timeEnded: Long
) {

    val duration: Long = timeEnded - timeStarted
}