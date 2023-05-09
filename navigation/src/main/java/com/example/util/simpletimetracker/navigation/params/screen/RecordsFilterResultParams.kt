package com.example.util.simpletimetracker.navigation.params.screen

import com.example.util.simpletimetracker.domain.model.RecordsFilter

data class RecordsFilterResultParams(
    val tag: String,
    val filters: List<RecordsFilter>,
) : ScreenParams