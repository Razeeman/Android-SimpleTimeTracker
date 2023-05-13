package com.example.util.simpletimetracker.domain.model

enum class DaysInCalendar {
    ONE, THREE, FIVE, SEVEN,
}

val DaysInCalendar.count: Int
    get() {
        return when (this) {
            DaysInCalendar.ONE -> 1
            DaysInCalendar.THREE -> 3
            DaysInCalendar.FIVE -> 5
            DaysInCalendar.SEVEN -> 7
        }
    }