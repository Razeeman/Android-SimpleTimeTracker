package com.example.util.simpletimetracker.feature_dialogs.typesFilter.mapper

import com.example.util.simpletimetracker.core.R
import com.example.util.simpletimetracker.feature_base_adapter.category.CategoryViewData
import com.example.util.simpletimetracker.core.mapper.CategoryViewDataMapper
import com.example.util.simpletimetracker.core.mapper.ColorMapper
import com.example.util.simpletimetracker.core.mapper.IconMapper
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.model.RecordType
import javax.inject.Inject

class TypesFilterMapper @Inject constructor(
    private val resourceRepo: ResourceRepo,
    private val colorMapper: ColorMapper,
    private val iconMapper: IconMapper,
    private val categoryViewDataMapper: CategoryViewDataMapper
) {

    fun mapRecordTagUntagged(
        type: RecordType,
        isDarkTheme: Boolean,
        isFiltered: Boolean
    ): CategoryViewData.Record.Untagged {
        val icon = type.icon.let(iconMapper::mapIcon)

        return CategoryViewData.Record.Untagged(
            typeId = type.id,
            name = R.string.change_record_untagged.let(resourceRepo::getString),
            iconColor = categoryViewDataMapper.getTextColor(isDarkTheme, isFiltered),
            iconAlpha = colorMapper.toIconAlpha(icon, isFiltered),
            color = if (isFiltered) {
                colorMapper.toFilteredColor(isDarkTheme)
            } else {
                colorMapper.toUntrackedColor(isDarkTheme)
            },
            icon = icon
        )
    }
}