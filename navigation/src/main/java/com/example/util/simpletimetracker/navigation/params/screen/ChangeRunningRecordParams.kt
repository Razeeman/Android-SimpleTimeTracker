package com.example.util.simpletimetracker.navigation.params.screen

import android.os.Parcelable
import androidx.annotation.ColorInt
import kotlinx.parcelize.Parcelize

@Parcelize
data class ChangeRunningRecordParams(
    val transitionName: String = "",
    val id: Long = 0,
    val preview: Preview? = null,
) : Parcelable, ScreenParams {

    @Parcelize
    data class Preview(
        var name: String,
        val tagName: String,
        var timeStarted: String,
        var duration: String,
        var goalTime: GoalTimeParams,
        var goalTime2: GoalTimeParams,
        var goalTime3: GoalTimeParams,
        var goalTime4: GoalTimeParams,
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
}