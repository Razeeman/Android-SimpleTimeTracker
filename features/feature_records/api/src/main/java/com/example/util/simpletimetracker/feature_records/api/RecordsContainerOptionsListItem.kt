package com.example.util.simpletimetracker.feature_records.api

import com.example.util.simpletimetracker.core.viewData.CommonOptionsListItem
import com.example.util.simpletimetracker.navigation.params.screen.OptionsListParams
import kotlinx.parcelize.Parcelize

sealed interface RecordsContainerOptionsListItem : OptionsListParams.Item.Id {

    @Parcelize
    data object CalendarView : RecordsContainerOptionsListItem

    @Parcelize
    data object Filter : RecordsContainerOptionsListItem, CommonOptionsListItem.Filter

    @Parcelize
    data object Share : RecordsContainerOptionsListItem, CommonOptionsListItem.Share

    @Parcelize
    data object BackToToday : RecordsContainerOptionsListItem, CommonOptionsListItem.BackToToday

    @Parcelize
    data object SelectDate : RecordsContainerOptionsListItem, CommonOptionsListItem.SelectDate
}