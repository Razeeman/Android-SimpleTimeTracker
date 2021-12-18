package com.example.util.simpletimetracker.core.mapper

import androidx.annotation.ColorInt
import com.example.util.simpletimetracker.core.R
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.empty.EmptyViewData
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.model.AppColor
import com.example.util.simpletimetracker.feature_views.viewData.RecordTypeIcon
import com.example.util.simpletimetracker.feature_base_adapter.recordType.RecordTypeViewData
import com.example.util.simpletimetracker.domain.model.RecordType
import javax.inject.Inject

class RecordTypeViewDataMapper @Inject constructor(
    private val iconMapper: IconMapper,
    private val colorMapper: ColorMapper,
    private val resourceRepo: ResourceRepo,
    private val recordTypeCardSizeMapper: RecordTypeCardSizeMapper
) {

    fun mapToEmpty(): List<ViewHolderType> {
        return EmptyViewData(
            message = resourceRepo.getString(R.string.record_types_empty)
        ).let(::listOf)
    }

    fun map(
        recordType: RecordType,
        isDarkTheme: Boolean
    ): RecordTypeViewData {
        return RecordTypeViewData(
            id = recordType.id,
            name = recordType.name,
            iconId = iconMapper.mapIcon(recordType.icon),
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
            iconId = iconMapper.mapIcon(recordType.icon),
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
                iconColor = colorMapper.toFilteredIconColor(isDarkTheme),
                iconAlpha = if (default.iconId is RecordTypeIcon.Emoji) {
                    FILTERED_ICON_EMOJI_ALPHA
                } else {
                    DEFAULT_ICON_EMOJI_ALPHA
                }
            )
        } else {
            default
        }
    }

    @ColorInt private fun mapColor(color: AppColor, isDarkTheme: Boolean): Int {
        return colorMapper.mapToColorInt(color, isDarkTheme)
    }

    companion object {
        private const val DEFAULT_ICON_EMOJI_ALPHA = 1.0f
        private const val FILTERED_ICON_EMOJI_ALPHA = 0.3f
    }
}