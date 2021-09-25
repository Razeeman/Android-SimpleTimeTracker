package com.example.util.simpletimetracker.navigation.params.action

data class OpenFileParams(
    val notHandledCallback: (() -> Unit),
) : ActionParams