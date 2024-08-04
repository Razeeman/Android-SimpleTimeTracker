package com.example.util.simpletimetracker.navigation.params.screen

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ChartFilterDialogParams(
    val type: Type,
) : Parcelable, ScreenParams {

    sealed interface Type : Parcelable {
        @Parcelize
        object RecordsList : Type

        @Parcelize
        object Statistics : Type
    }

    companion object {
        val Empty = ChartFilterDialogParams(
            type = Type.Statistics,
        )
    }
}