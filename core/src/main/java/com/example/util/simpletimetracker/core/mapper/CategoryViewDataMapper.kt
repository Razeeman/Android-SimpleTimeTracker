package com.example.util.simpletimetracker.core.mapper

import com.example.util.simpletimetracker.core.R
import com.example.util.simpletimetracker.core.adapter.ViewHolderType
import com.example.util.simpletimetracker.core.adapter.category.CategoryViewData
import com.example.util.simpletimetracker.core.adapter.empty.EmptyViewData
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.model.Category
import com.example.util.simpletimetracker.domain.model.RecordTag
import com.example.util.simpletimetracker.domain.model.RecordType
import com.example.util.simpletimetracker.domain.model.TagType
import javax.inject.Inject

class CategoryViewDataMapper @Inject constructor(
    private val colorMapper: ColorMapper,
    private val resourceRepo: ResourceRepo
) {

    fun mapActivityTag(
        category: Category,
        isDarkTheme: Boolean,
        isFiltered: Boolean = false
    ): CategoryViewData {
        return CategoryViewData(
            type = TagType.RECORD_TYPE,
            id = category.id,
            name = category.name,
            textColor = getTextColor(isDarkTheme, isFiltered),
            color = getColor(category.color, isDarkTheme, isFiltered)
        )
    }

    fun mapRecordTag(
        tag: RecordTag,
        type: RecordType,
        isDarkTheme: Boolean,
        isFiltered: Boolean = false
    ): CategoryViewData {
        return CategoryViewData(
            type = TagType.RECORD,
            id = tag.id,
            name = tag.name,
            textColor = getTextColor(isDarkTheme, isFiltered),
            color = getColor(type.color, isDarkTheme, isFiltered)
        )
    }

    fun mapRecordTagUntagged(
        isDarkTheme: Boolean
    ): CategoryViewData {
        return CategoryViewData(
            type = TagType.RECORD,
            id = 0L,
            name = R.string.change_record_untagged.let(resourceRepo::getString),
            textColor = getTextColor(isDarkTheme, false),
            color = colorMapper.toUntrackedColor(isDarkTheme)
        )
    }

    fun mapRecordTagUntyped(
        tag: RecordTag,
        isDarkTheme: Boolean
    ): CategoryViewData {
        return CategoryViewData(
            type = TagType.RECORD,
            id = 0L,
            name = tag.name,
            textColor = getTextColor(isDarkTheme, false),
            color = colorMapper.toUntrackedColor(isDarkTheme)
        )
    }

    fun mapToRecordTagsEmpty(): List<ViewHolderType> {
        return EmptyViewData(
            message = resourceRepo.getString(R.string.change_record_categories_empty)
        ).let(::listOf)
    }

    fun mapToTypeNotSelected(): List<ViewHolderType> {
        return EmptyViewData(
            message = resourceRepo.getString(R.string.change_record_activity_not_selected)
        ).let(::listOf)
    }

    private fun getTextColor(
        isDarkTheme: Boolean,
        isFiltered: Boolean
    ): Int {
        return if (isFiltered) {
            colorMapper.toFilteredIconColor(isDarkTheme)
        } else {
            colorMapper.toIconColor(isDarkTheme)
        }
    }

    private fun getColor(
        colorId: Int,
        isDarkTheme: Boolean,
        isFiltered: Boolean
    ): Int {
        return if (isFiltered) {
            colorMapper.toFilteredColor(isDarkTheme)
        } else {
            colorId
                .let { colorMapper.mapToColorResId(it, isDarkTheme) }
                .let(resourceRepo::getColor)
        }
    }
}