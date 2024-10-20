package com.example.util.simpletimetracker.navigation.params.screen

import android.os.Parcelable
import androidx.annotation.ColorInt
import kotlinx.parcelize.Parcelize

@Parcelize
data class ChangeRunningRecordParams(
    val transitionName: String,
    val id: Long,
    val from: From,
    val preview: Preview?,
) : Parcelable, ScreenParams {

    @Parcelize
    data class Preview(
        val name: String,
        val tagName: String,
        val timeStarted: String,
        val timeStartedDateTime: ChangeRecordDateTimeStateParams,
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
    }

    sealed class From : Parcelable {
        @Parcelize
        object Records : From()

        @Parcelize
        object RunningRecords : From()
    }

    companion object {
        val Empty = ChangeRunningRecordParams(
            transitionName = "",
            id = 0,
            from = From.RunningRecords,
            preview = null,
        )
    }
}