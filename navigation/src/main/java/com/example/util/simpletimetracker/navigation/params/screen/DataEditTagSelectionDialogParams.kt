package com.example.util.simpletimetracker.navigation.params.screen

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class DataEditTagSelectionDialogParams(
    val tag: String = "",
    val typeId: Long = 0L,
) : Parcelable, ScreenParams
