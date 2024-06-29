package com.example.util.simpletimetracker.domain.model

sealed interface PomodoroCycleType {
    object Focus : PomodoroCycleType
    object Break : PomodoroCycleType
    object LongBreak : PomodoroCycleType
}