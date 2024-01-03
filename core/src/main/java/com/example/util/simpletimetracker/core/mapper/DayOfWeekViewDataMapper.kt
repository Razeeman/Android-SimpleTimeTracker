package com.example.util.simpletimetracker.core.mapper

import com.example.util.simpletimetracker.domain.model.DayOfWeek
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.dayOfWeek.DayOfWeekViewData
import javax.inject.Inject

class DayOfWeekViewDataMapper @Inject constructor(
    private val timeMapper: TimeMapper,
    private val colorMapper: ColorMapper,
) {

    fun mapViewData(
        selectedDaysOfWeek: List<DayOfWeek>,
        isDarkTheme: Boolean,
        width: DayOfWeekViewData.Width,
        paddingHorizontalDp: Int,
    ): List<ViewHolderType> {
        return DayOfWeek.values().map {
            val selected = it in selectedDaysOfWeek
            DayOfWeekViewData(
                dayOfWeek = it,
                text = timeMapper.toShortDayOfWeekName(it),
                color = if (selected) {
                    colorMapper.toActiveColor(isDarkTheme)
                } else {
                    colorMapper.toInactiveColor(isDarkTheme)
                },
                width = width,
                paddingHorizontalDp = paddingHorizontalDp,
            )
        }
    }
}