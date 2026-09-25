package com.example.util.simpletimetracker.feature_statistics_detail.model

import com.example.util.simpletimetracker.navigation.params.screen.OptionsListParams
import kotlinx.parcelize.Parcelize

@Parcelize
data class StatisticsDetailGoalOptionsListItem(
    val position: Int,
    val type: Type,
) : OptionsListParams.Item.Id {

    enum class Type {
        STREAKS,
        GOALS,
    }
}
