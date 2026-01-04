package com.example.util.simpletimetracker.feature_statistics.api

import com.example.util.simpletimetracker.domain.base.ContainerOptionsModel
import com.example.util.simpletimetracker.domain.statistics.model.RangeLength
import com.example.util.simpletimetracker.navigation.params.screen.OptionsListParams

interface StatisticsContainerOptionsListMapper {

    suspend fun map(
        filterHidden: Boolean,
        rangeLength: RangeLength,
    ): List<OptionsListParams.Item>

    fun mapItemFromModel(model: ContainerOptionsModel.Statistics): OptionsListParams.Item.Id

    fun mapItemToModel(id: StatisticsContainerOptionsListItem): ContainerOptionsModel?
}