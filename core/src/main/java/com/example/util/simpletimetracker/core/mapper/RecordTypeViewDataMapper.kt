package com.example.util.simpletimetracker.core.mapper

import androidx.annotation.ColorInt
import androidx.annotation.StringRes
import com.example.util.simpletimetracker.core.R
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.model.AppColor
import com.example.util.simpletimetracker.domain.model.RecordType
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.empty.EmptyViewData
import com.example.util.simpletimetracker.feature_base_adapter.recordType.RecordTypeViewData
import com.example.util.simpletimetracker.feature_base_adapter.recordTypeSpecial.RunningRecordTypeSpecialViewData
import com.example.util.simpletimetracker.feature_views.viewData.RecordTypeIcon
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
                iconAlpha = colorMapper.toIconAlpha(default.iconId, true),
            )
        } else {
            default
        }
    }

    fun mapToAddItem(
        numberOfCards: Int,
        isDarkTheme: Boolean,
    ): RunningRecordTypeSpecialViewData {
        return mapToSpecial(
            type = RunningRecordTypeSpecialViewData.Type.Add,
            name = R.string.running_records_add_type,
            icon = RecordTypeIcon.Image(R.drawable.add),
            numberOfCards = numberOfCards,
            isDarkTheme = isDarkTheme,
        )
    }

    fun mapToAddDefaultItem(
        numberOfCards: Int,
        isDarkTheme: Boolean,
    ): RunningRecordTypeSpecialViewData {
        return mapToSpecial(
            type = RunningRecordTypeSpecialViewData.Type.Default,
            name = R.string.running_records_add_default,
            icon = RecordTypeIcon.Image(R.drawable.add),
            numberOfCards = numberOfCards,
            isDarkTheme = isDarkTheme,
        )
    }

    fun mapToRepeatItem(
        numberOfCards: Int,
        isDarkTheme: Boolean,
    ): RunningRecordTypeSpecialViewData {
        return mapToSpecial(
            type = RunningRecordTypeSpecialViewData.Type.Repeat,
            name = R.string.running_records_repeat,
            icon = RecordTypeIcon.Image(R.drawable.repeat),
            numberOfCards = numberOfCards,
            isDarkTheme = isDarkTheme,
        )
    }

    private fun mapToSpecial(
        type: RunningRecordTypeSpecialViewData.Type,
        @StringRes name: Int,
        icon: RecordTypeIcon,
        numberOfCards: Int,
        isDarkTheme: Boolean,
    ): RunningRecordTypeSpecialViewData {
        return RunningRecordTypeSpecialViewData(
            type = type,
            name = name.let(resourceRepo::getString),
            iconId = icon,
            color = colorMapper.toInactiveColor(isDarkTheme),
            width = recordTypeCardSizeMapper.toCardWidth(numberOfCards),
            height = recordTypeCardSizeMapper.toCardHeight(numberOfCards),
            asRow = recordTypeCardSizeMapper.toCardAsRow(numberOfCards),
        )
    }

    @ColorInt private fun mapColor(color: AppColor, isDarkTheme: Boolean): Int {
        return colorMapper.mapToColorInt(color, isDarkTheme)
    }
}