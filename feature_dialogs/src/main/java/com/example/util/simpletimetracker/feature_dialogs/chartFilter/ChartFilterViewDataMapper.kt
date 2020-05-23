package com.example.util.simpletimetracker.feature_dialogs.chartFilter

import com.example.util.simpletimetracker.core.mapper.ColorMapper
import com.example.util.simpletimetracker.core.mapper.IconMapper
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.model.RecordType
import javax.inject.Inject

class ChartFilterViewDataMapper @Inject constructor(
    private val iconMapper: IconMapper,
    private val colorMapper: ColorMapper,
    private val resourceRepo: ResourceRepo
) {

    // TODO move to core?
    fun map(recordType: RecordType): ChartFilterRecordTypeViewData {
        return ChartFilterRecordTypeViewData(
            id = recordType.id,
            name = recordType.name,
            iconId = recordType.icon
                .let(iconMapper::mapToDrawableResId),
            color = recordType.color
                .let(colorMapper::mapToColorResId)
                .let(resourceRepo::getColor)
        )
    }
}