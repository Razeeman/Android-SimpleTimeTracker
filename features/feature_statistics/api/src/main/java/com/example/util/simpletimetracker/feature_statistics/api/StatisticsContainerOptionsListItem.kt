package com.example.util.simpletimetracker.feature_statistics.api

import com.example.util.simpletimetracker.core.viewData.CommonOptionsListItem
import com.example.util.simpletimetracker.navigation.params.screen.OptionsListParams
import kotlinx.parcelize.Parcelize

sealed interface StatisticsContainerOptionsListItem : OptionsListParams.Item.Id {

    @Parcelize
    data object Filter : StatisticsContainerOptionsListItem, CommonOptionsListItem.Filter

    @Parcelize
    data object Share : StatisticsContainerOptionsListItem, CommonOptionsListItem.Share

    @Parcelize
    data object BackToToday : StatisticsContainerOptionsListItem, CommonOptionsListItem.BackToToday

    @Parcelize
    data object SelectDate : StatisticsContainerOptionsListItem, CommonOptionsListItem.SelectDate

    @Parcelize
    data object SelectRange : StatisticsContainerOptionsListItem, CommonOptionsListItem.SelectRange
}