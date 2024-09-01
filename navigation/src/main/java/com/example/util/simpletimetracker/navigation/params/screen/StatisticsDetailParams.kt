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

    sealed class RangeLengthParams : Parcelable {
        @Parcelize
        object Day : RangeLengthParams()

        @Parcelize
        object Week : RangeLengthParams()

        @Parcelize
        object Month : RangeLengthParams()

        @Parcelize
        object Year : RangeLengthParams()

        @Parcelize
        object All : RangeLengthParams()

        @Parcelize
        data class Custom(val start: Long, val end: Long) : RangeLengthParams()

        @Parcelize
        data class Last(val days: Int) : RangeLengthParams()
    }

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