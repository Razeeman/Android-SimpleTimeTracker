package com.example.util.simpletimetracker.domain.model

data class Record(
    var id: Long = 0,
    var name: String,
    var timeStarted: Long,
    var timeEnded: Long
)