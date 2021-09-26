package com.example.util.simpletimetracker.navigation.params.screen

import android.os.Parcelable
import androidx.annotation.ColorInt
import kotlinx.parcelize.Parcelize

@Parcelize
data class StatisticsDetailParams(
    val transitionName: String = "",
    val filter: TypesFilterParams = TypesFilterParams(),
    val preview: Preview? = null
) : Parcelable, ScreenParams {

    @Parcelize
    data class Preview(
        val name: String,
        val iconId: RecordTypeIconParams? = null,
        @ColorInt val color: Int
    ) : Parcelable
}