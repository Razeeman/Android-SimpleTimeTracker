package com.example.util.simpletimetracker.navigation.params.screen

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class RecordTagSelectionParams(
    val typeId: Long,
    val fields: List<Field>,
) : Parcelable, ScreenParams {

    sealed interface Field : Parcelable {
        @Parcelize
        object Tags : Field

        @Parcelize
        object Comment : Field
    }

    companion object {
        val Empty = RecordTagSelectionParams(
            typeId = 0L,
            fields = emptyList(),
        )
    }
}
