package com.example.util.simpletimetracker.feature_records.model

import androidx.annotation.DrawableRes

data class RecordsOptionsSwitchState(
    @DrawableRes val moreIconResId: Int,
    val state: State,
    val calendarSwitchState: CalendarSwitchState,
) {

    sealed interface State {
        object Closed : State
        object Opened : State
    }

    sealed interface CalendarSwitchState {

        object Hidden : CalendarSwitchState

        data class Visible(
            @DrawableRes val iconResId: Int,
        ) : CalendarSwitchState
    }
}