package com.example.util.simpletimetracker.navigation.params.screen

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class RecordsFilterParams(
    val tag: String = "",
    val title: String = "",
    val dateSelectionAvailable: Boolean = true,
    val untrackedSelectionAvailable: Boolean = true,
    val multitaskSelectionAvailable: Boolean = true,
    val addRunningRecords: Boolean = true,
    val filters: List<RecordsFilterParam> = emptyList(),
) : ScreenParams, Parcelable