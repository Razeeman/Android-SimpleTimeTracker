package com.example.util.simpletimetracker.navigation.params.action

data class CreateFileParams(
    val fileName: String,
    val type: String,
    val notHandledCallback: (() -> Unit),
) : ActionParams