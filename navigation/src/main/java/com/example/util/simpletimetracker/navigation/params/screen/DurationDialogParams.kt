package com.example.util.simpletimetracker.navigation.params.screen

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class DurationDialogParams(
    val tag: String? = null,
    val duration: Long = 0,
    val hideDisableButton: Boolean = false,
) : ScreenParams, Parcelable