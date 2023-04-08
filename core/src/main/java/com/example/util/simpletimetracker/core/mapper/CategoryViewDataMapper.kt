package com.example.util.simpletimetracker.core.mapper

import com.example.util.simpletimetracker.core.R
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.model.AppColor
import com.example.util.simpletimetracker.domain.model.Category
import com.example.util.simpletimetracker.domain.model.RecordTag
import com.example.util.simpletimetracker.domain.model.RecordType
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.category.CategoryViewData
import com.example.util.simpletimetracker.feature_base_adapter.empty.EmptyViewData
import com.example.util.simpletimetracker.feature_base_adapter.info.InfoViewData
import javax.inject.Inject

class CategoryViewDataMapper @Inject constructor(
    private val colorMapper: ColorMapper,
    private val iconMapper: IconMapper,
    private val resourceRepo: ResourceRepo,
) {

    fun mapCategory(
        category: Category,
        isDarkTheme: Boolean,
        isFiltered: Boolean = false,
    ): CategoryViewData.Category {
        return CategoryViewData.Category(
            id = category.id,
            name = category.name,
            iconColor = getTextColor(isDarkTheme, isFiltered),
            color = getColor(category.color, isDarkTheme, isFiltered)
        )
    }

    fun mapRecordTag(
        tag: RecordTag,
        type: RecordType?,
        isDarkTheme: Boolean,
        isFiltered: Boolean = false,
        showIcon: Boolean = true,
    ): CategoryViewData.Record {
        val isTyped = tag.typeId != 0L
        val icon = type?.icon?.let(iconMapper::mapIcon).takeIf { isTyped }
        val color = type?.color.takeIf { isTyped } ?: tag.color

        return CategoryViewData.Record.Tagged(
            id = tag.id,
            name = tag.name,
            iconColor = getTextColor(isDarkTheme, isFiltered),
            iconAlpha = colorMapper.toIconAlpha(icon, isFiltered),
            color = getColor(color, isDarkTheme, isFiltered),
            icon = if (showIcon) icon else null
        )
    }

    fun mapRecordTagUntagged(
        type: RecordType,
        isDarkTheme: Boolean,
        isFiltered: Boolean
    ): CategoryViewData.Record.Untagged {
        val icon = type.icon.let(iconMapper::mapIcon)

        return CategoryViewData.Record.Untagged(
            typeId = type.id,
            name = R.string.change_record_untagged.let(resourceRepo::getString),
            iconColor = getTextColor(isDarkTheme, isFiltered),
            iconAlpha = colorMapper.toIconAlpha(icon, isFiltered),
            color = if (isFiltered) {
                colorMapper.toFilteredColor(isDarkTheme)
            } else {
                colorMapper.toUntrackedColor(isDarkTheme)
            },
            icon = icon
        )
    }

    fun mapToRecordTagsEmpty(): ViewHolderType {
        return EmptyViewData(
            message = resourceRepo.getString(R.string.change_record_categories_empty)
        )
    }

    fun mapToCategoriesEmpty(): ViewHolderType {
        return EmptyViewData(
            message = resourceRepo.getString(R.string.change_record_type_categories_empty)
        )
    }

    fun mapSelectedCategoriesHint(isEmpty: Boolean): ViewHolderType {
        return InfoViewData(
            text = if (isEmpty) {
                R.string.nothing_selected
            } else {
                R.string.something_selected
            }.let(resourceRepo::getString)
        )
    }

    fun getTextColor(
        isDarkTheme: Boolean,
        isFiltered: Boolean,
    ): Int {
        return if (isFiltered) {
            colorMapper.toFilteredIconColor(isDarkTheme)
        } else {
            colorMapper.toIconColor(isDarkTheme)
        }
    }

    private fun getColor(
        color: AppColor,
        isDarkTheme: Boolean,
        isFiltered: Boolean,
    ): Int {
        return if (isFiltered) {
            colorMapper.toFilteredColor(isDarkTheme)
        } else {
            color.let { colorMapper.mapToColorInt(it, isDarkTheme) }
        }
    }
}