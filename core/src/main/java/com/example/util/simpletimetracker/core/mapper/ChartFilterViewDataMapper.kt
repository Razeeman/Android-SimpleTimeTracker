package com.example.util.simpletimetracker.core.mapper

import com.example.util.simpletimetracker.core.R
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.core.viewData.ChartFilterTypeViewData
import com.example.util.simpletimetracker.domain.UNCATEGORIZED_ITEM_ID
import com.example.util.simpletimetracker.domain.UNTRACKED_ITEM_ID
import com.example.util.simpletimetracker.domain.model.Category
import com.example.util.simpletimetracker.domain.model.ChartFilterType
import com.example.util.simpletimetracker.domain.model.RecordTag
import com.example.util.simpletimetracker.domain.model.RecordType
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.category.CategoryViewData
import com.example.util.simpletimetracker.feature_base_adapter.recordType.RecordTypeViewData
import com.example.util.simpletimetracker.feature_views.viewData.RecordTypeIcon
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
            recordType = recordType,
            numberOfCards = numberOfCards,
            isDarkTheme = isDarkTheme,
            isFiltered = recordType.id in typeIdsFiltered
        )
    }

    fun mapTypesEmpty(): List<ViewHolderType> {
        return recordTypeViewDataMapper.mapToEmpty()
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

    fun mapCategory(
        category: Category,
        categoryIdsFiltered: List<Long>,
        isDarkTheme: Boolean
    ): CategoryViewData {
        return categoryViewDataMapper.mapCategory(
            category = category,
            isDarkTheme = isDarkTheme,
            isFiltered = category.id in categoryIdsFiltered
        )
    }

    fun mapCategoriesEmpty(): List<ViewHolderType> {
        return categoryViewDataMapper.mapToCategoriesEmpty().let(::listOf)
    }

    fun mapToCategoryUntrackedItem(
        categoryIdsFiltered: List<Long>,
        isDarkTheme: Boolean
    ): CategoryViewData {
        return CategoryViewData.Category(
            id = UNTRACKED_ITEM_ID,
            name = R.string.untracked_time_name
                .let(resourceRepo::getString),
            iconColor = categoryViewDataMapper.getTextColor(
                isDarkTheme = isDarkTheme,
                isFiltered = UNTRACKED_ITEM_ID in categoryIdsFiltered
            ),
            color = if (UNTRACKED_ITEM_ID in categoryIdsFiltered) {
                colorMapper.toFilteredColor(isDarkTheme)
            } else {
                colorMapper.toUntrackedColor(isDarkTheme)
            },
        )
    }

    fun mapToUncategorizedItem(
        categoryIdsFiltered: List<Long>,
        isDarkTheme: Boolean
    ): CategoryViewData {
        return CategoryViewData.Category(
            id = UNCATEGORIZED_ITEM_ID,
            name = R.string.uncategorized_time_name
                .let(resourceRepo::getString),
            iconColor = categoryViewDataMapper.getTextColor(
                isDarkTheme = isDarkTheme,
                isFiltered = UNCATEGORIZED_ITEM_ID in categoryIdsFiltered
            ),
            color = if (UNCATEGORIZED_ITEM_ID in categoryIdsFiltered) {
                colorMapper.toFilteredColor(isDarkTheme)
            } else {
                colorMapper.toUntrackedColor(isDarkTheme)
            },
        )
    }

    fun mapTag(
        tag: RecordTag,
        type: RecordType?,
        tagIdsFiltered: List<Long>,
        isDarkTheme: Boolean
    ): CategoryViewData {
        return categoryViewDataMapper.mapRecordTag(
            tag = tag,
            type = type,
            isDarkTheme = isDarkTheme,
            isFiltered = tag.id in tagIdsFiltered,
        )
    }

    fun mapTagsEmpty(): List<ViewHolderType> {
        return categoryViewDataMapper.mapToRecordTagsEmpty().let(::listOf)
    }

    fun mapToTagUntrackedItem(
        tagIdsFiltered: List<Long>,
        isDarkTheme: Boolean
    ): CategoryViewData {
        return CategoryViewData.Record.Tagged(
            id = UNTRACKED_ITEM_ID,
            name = R.string.untracked_time_name
                .let(resourceRepo::getString),
            icon = RecordTypeIcon.Image(R.drawable.unknown),
            iconColor = categoryViewDataMapper.getTextColor(
                isDarkTheme = isDarkTheme,
                isFiltered = UNTRACKED_ITEM_ID in tagIdsFiltered
            ),
            color = if (UNTRACKED_ITEM_ID in tagIdsFiltered) {
                colorMapper.toFilteredColor(isDarkTheme)
            } else {
                colorMapper.toUntrackedColor(isDarkTheme)
            },
        )
    }

    fun mapToUntaggedItem(
        tagIdsFiltered: List<Long>,
        isDarkTheme: Boolean
    ): CategoryViewData {
        return CategoryViewData.Record.Tagged(
            id = UNCATEGORIZED_ITEM_ID,
            name = R.string.change_record_untagged
                .let(resourceRepo::getString),
            icon = RecordTypeIcon.Image(R.drawable.untagged),
            iconColor = categoryViewDataMapper.getTextColor(
                isDarkTheme = isDarkTheme,
                isFiltered = UNCATEGORIZED_ITEM_ID in tagIdsFiltered
            ),
            color = if (UNCATEGORIZED_ITEM_ID in tagIdsFiltered) {
                colorMapper.toFilteredColor(isDarkTheme)
            } else {
                colorMapper.toUntrackedColor(isDarkTheme)
            },
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