package com.example.util.simpletimetracker.core.mapper

import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.core.viewData.RecordTypeViewData
import com.example.util.simpletimetracker.domain.model.RecordType
import javax.inject.Inject

class RecordTypeViewDataMapper @Inject constructor(
    private val iconMapper: IconMapper,
    private val colorMapper: ColorMapper,
    private val resourceRepo: ResourceRepo,
    private val recordTypeCardSizeMapper: RecordTypeCardSizeMapper
) {

    fun map(recordType: RecordType, isDarkTheme: Boolean): RecordTypeViewData {
        return RecordTypeViewData(
            id = recordType.id,
            name = recordType.name,
            iconId = recordType.icon
                .let(iconMapper::mapToDrawableResId),
            color = recordType.color
                .let { colorMapper.mapToColorResId(it, isDarkTheme) }
                .let(resourceRepo::getColor)
        )
    }

    fun map(recordType: RecordType, numberOfCards: Int, isDarkTheme: Boolean): RecordTypeViewData {
        return RecordTypeViewData(
            id = recordType.id,
            name = recordType.name,
            iconId = recordType.icon
                .let(iconMapper::mapToDrawableResId),
            color = recordType.color
                .let { colorMapper.mapToColorResId(it, isDarkTheme) }
                .let(resourceRepo::getColor),
            width = recordTypeCardSizeMapper.toCardWidth(numberOfCards),
            height = recordTypeCardSizeMapper.toCardHeight(numberOfCards),
            asRow = recordTypeCardSizeMapper.toCardAsRow(numberOfCards)
        )
    }
}