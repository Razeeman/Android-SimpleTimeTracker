package com.example.util.simpletimetracker.navigation.params

import android.os.Parcelable
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import com.example.util.simpletimetracker.domain.model.ChartFilterType
import kotlinx.android.parcel.Parcelize

@Parcelize
data class StatisticsDetailParams(
    val id: Long = 0,
    val filterType: ChartFilterType = ChartFilterType.ACTIVITY,
    val preview: Preview? = null
) : Parcelable {

    @Parcelize
    data class Preview(
        val name: String,
        @DrawableRes val iconId: Int? = null,
        @ColorInt val color: Int
    ) : Parcelable
}