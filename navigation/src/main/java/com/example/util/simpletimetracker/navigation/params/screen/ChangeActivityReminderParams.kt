package com.example.util.simpletimetracker.navigation.params.screen

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
sealed interface ChangeActivityReminderParams : ScreenParams, Parcelable {

    @Parcelize
    data class Change(val activityId: Long) : ChangeActivityReminderParams

    @Parcelize
    data object New : ChangeActivityReminderParams
}
