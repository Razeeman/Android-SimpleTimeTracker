package com.example.util.simpletimetracker.feature_goals.model

import com.example.util.simpletimetracker.navigation.params.screen.OptionsListParams
import kotlinx.parcelize.Parcelize

sealed interface GoalsOptionsListItem : OptionsListParams.Item.Id {

    @Parcelize
    data object HideFinished : GoalsOptionsListItem
}
