package com.example.util.simpletimetracker.navigation.params.screen

import android.os.Parcelable
import androidx.annotation.ColorInt
import kotlinx.parcelize.Parcelize

sealed class ChangeRecordParams : Parcelable {
    abstract val daysFromToday: Int

    @Parcelize
    data class Tracked(
        val transitionName: String,
        val id: Long,
        val from: From,
        val preview: Preview,
        override val daysFromToday: Int,
    ) : ChangeRecordParams()

    @Parcelize
    data class Untracked(
        val transitionName: String,
        val timeStarted: Long,
        val timeEnded: Long,
        val preview: Preview,
        override val daysFromToday: Int,
    ) : ChangeRecordParams()

    @Parcelize
    data class New(
        override val daysFromToday: Int,
    ) : ChangeRecordParams()

    @Parcelize
    data class Preview(
        val name: String,
        val tagName: String,
        val timeStarted: String,
        val timeFinished: String,
        val timeStartedDateTime: ChangeRecordDateTimeStateParams,
        val timeEndedDateTime: ChangeRecordDateTimeStateParams,
        val duration: String,
        val iconId: RecordTypeIconParams,
        @ColorInt val color: Int,
        val comment: String,
    ) : Parcelable

    sealed class From : Parcelable {
        @Parcelize
        object Records : From()

        @Parcelize
        object RecordsAll : From()
    }
}