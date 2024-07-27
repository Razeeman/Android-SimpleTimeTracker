package com.example.util.simpletimetracker.feature_records.model

import androidx.annotation.DrawableRes

sealed interface RecordsCalendarSwitchState {

    object Hidden : RecordsCalendarSwitchState

    data class Visible(
        @DrawableRes val iconResId: Int,
    ) : RecordsCalendarSwitchState
}