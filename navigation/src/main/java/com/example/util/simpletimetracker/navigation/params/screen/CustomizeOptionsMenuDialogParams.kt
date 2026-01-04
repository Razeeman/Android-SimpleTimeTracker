package com.example.util.simpletimetracker.navigation.params.screen

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class CustomizeOptionsMenuDialogParams(
    val from: From,
) : ScreenParams, Parcelable {

    sealed interface From : Parcelable {
        @Parcelize
        data object Records : From

        @Parcelize
        data object Statistics : From

        @Parcelize
        data object DetailedStatistics : From
    }

    companion object {
        val Empty = CustomizeOptionsMenuDialogParams(
            from = From.Records,
        )
    }
}