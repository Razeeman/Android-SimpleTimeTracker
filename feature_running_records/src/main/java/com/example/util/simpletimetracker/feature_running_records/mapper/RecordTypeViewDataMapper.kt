package com.example.util.simpletimetracker.feature_running_records.mapper

import com.example.util.simpletimetracker.core.mapper.IconMapper
import com.example.util.simpletimetracker.domain.model.RecordType
import com.example.util.simpletimetracker.feature_running_records.adapter.recordType.RecordTypeViewData
import javax.inject.Inject

class RecordTypeViewDataMapper @Inject constructor(
    private val iconMapper: IconMapper
) {

    fun map(recordType: RecordType): RecordTypeViewData {
        return RecordTypeViewData(
            id = recordType.id,
            name = recordType.name,
            iconId = recordType.icon.let(iconMapper::mapToDrawableId),
            color = recordType.color
        )
    }
}