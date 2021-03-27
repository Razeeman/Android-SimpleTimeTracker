package com.example.util.simpletimetracker.navigation.params

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class RecordsAllParams(
    val typeIds: List<Long> = emptyList(),
    val rangeStart: Long = 0L,
    val rangeEnd: Long = 0L
) : Parcelable