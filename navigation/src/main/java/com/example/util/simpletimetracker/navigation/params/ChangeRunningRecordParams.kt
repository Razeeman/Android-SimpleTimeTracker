package com.example.util.simpletimetracker.navigation.params

import android.os.Parcelable
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ChangeRunningRecordParams(
    val id: Long = 0,
    val preview: Preview? = null
) : Parcelable {

    @Parcelize
    data class Preview(
        var name: String,
        var timeStarted: String,
        var duration: String,
        var goalTime: String,
        @DrawableRes val iconId: Int,
        @ColorInt val color: Int
    ) : Parcelable
}