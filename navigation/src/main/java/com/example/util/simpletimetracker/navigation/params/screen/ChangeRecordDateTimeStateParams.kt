package com.example.util.simpletimetracker.navigation.params.screen

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ChangeRecordDateTimeStateParams(
    val hint: String,
    val state: State,
) : Parcelable {

    sealed interface State : Parcelable {
        @Parcelize
        data class DateTime(
            val date: String,
            val time: String,
        ) : State

        @Parcelize
        data class Duration(
            val data: String,
        ) : State
    }
}