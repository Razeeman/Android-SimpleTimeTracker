package com.example.util.simpletimetracker.feature_change_record_type.mapper

import com.example.util.simpletimetracker.core.mapper.ColorMapper
import com.example.util.simpletimetracker.core.mapper.IconMapper
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.model.RecordType
import com.example.util.simpletimetracker.feature_change_record_type.viewData.ChangeRecordTypeViewData
import javax.inject.Inject

class ChangeRecordTypeViewDataMapper @Inject constructor(
    private val iconMapper: IconMapper,
    private val colorMapper: ColorMapper,
    private val resourceRepo: ResourceRepo
) {

    fun map(recordType: RecordType): ChangeRecordTypeViewData {
        return ChangeRecordTypeViewData(
            name = recordType.name,
            icon = recordType.icon
                .let(iconMapper::mapToDrawableResId),
            color = recordType.color
                .let(colorMapper::mapToColorResId)
                .let(resourceRepo::getColor)
        )
    }
}