package com.example.util.simpletimetracker.navigation.params

import android.os.Parcelable
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import kotlinx.android.parcel.Parcelize

sealed class ChangeRecordParams : Parcelable {

    @Parcelize
    data class Tracked(
        val transitionName: String,
        val id: Long,
        val preview: Preview
    ) : ChangeRecordParams()

    @Parcelize
    data class Untracked(
        val transitionName: String,
        val timeStarted: Long,
        val timeEnded: Long,
        val preview: Preview
    ) : ChangeRecordParams()

    @Parcelize
    data class New(
        val daysFromToday: Int = 0
    ) : ChangeRecordParams()

    @Parcelize
    data class Preview(
        val name: String,
        val timeStarted: String,
        val timeFinished: String,
        val duration: String,
        @DrawableRes val iconId: Int,
        @ColorInt val color: Int,
        val comment: String
    ) : Parcelable
}