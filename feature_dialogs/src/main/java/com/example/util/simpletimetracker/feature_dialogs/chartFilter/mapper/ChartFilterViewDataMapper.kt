package com.example.util.simpletimetracker.feature_dialogs.chartFilter.mapper

import com.example.util.simpletimetracker.core.mapper.ColorMapper
import com.example.util.simpletimetracker.core.mapper.RecordTypeCardSizeMapper
import com.example.util.simpletimetracker.core.mapper.RecordTypeViewDataMapper
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.core.viewData.RecordTypeViewData
import com.example.util.simpletimetracker.domain.model.RecordType
import com.example.util.simpletimetracker.feature_dialogs.R
import javax.inject.Inject

class ChartFilterViewDataMapper @Inject constructor(
    private val colorMapper: ColorMapper,
    private val resourceRepo: ResourceRepo,
    private val recordTypeViewDataMapper: RecordTypeViewDataMapper,
    private val recordTypeCardSizeMapper: RecordTypeCardSizeMapper
) {

    fun map(
        recordType: RecordType,
        typeIdsFiltered: List<Long>,
        numberOfCards: Int
    ): RecordTypeViewData {
        return recordTypeViewDataMapper.map(recordType, numberOfCards).copy(
            color = if (recordType.id in typeIdsFiltered) {
                R.color.filtered_color
            } else {
                recordType.color.let(colorMapper::mapToColorResId)
            }.let(resourceRepo::getColor)
        )
    }

    fun mapToUntrackedItem(
        typeIdsFiltered: List<Long>,
        numberOfCards: Int
    ): RecordTypeViewData {
        return RecordTypeViewData(
            id = -1L,
            name = R.string.untracked_time_name
                .let(resourceRepo::getString),
            iconId = R.drawable.unknown,
            color = if (-1L in typeIdsFiltered) {
                R.color.filtered_color
            } else {
                R.color.untracked_time_color
            }.let(resourceRepo::getColor),
            width = recordTypeCardSizeMapper.toCardWidth(numberOfCards),
            height = recordTypeCardSizeMapper.toCardHeight(numberOfCards),
            asRow = recordTypeCardSizeMapper.toCardAsRow(numberOfCards)
        )
    }
}