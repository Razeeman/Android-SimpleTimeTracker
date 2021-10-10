package com.example.util.simpletimetracker.domain.model

data class RunningRecord(
    val id: Long,
    val timeStarted: Long,
    val comment: String,
    val tagIds: List<Long> = emptyList()
)