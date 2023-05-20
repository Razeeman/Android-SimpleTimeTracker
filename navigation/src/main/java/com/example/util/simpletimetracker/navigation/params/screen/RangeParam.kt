package com.example.util.simpletimetracker.navigation.params.screen

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class RangeParams(
    val timeStarted: Long,
    val timeEnded: Long,
) : Parcelable