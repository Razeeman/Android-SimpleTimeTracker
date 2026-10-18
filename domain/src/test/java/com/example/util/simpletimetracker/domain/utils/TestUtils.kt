package com.example.util.simpletimetracker.domain.utils

import com.example.util.simpletimetracker.domain.color.model.AppColor
import com.example.util.simpletimetracker.domain.record.model.RecordBase
import com.example.util.simpletimetracker.domain.recordTag.model.RecordTag
import com.example.util.simpletimetracker.domain.recordTag.model.RecordTagValueType

fun tag(id: Long, value: Double? = null): RecordBase.Tag {
    return RecordBase.Tag(
        tagId = id,
        numericValue = value,
    )
}

fun recordTag(tagId: Long): RecordTag {
    return RecordTag(
        id = tagId,
        name = "",
        icon = "",
        color = AppColor(0, "", null),
        iconColorSource = 0,
        note = "",
        archived = false,
        valueType = RecordTagValueType.NONE,
        valueSuffix = "",
    )
}