package com.example.util.simpletimetracker.navigation.params.action

data class CreateCsvFileParams(
    val notHandledCallback: (() -> Unit),
) : ActionParams