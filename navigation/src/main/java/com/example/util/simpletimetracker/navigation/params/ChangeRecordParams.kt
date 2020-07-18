package com.example.util.simpletimetracker.navigation.params

sealed class ChangeRecordParams {

    data class Tracked(
        val transitionName: String,
        val id: Long
    ) : ChangeRecordParams()

    data class Untracked(
        val transitionName: String,
        val timeStarted: Long,
        val timeEnded: Long
    ) : ChangeRecordParams()

    data class New(
        val daysFromToday: Int
    ) : ChangeRecordParams()
}