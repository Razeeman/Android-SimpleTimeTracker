package com.example.util.simpletimetracker.navigation.params.action

data class OpenFileParams(
    val type: String,
    val notHandledCallback: (() -> Unit),
) : ActionParams