package com.example.util.simpletimetracker.domain.model

sealed interface RepeatButtonType {
    object RepeatLast : RepeatButtonType
    object RepeatBeforeLast : RepeatButtonType
}