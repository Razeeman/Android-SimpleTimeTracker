package com.example.util.simpletimetracker.navigation.params.screen

import com.example.util.simpletimetracker.domain.model.RecordsFilter

data class RecordsFilterResultParams(
    val filters: List<RecordsFilter> = emptyList(),
    val filteredRecordsTypeId: Long? = null, // If all records of one type.
) : ScreenParams