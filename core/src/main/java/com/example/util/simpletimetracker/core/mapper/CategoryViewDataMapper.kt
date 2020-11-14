package com.example.util.simpletimetracker.core.mapper

import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.core.viewData.CategoryViewData
import com.example.util.simpletimetracker.domain.model.Category
import javax.inject.Inject

class CategoryViewDataMapper @Inject constructor(
    private val colorMapper: ColorMapper,
    private val resourceRepo: ResourceRepo
) {

    fun map(category: Category, isDarkTheme: Boolean): CategoryViewData {
        return CategoryViewData(
            id = category.id,
            name = category.name,
            textColor = colorMapper.toIconColor(isDarkTheme),
            color = category.color
                .let { colorMapper.mapToColorResId(it, isDarkTheme) }
                .let(resourceRepo::getColor)
        )
    }

    fun mapFiltered(
        category: Category,
        isDarkTheme: Boolean,
        isFiltered: Boolean
    ): CategoryViewData {
        val default = map(category, isDarkTheme)

        return if (isFiltered) {
            default.copy(
                color = colorMapper.toFilteredColor(isDarkTheme),
                textColor = colorMapper.toFilteredIconColor(isDarkTheme)
            )
        } else {
            default
        }
    }
}