package com.example.util.simpletimetracker.core.mapper

import com.example.util.simpletimetracker.domain.model.ActivityFilter
import com.example.util.simpletimetracker.feature_base_adapter.activityFilter.ActivityFilterViewData
import javax.inject.Inject

class ActivityFilterViewDataMapper @Inject constructor(
    private val colorMapper: ColorMapper,
) {

    fun map(
        filter: ActivityFilter,
        isDarkTheme: Boolean,
    ): ActivityFilterViewData {
        val selected = filter.selected
        return ActivityFilterViewData(
            id = filter.id,
            name = filter.name,
            iconColor = if (selected) {
                colorMapper.toIconColor(isDarkTheme)
            } else {
                colorMapper.toFilteredIconColor(isDarkTheme)
            },
            color = if (selected) {
                colorMapper.mapToColorInt(filter.color, isDarkTheme)
            } else {
                colorMapper.toFilteredColor(isDarkTheme)
            },
            selected = selected,
        )
    }
}