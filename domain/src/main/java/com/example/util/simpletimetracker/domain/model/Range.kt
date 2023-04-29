package com.example.util.simpletimetracker.domain.model

// TODO switch to typealias to avoid object creation.
data class Range(
    val timeStarted: Long,
    val timeEnded: Long
) {

    val duration: Long = timeEnded - timeStarted
}