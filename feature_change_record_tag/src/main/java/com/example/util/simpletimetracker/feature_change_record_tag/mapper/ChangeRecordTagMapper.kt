package com.example.util.simpletimetracker.feature_change_record_tag.mapper

import com.example.util.simpletimetracker.core.R
import com.example.util.simpletimetracker.core.adapter.category.CategoryViewData
import com.example.util.simpletimetracker.core.mapper.CategoryViewDataMapper
import com.example.util.simpletimetracker.core.mapper.ColorMapper
import com.example.util.simpletimetracker.core.viewData.RecordTypeIcon
import com.example.util.simpletimetracker.domain.model.RecordTag
import javax.inject.Inject

class ChangeRecordTagMapper @Inject constructor(
    private val colorMapper: ColorMapper,
    private val mapper: CategoryViewDataMapper
) {

    fun mapRecordTagUntyped(
        tag: RecordTag,
        isDarkTheme: Boolean
    ): CategoryViewData.Record {
        return CategoryViewData.Record.Tagged(
            id = 0L,
            name = tag.name,
            iconColor = mapper.getTextColor(isDarkTheme, false),
            color = colorMapper.toUntrackedColor(isDarkTheme),
            icon = RecordTypeIcon.Image(R.drawable.unknown)
        )
    }
}