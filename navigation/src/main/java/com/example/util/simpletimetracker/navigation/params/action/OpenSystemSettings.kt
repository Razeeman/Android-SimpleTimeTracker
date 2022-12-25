package com.example.util.simpletimetracker.navigation.params.action

sealed interface OpenSystemSettings : ActionParams {
    object ExactAlarms : OpenSystemSettings
}