package com.example.util.simpletimetracker.feature_records.api

import com.example.util.simpletimetracker.domain.base.ContainerOptionsModel
import com.example.util.simpletimetracker.navigation.params.screen.OptionsListParams

interface RecordsContainerOptionsListMapper {

    suspend fun map(
        filterHidden: Boolean,
    ): List<OptionsListParams.Item>

    fun mapItemFromModel(model: ContainerOptionsModel.Records): RecordsContainerOptionsListItem

    fun mapItemToModel(id: RecordsContainerOptionsListItem): ContainerOptionsModel
}