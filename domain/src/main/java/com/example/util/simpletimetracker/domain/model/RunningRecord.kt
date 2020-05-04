package com.example.util.simpletimetracker.domain.model

data class RunningRecord(
    var id: Long = 0,
    var name: String,
    var timeStarted: Long
)