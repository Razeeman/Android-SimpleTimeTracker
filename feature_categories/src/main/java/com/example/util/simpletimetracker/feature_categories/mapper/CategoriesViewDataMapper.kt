package com.example.util.simpletimetracker.feature_categories.mapper

import com.example.util.simpletimetracker.core.mapper.ColorMapper
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.feature_categories.R
import com.example.util.simpletimetracker.feature_categories.viewData.CategoryAddViewData
import javax.inject.Inject

class CategoriesViewDataMapper @Inject constructor(
    private val colorMapper: ColorMapper,
    private val resourceRepo: ResourceRepo
) {

    fun mapToAddItem(isDarkTheme: Boolean): CategoryAddViewData {
        return CategoryAddViewData(
            name = R.string.categories_add.let(resourceRepo::getString),
            color = colorMapper.toInactiveColor(isDarkTheme)
        )
    }
}