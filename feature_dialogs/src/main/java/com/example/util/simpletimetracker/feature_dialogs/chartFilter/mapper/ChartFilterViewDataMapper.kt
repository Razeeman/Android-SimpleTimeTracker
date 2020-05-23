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
                filteredColorResId
            } else {
                recordType.color.let(colorMapper::mapToColorResId)
            }.let(resourceRepo::getColor)
        )
    }

    companion object {
        private val filteredColorResId = R.color.blue_grey_200
    }
}