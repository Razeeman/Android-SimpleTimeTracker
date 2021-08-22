package com.example.util.simpletimetracker.navigation.params

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class CsvExportSettingsParams(
    val range: Range? = null,
) : Parcelable {

    @Parcelize
    data class Range(
        val rangeStart: Long,
        val rangeEnd: Long,
    ) : Parcelable
}