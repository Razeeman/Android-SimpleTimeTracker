package com.example.util.simpletimetracker.navigation.params.screen

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

sealed interface RangeLengthParams : Parcelable {
    @Parcelize
    object Day : RangeLengthParams

    @Parcelize
    object Week : RangeLengthParams

    @Parcelize
    object Month : RangeLengthParams

    @Parcelize
    object Year : RangeLengthParams

    @Parcelize
    object All : RangeLengthParams

    @Parcelize
    data class Custom(val start: Long, val end: Long) : RangeLengthParams

    @Parcelize
    data class Last(val days: Int) : RangeLengthParams
}