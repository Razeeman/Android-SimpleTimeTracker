package com.example.util.simpletimetracker.domain.model

sealed interface RecordBase {
    val typeIds: List<Long>
    val timeStarted: Long
    val timeEnded: Long
    val comment: String
    val tagIds: List<Long>

    val duration: Long get() = timeEnded - timeStarted
}