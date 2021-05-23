package com.example.util.simpletimetracker.navigation.params

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class RecordTagSelectionParams(
    val typeId: Long = 0L
) : Parcelable
