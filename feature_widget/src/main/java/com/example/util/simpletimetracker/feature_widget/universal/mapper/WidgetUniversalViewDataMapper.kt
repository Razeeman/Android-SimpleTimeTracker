package com.example.util.simpletimetracker.feature_widget.universal.mapper

import com.example.util.simpletimetracker.core.adapter.ViewHolderType
import com.example.util.simpletimetracker.core.adapter.empty.EmptyViewData
import com.example.util.simpletimetracker.core.mapper.ColorMapper
import com.example.util.simpletimetracker.core.mapper.RecordTypeViewDataMapper
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.core.viewData.RecordTypeViewData
import com.example.util.simpletimetracker.domain.model.RecordType
import com.example.util.simpletimetracker.feature_widget.R
import javax.inject.Inject

class WidgetUniversalViewDataMapper @Inject constructor(
    private val colorMapper: ColorMapper,
    private val resourceRepo: ResourceRepo,
    private val recordTypeViewDataMapper: RecordTypeViewDataMapper
) {

    fun map(
        recordType: RecordType,
        isFiltered: Boolean,
        numberOfCards: Int
    ): RecordTypeViewData {
        return recordTypeViewDataMapper.map(recordType, numberOfCards).copy(
            color = if (isFiltered) {
                R.color.filtered_color
            } else {
                recordType.color.let(colorMapper::mapToColorResId)
            }.let(resourceRepo::getColor)
        )
    }

    fun mapToEmpty(): ViewHolderType {
        return EmptyViewData(
            message = R.string.widget_empty.let(resourceRepo::getString)
        )
    }
}