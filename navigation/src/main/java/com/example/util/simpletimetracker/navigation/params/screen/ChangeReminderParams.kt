package com.example.util.simpletimetracker.navigation.params.screen

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

sealed interface ChangeReminderParams : ScreenParams, Parcelable {

    @Parcelize
    data class Change(val id: Long) : ChangeReminderParams

    @Parcelize
    object New : ChangeReminderParams
}
