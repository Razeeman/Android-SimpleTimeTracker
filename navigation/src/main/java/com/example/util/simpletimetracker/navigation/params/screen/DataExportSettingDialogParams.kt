package com.example.util.simpletimetracker.navigation.params.screen

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class DataExportSettingDialogParams(
    val tag: String,
    val selectedRange: RangeLengthParams,
    val defaultFileName: String,
    val customFileName: String,
) : ScreenParams, Parcelable {

    companion object {
        val Empty = DataExportSettingDialogParams(
            tag = "",
            selectedRange = RangeLengthParams.Day,
            defaultFileName = "",
            customFileName = "",
        )
    }
}

@Parcelize
data class DataExportSettingsResult(
    val tag: String,
    val customFileName: String,
    val range: RangeLengthParams,
) : Parcelable