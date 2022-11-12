package com.example.util.simpletimetracker.core.mapper

import com.example.util.simpletimetracker.core.R
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.hint.HintViewData
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.feature_base_adapter.category.TagType
import com.example.util.simpletimetracker.feature_base_adapter.category.CategoryAddViewData
import javax.inject.Inject

class CategoriesViewDataMapper @Inject constructor(
    private val colorMapper: ColorMapper,
    private val resourceRepo: ResourceRepo
) {

    fun mapToTypeTagHint(): ViewHolderType = HintViewData(
        text = R.string.categories_record_type_hint
            .let(resourceRepo::getString)
    )

    fun mapToRecordTagHint(): ViewHolderType = HintViewData(
        text = R.string.categories_record_hint
            .let(resourceRepo::getString)
    )

    fun mapToTypeTagAddItem(isDarkTheme: Boolean): CategoryAddViewData =
        map(type = TagType.RECORD_TYPE, isDarkTheme = isDarkTheme)

    fun mapToRecordTagAddItem(isDarkTheme: Boolean): CategoryAddViewData =
        map(type = TagType.RECORD, isDarkTheme = isDarkTheme)

    private fun map(type: TagType, isDarkTheme: Boolean): CategoryAddViewData {
        val name = when (type) {
            TagType.RECORD_TYPE -> R.string.categories_add_category
            TagType.RECORD -> R.string.categories_add_record_tag
        }.let(resourceRepo::getString)

        return CategoryAddViewData(
            type = type,
            name = name,
            color = colorMapper.toInactiveColor(isDarkTheme)
        )
    }
}