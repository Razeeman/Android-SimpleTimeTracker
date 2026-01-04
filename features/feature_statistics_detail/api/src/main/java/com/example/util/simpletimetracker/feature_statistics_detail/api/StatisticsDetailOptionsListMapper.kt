package com.example.util.simpletimetracker.feature_statistics_detail.api

import com.example.util.simpletimetracker.domain.base.ContainerOptionsModel
import com.example.util.simpletimetracker.domain.statistics.model.RangeLength
import com.example.util.simpletimetracker.navigation.params.screen.OptionsListParams

interface StatisticsDetailOptionsListMapper {

    suspend fun map(
        filterHidden: Boolean,
        rangeLength: RangeLength,
    ): List<OptionsListParams.Item>

    fun mapItemFromModel(model: ContainerOptionsModel.DetailedStatistics): OptionsListParams.Item.Id

    fun mapItemToModel(id: StatisticsDetailOptionsListItem): ContainerOptionsModel?
}