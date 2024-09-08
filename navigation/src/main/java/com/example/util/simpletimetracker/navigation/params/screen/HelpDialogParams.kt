package com.example.util.simpletimetracker.navigation.params.screen

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class HelpDialogParams(
    val title: String = "",
    val text: CharSequence = "",
) : Parcelable, ScreenParams