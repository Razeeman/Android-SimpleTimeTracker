package com.example.util.simpletimetracker.navigation.params.screen

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class RecordsFilterParams(
    val tag: String,
    val title: String,
    val dateSelectionAvailable: Boolean,
    val untrackedSelectionAvailable: Boolean,
    val multitaskSelectionAvailable: Boolean,
    val addRunningRecords: Boolean,
    val filters: List<RecordsFilterParam>,
    val defaultLastDaysNumber: Int,
) : ScreenParams, Parcelable {

    companion object {
        val Empty = RecordsFilterParams(
            tag = "",
            title = "",
            dateSelectionAvailable = true,
            untrackedSelectionAvailable = true,
            multitaskSelectionAvailable = true,
            addRunningRecords = true,
            filters = emptyList(),
            defaultLastDaysNumber = 0,
        )
    }
}