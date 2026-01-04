package com.example.util.simpletimetracker.feature_settings.model

import com.example.util.simpletimetracker.navigation.params.screen.OptionsListParams
import kotlinx.parcelize.Parcelize

sealed interface CustomizeOptionsMenuListItem : OptionsListParams.Item.Id {

    @Parcelize
    data object Records : CustomizeOptionsMenuListItem

    @Parcelize
    data object Statistics : CustomizeOptionsMenuListItem

    @Parcelize
    data object DetailedStatistics : CustomizeOptionsMenuListItem
}