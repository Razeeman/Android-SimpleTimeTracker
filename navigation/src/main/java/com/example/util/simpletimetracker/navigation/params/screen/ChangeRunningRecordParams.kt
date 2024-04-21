package com.example.util.simpletimetracker.navigation.params.screen

import android.os.Parcelable
import androidx.annotation.ColorInt
import kotlinx.parcelize.Parcelize

@Parcelize
data class ChangeRunningRecordParams(
    val transitionName: String = "",
    val id: Long = 0,
    val from: From = From.RunningRecords,
    val preview: Preview? = null,
) : Parcelable, ScreenParams {

    @Parcelize
    data class Preview(
        val name: String,
        val tagName: String,
        val timeStarted: String,
        val timeStartedDateTime: DateTime,
        val duration: String,
        val durationTotal: String,
        val goalTime: GoalTimeParams,
        val iconId: RecordTypeIconParams,
        @ColorInt val color: Int,
        val comment: String,
    ) : Parcelable {

        @Parcelize
        data class GoalTimeParams(
            val text: String,
            val complete: Boolean,
        ) : Parcelable

        @Parcelize
        data class DateTime(
            val date: String,
            val time: String,
        ) : Parcelable
    }

    sealed class From : Parcelable {
        @Parcelize
        object Records : From()

        @Parcelize
        object RunningRecords : From()
    }
}