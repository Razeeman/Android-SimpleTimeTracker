package com.example.util.simpletimetracker.navigation.params.screen

import android.os.Parcelable
import androidx.annotation.ColorInt
import kotlinx.parcelize.Parcelize

@Parcelize
data class StatisticsDetailParams(
    val transitionName: String,
    val filter: List<RecordsFilterParam>,
    val range: RangeLengthParams,
    val shift: Int,
    val preview: Preview?,
) : Parcelable, ScreenParams {

    @Parcelize
    data class Preview(
        val name: String,
        val iconId: RecordTypeIconParams? = null,
        @ColorInt val color: Int,
    ) : Parcelable

    companion object {
        val Empty = StatisticsDetailParams(
            transitionName = "",
            filter = emptyList(),
            range = RangeLengthParams.All,
            shift = 0,
            preview = null,
        )
    }
}