package com.example.util.simpletimetracker.navigation.params.screen

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class DurationDialogParams(
    val tag: String? = null,
    val value: Value = Value.DurationSeconds(0),
    val hideDisableButton: Boolean = false,
    val showSeconds: Boolean = true,
) : ScreenParams, Parcelable {

    sealed interface Value : Parcelable {
        @Parcelize
        data class DurationSeconds(val duration: Long) : Value

        @Parcelize
        data class Count(val count: Long) : Value
    }
}