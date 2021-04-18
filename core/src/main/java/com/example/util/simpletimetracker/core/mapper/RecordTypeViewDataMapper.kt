package com.example.util.simpletimetracker.core.mapper

import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.core.viewData.RecordTypeIcon
import com.example.util.simpletimetracker.core.viewData.RecordTypeViewData
import com.example.util.simpletimetracker.domain.model.RecordType
import javax.inject.Inject

class RecordTypeViewDataMapper @Inject constructor(
    private val iconImageMapper: IconImageMapper,
    private val colorMapper: ColorMapper,
    private val resourceRepo: ResourceRepo,
    private val recordTypeCardSizeMapper: RecordTypeCardSizeMapper
) {

    fun map(
        recordType: RecordType,
        isDarkTheme: Boolean
    ): RecordTypeViewData {
        return RecordTypeViewData(
            id = recordType.id,
            name = recordType.name,
            iconId = mapIcon(recordType.icon),
            iconColor = colorMapper.toIconColor(isDarkTheme),
            color = mapColor(recordType.color, isDarkTheme)
        )
    }

    fun map(
        recordType: RecordType,
        numberOfCards: Int,
        isDarkTheme: Boolean
    ): RecordTypeViewData {
        return RecordTypeViewData(
            id = recordType.id,
            name = recordType.name,
            iconId = mapIcon(recordType.icon),
            iconColor = colorMapper.toIconColor(isDarkTheme),
            color = mapColor(recordType.color, isDarkTheme),
            width = recordTypeCardSizeMapper.toCardWidth(numberOfCards),
            height = recordTypeCardSizeMapper.toCardHeight(numberOfCards),
            asRow = recordTypeCardSizeMapper.toCardAsRow(numberOfCards)
        )
    }

    fun mapFiltered(
        recordType: RecordType,
        numberOfCards: Int,
        isDarkTheme: Boolean,
        isFiltered: Boolean
    ): RecordTypeViewData {
        val default = map(recordType, numberOfCards, isDarkTheme)

        return if (isFiltered) {
            default.copy(
                color = colorMapper.toFilteredColor(isDarkTheme),
                iconColor = colorMapper.toFilteredIconColor(isDarkTheme)
            )
        } else {
            default
        }
    }

    fun mapIcon(icon: String): RecordTypeIcon {
        return if (icon.startsWith("ic_") || icon.isEmpty()) {
            icon.let(iconImageMapper::mapToDrawableResId).let(RecordTypeIcon::Image)
        } else {
            RecordTypeIcon.Emoji(icon)
        }
    }

    private fun mapColor(color: Int, isDarkTheme: Boolean): Int {
        return color
            .let { colorMapper.mapToColorResId(it, isDarkTheme) }
            .let(resourceRepo::getColor)
    }
}