package com.example.util.simpletimetracker.feature_statistics.customView

import android.os.Parcelable
import androidx.annotation.ColorInt
import com.example.util.simpletimetracker.core.viewData.RecordTypeIcon
import kotlinx.parcelize.Parcelize

@Parcelize
data class PiePortion(
    val value: Long,
    @ColorInt val colorInt: Int,
    val iconId: RecordTypeIcon? = null
) : Parcelable