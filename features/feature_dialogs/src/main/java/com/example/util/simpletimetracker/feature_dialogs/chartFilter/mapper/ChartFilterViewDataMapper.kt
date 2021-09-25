package com.example.util.simpletimetracker.feature_dialogs.chartFilter.mapper

import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.category.CategoryViewData
import com.example.util.simpletimetracker.feature_base_adapter.empty.EmptyViewData
import com.example.util.simpletimetracker.core.mapper.CategoryViewDataMapper
import com.example.util.simpletimetracker.core.mapper.ColorMapper
import com.example.util.simpletimetracker.core.mapper.RecordTypeCardSizeMapper
import com.example.util.simpletimetracker.core.mapper.RecordTypeViewDataMapper
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.core.utils.UNTRACKED_ITEM_ID
import com.example.util.simpletimetracker.feature_views.viewData.RecordTypeIcon
import com.example.util.simpletimetracker.feature_base_adapter.recordType.RecordTypeViewData
import com.example.util.simpletimetracker.domain.model.Category
import com.example.util.simpletimetracker.domain.model.ChartFilterType
import com.example.util.simpletimetracker.domain.model.RecordType
import com.example.util.simpletimetracker.feature_dialogs.R
import com.example.util.simpletimetracker.feature_dialogs.chartFilter.viewData.ChartFilterTypeViewData
import javax.inject.Inject

class ChartFilterViewDataMapper @Inject constructor(
    private val colorMapper: ColorMapper,
    private val resourceRepo: ResourceRepo,
    private val recordTypeViewDataMapper: RecordTypeViewDataMapper,
    private val recordTypeCardSizeMapper: RecordTypeCardSizeMapper,
    private val categoryViewDataMapper: CategoryViewDataMapper
) {

    fun mapRecordType(
        recordType: RecordType,
        typeIdsFiltered: List<Long>,
        numberOfCards: Int,
        isDarkTheme: Boolean
    ): RecordTypeViewData {
        return recordTypeViewDataMapper.mapFiltered(
            recordType,
            numberOfCards,
            isDarkTheme,
            recordType.id in typeIdsFiltered
        )
    }

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
            iconColor = if (UNTRACKED_ITEM_ID in typeIdsFiltered) {
                colorMapper.toFilteredIconColor(isDarkTheme)
            } else {
                colorMapper.toIconColor(isDarkTheme)
            },
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

    fun mapCategory(
        category: Category,
        categoryIdsFiltered: List<Long>,
        isDarkTheme: Boolean
    ): CategoryViewData {
        return categoryViewDataMapper.mapActivityTag(
            category = category,
            isDarkTheme = isDarkTheme,
            isFiltered = category.id in categoryIdsFiltered
        )
    }

    fun mapCategoriesEmpty(): List<EmptyViewData> {
        return EmptyViewData(
            message = R.string.chart_filter_categories_empty.let(resourceRepo::getString)
        ).let(::listOf)
    }

    fun mapToFilterTypeViewData(filterType: ChartFilterType): List<ViewHolderType> {
        return listOf(
            ChartFilterType.ACTIVITY,
            ChartFilterType.CATEGORY
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
            ChartFilterType.ACTIVITY -> R.string.chart_filter_type_activity
            ChartFilterType.CATEGORY -> R.string.chart_filter_type_category
        }.let(resourceRepo::getString)
    }
}