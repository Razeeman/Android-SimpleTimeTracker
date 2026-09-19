package com.example.util.simpletimetracker.navigation.params.screen

import kotlinx.parcelize.Parcelize

sealed interface ReminderConditionTargetType : OptionsListParams.Item.Id {

    @Parcelize
    data object Activity : ReminderConditionTargetType

    @Parcelize
    data object Category : ReminderConditionTargetType

    @Parcelize
    data object Tag : ReminderConditionTargetType
}
