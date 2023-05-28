package com.example.util.simpletimetracker.core.mapper

import com.example.util.simpletimetracker.core.R
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.core.viewData.ChartFilterTypeViewData
import com.example.util.simpletimetracker.domain.UNTRACKED_ITEM_ID
import com.example.util.simpletimetracker.domain.model.ChartFilterType
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.recordType.RecordTypeViewData
import com.example.util.simpletimetracker.feature_views.viewData.RecordTypeIcon
import javax.inject.Inject

class ChartFilterViewDataMapper @Inject constructor(
    private val colorMapper: ColorMapper,
    private val resourceRepo: ResourceRepo,
    private val recordTypeCardSizeMapper: RecordTypeCardSizeMapper,
    private val categoryViewDataMapper: CategoryViewDataMapper
) {

    fun mapToUntrackedItem(
        typeIdsFiltered: List<Long>,
        numberOfCards: Int,
        isDarkTheme: Boolean
    ): RecordTypeViewData {
        return RecordTypeViewData(
            id = UNTRACKED_ITEM_ID,
            name = R.string.untracked_time_name
                .let(resourceRepo::getString),
            iconId = RecordTypeIcon.Image(R.drawable.unknown),
            iconColor = categoryViewDataMapper.getTextColor(
                isDarkTheme = isDarkTheme,
                isFiltered = UNTRACKED_ITEM_ID in typeIdsFiltered
            ),
            color = if (UNTRACKED_ITEM_ID in typeIdsFiltered) {
                colorMapper.toFilteredColor(isDarkTheme)
            } else {
                colorMapper.toUntrackedColor(isDarkTheme)
            },
            width = recordTypeCardSizeMapper.toCardWidth(numberOfCards),
            height = recordTypeCardSizeMapper.toCardHeight(numberOfCards),
            asRow = recordTypeCardSizeMapper.toCardAsRow(numberOfCards)
        )
    }

    fun mapToFilterTypeViewData(filterType: ChartFilterType): List<ViewHolderType> {
        return listOf(
            ChartFilterType.ACTIVITY,
            ChartFilterType.CATEGORY,
            ChartFilterType.RECORD_TAG,
        ).map {
            ChartFilterTypeViewData(
                filterType = it,
                name = mapToFilterTypeName(it),
                isSelected = it == filterType
            )
        }
    }

    private fun mapToFilterTypeName(filterType: ChartFilterType): String {
        return when (filterType) {
            ChartFilterType.ACTIVITY -> R.string.activity_hint
            ChartFilterType.CATEGORY -> R.string.category_hint
            ChartFilterType.RECORD_TAG -> R.string.record_tag_hint_short
        }.let(resourceRepo::getString)
    }
}