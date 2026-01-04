package com.example.util.simpletimetracker.feature_statistics_detail.api

import com.example.util.simpletimetracker.core.viewData.CommonOptionsListItem
import com.example.util.simpletimetracker.navigation.params.screen.OptionsListParams
import kotlinx.parcelize.Parcelize

sealed interface StatisticsDetailOptionsListItem : OptionsListParams.Item.Id {

    @Parcelize
    data object Filter : StatisticsDetailOptionsListItem, CommonOptionsListItem.Filter

    @Parcelize
    data object Compare : StatisticsDetailOptionsListItem

    @Parcelize
    data object BackToToday : StatisticsDetailOptionsListItem, CommonOptionsListItem.BackToToday

    @Parcelize
    data object SelectDate : StatisticsDetailOptionsListItem, CommonOptionsListItem.SelectDate

    @Parcelize
    data object SelectRange : StatisticsDetailOptionsListItem, CommonOptionsListItem.SelectRange
}