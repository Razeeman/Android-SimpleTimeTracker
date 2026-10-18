package com.example.util.simpletimetracker.data_local.recordTag

import com.example.util.simpletimetracker.domain.color.model.AppColor
import com.example.util.simpletimetracker.domain.recordTag.model.RecordTag
import com.example.util.simpletimetracker.domain.recordTag.model.RecordTagValueType
import javax.inject.Inject

class RecordTagDataLocalMapper @Inject constructor() {

    fun map(dbo: RecordTagDBO): RecordTag {
        return RecordTag(
            id = dbo.id,
            name = dbo.name,
            icon = dbo.icon,
            color = AppColor(
                colorId = dbo.color,
                colorInt = dbo.colorInt,
                gradient = null,
            ),
            iconColorSource = dbo.iconColorSource,
            note = dbo.note,
            archived = dbo.archived,
            valueType = when (dbo.valueType) {
                null -> RecordTagValueType.NONE
                1 -> RecordTagValueType.NUMERIC
                else -> RecordTagValueType.NONE
            },
            valueSuffix = dbo.valueSuffix.orEmpty(),
        )
    }

    fun map(domain: RecordTag): RecordTagDBO {
        return RecordTagDBO(
            id = domain.id,
            typeId = 0,
            name = domain.name,
            icon = domain.icon,
            color = domain.color.colorId,
            colorInt = domain.color.colorInt,
            iconColorSource = domain.iconColorSource,
            note = domain.note,
            archived = domain.archived,
            valueType = when (domain.valueType) {
                // Store null to save storage.
                RecordTagValueType.NONE -> null
                RecordTagValueType.NUMERIC -> 1
            },
            valueSuffix = domain.valueSuffix.takeIf(String::isNotEmpty),
        )
    }
}