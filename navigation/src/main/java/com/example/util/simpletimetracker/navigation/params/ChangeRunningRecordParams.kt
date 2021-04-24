package com.example.util.simpletimetracker.navigation.params

import android.os.Parcelable
import androidx.annotation.ColorInt
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
        val iconId: RecordTypeIconParams,
        @ColorInt val color: Int,
        val comment: String
    ) : Parcelable
}