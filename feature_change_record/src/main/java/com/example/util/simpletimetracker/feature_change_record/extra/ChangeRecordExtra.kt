package com.example.util.simpletimetracker.feature_change_record.extra

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

sealed class ChangeRecordExtra : Parcelable {

    @Parcelize
    data class Tracked(
        val transitionName: String,
        val id: Long
    ) : ChangeRecordExtra()

    @Parcelize
    data class Untracked(
        val transitionName: String,
        val timeStarted: Long,
        val timeEnded: Long
    ) : ChangeRecordExtra()

    @Parcelize
    data class New(
        val daysFromToday: Int = 0
    ) : ChangeRecordExtra()
}