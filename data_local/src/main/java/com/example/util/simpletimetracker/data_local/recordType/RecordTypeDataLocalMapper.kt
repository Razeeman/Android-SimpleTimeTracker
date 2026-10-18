package com.example.util.simpletimetracker.data_local.recordType

import com.example.util.simpletimetracker.domain.color.model.AppColor
import com.example.util.simpletimetracker.domain.extension.normalizeColorAngle
import com.example.util.simpletimetracker.domain.extension.orZero
import com.example.util.simpletimetracker.domain.recordType.model.RecordType
import javax.inject.Inject

class RecordTypeDataLocalMapper @Inject constructor() {

    fun map(dbo: RecordTypeDBO): RecordType {
        return RecordType(
            id = dbo.id,
            name = dbo.name,
            icon = dbo.icon,
            color = AppColor(
                colorId = dbo.color,
                colorInt = dbo.colorInt,
                gradient = dbo.gradientAngle?.let { angle ->
                    AppColor.Gradient(
                        endColorId = dbo.endColor,
                        endColorInt = dbo.endColorInt,
                        angleDegrees = normalizeColorAngle(angle),
                    )
                },
            ),
            defaultDuration = dbo.defaultDuration,
            note = dbo.note,
            hidden = dbo.hidden,
        )
    }

    fun map(domain: RecordType): RecordTypeDBO {
        return RecordTypeDBO(
            id = domain.id,
            name = domain.name,
            icon = domain.icon,
            color = domain.color.colorId,
            colorInt = domain.color.colorInt,
            endColor = domain.color.gradient?.endColorId.orZero(),
            endColorInt = domain.color.gradient?.endColorInt.orEmpty(),
            gradientAngle = domain.color.gradient?.angleDegrees?.let(::normalizeColorAngle),
            defaultDuration = domain.defaultDuration,
            note = domain.note,
            hidden = domain.hidden,
        )
    }
}