package com.example.util.simpletimetracker.navigation.params.screen

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class DurationDialogParams(
    val tag: String? = null,
    val value: Value = Value.Duration(0),
    val hideDisableButton: Boolean = false,
) : ScreenParams, Parcelable {

    sealed interface Value : Parcelable {
        @Parcelize
        data class Duration(val duration: Long) : Value

        @Parcelize
        data class Count(val count: Long) : Value
    }
}