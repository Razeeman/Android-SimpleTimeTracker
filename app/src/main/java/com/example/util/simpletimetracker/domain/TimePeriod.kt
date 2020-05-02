package com.example.util.simpletimetracker.domain

data class TimePeriod(
    var id: Long = 0,
    var name: String,
    var timeStarted: Long,
    var timeEnded: Long
)