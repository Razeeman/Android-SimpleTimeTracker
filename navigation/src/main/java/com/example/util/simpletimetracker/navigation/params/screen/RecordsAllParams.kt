package com.example.util.simpletimetracker.navigation.params.screen

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class RecordsAllParams(
    val filter: TypesFilterParams = TypesFilterParams(),
    val rangeStart: Long = 0L,
    val rangeEnd: Long = 0L
) : Parcelable, ScreenParams