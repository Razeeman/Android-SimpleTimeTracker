package com.example.util.simpletimetracker.navigation.params.screen

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class CustomRangeSelectionParams(
    val rangeStart: Long? = null,
    val rangeEnd: Long? = null,
) : Parcelable, ScreenParams