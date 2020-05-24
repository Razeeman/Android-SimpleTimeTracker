package com.example.util.simpletimetracker.feature_dialogs.chartFilter.mapper

import com.example.util.simpletimetracker.core.mapper.ColorMapper
import com.example.util.simpletimetracker.core.mapper.IconMapper
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.model.RecordType
import com.example.util.simpletimetracker.feature_dialogs.R
import com.example.util.simpletimetracker.feature_dialogs.chartFilter.viewData.ChartFilterRecordTypeViewData
import javax.inject.Inject

class ChartFilterViewDataMapper @Inject constructor(
    private val iconMapper: IconMapper,
    private val colorMapper: ColorMapper,
    private val resourceRepo: ResourceRepo
) {

    fun map(
        recordType: RecordType,
        typeIdsFiltered: List<Long>
    ): ChartFilterRecordTypeViewData {
        return ChartFilterRecordTypeViewData(
            id = recordType.id,
            name = recordType.name,
            iconId = recordType.icon
                .let(iconMapper::mapToDrawableResId),
            color = if (recordType.id in typeIdsFiltered) {
                R.color.filtered_color
            } else {
                recordType.color.let(colorMapper::mapToColorResId)
            }.let(resourceRepo::getColor)
        )
    }

    fun mapToUntrackedItem(
        typeIdsFiltered: List<Long>
    ): ChartFilterRecordTypeViewData {
        return ChartFilterRecordTypeViewData(
            id = -1L,
            name = R.string.untracked_time_name
                .let(resourceRepo::getString),
            iconId = R.drawable.unknown,
            color = if (-1L in typeIdsFiltered) {
                R.color.filtered_color
            } else {
                R.color.untracked_time_color
            }.let(resourceRepo::getColor)
        )
    }
}