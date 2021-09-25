package com.example.util.simpletimetracker.navigation.params.action

data class CreateFileParams(
    val notHandledCallback: (() -> Unit),
) : ActionParams