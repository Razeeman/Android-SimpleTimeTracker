package com.example.util.simpletimetracker.domain.record.model

data class Range(
    val timeStarted: Long,
    val timeEnded: Long,
) {

    val duration: Long = timeEnded - timeStarted

    val isUndefined: Boolean = timeStarted == 0L && timeEnded == 0L

    fun isOverlappingWith(other: Range): Boolean {
        return timeStarted < other.timeEnded && timeEnded > other.timeStarted
    }

    fun contains(timestamp: Long): Boolean {
        return isUndefined || timestamp in timeStarted..<timeEnded
    }
}