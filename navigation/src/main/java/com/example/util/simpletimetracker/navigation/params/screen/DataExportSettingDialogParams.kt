package com.example.util.simpletimetracker.navigation.params.screen

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class DataExportSettingDialogParams(
    val tag: String? = null,
) : ScreenParams, Parcelable

@Parcelize
data class DataExportSettingsResult(
    val tag: String,
    val range: Range? = null,
) : Parcelable {

    @Parcelize
    data class Range(
        val rangeStart: Long,
        val rangeEnd: Long,
    ) : Parcelable
}