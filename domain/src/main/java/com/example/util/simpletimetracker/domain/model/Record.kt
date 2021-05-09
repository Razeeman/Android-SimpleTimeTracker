package com.example.util.simpletimetracker.domain.model

data class Record(
    val id: Long = 0,
    val typeId: Long,
    val timeStarted: Long,
    val timeEnded: Long,
    val comment: String,
    val tagId: Long = 0
)