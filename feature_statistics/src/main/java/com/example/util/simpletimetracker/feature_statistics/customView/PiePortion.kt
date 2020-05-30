package com.example.util.simpletimetracker.feature_statistics.customView

import android.os.Parcelable
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import kotlinx.android.parcel.Parcelize

@Parcelize
data class PiePortion(
    val value: Long,
    @ColorInt val colorInt: Int,
    @DrawableRes val iconId: Int? = null
) : Parcelable