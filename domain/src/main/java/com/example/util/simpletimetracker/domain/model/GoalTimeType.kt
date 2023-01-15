package com.example.util.simpletimetracker.domain.model

sealed interface GoalTimeType {
    object Session : GoalTimeType
    object Day : GoalTimeType
    object Week : GoalTimeType
    object Month : GoalTimeType
}