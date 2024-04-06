package com.example.util.simpletimetracker.core.interactor

import com.example.util.simpletimetracker.core.mapper.CategoryViewDataMapper
import com.example.util.simpletimetracker.core.mapper.ChartFilterViewDataMapper
import com.example.util.simpletimetracker.core.mapper.RecordTypeViewDataMapper
import com.example.util.simpletimetracker.domain.UNCATEGORIZED_ITEM_ID
import com.example.util.simpletimetracker.domain.UNTRACKED_ITEM_ID
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.model.Category
import com.example.util.simpletimetracker.domain.model.RecordTag
import com.example.util.simpletimetracker.domain.model.RecordType
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import javax.inject.Inject

class ChartFilterViewDataInteractor @Inject constructor(
    private val chartFilterViewDataMapper: ChartFilterViewDataMapper,
    private val categoryViewDataMapper: CategoryViewDataMapper,
    private val recordTypeViewDataMapper: RecordTypeViewDataMapper,
    private val prefsInteractor: PrefsInteractor,
) {

    suspend fun loadRecordTypesViewData(
        types: List<RecordType>,
        typeIdsFiltered: List<Long>,
    ): List<ViewHolderType> {
        val numberOfCards = prefsInteractor.getNumberOfCards()
        val isDarkTheme = prefsInteractor.getDarkMode()
        val showUntracked = prefsInteractor.getShowUntrackedInStatistics()
        val untrackedItem = if (showUntracked) {
            chartFilterViewDataMapper.mapToUntrackedItem(
                typeIdsFiltered = typeIdsFiltered,
                numberOfCards = numberOfCards,
                isDarkTheme = isDarkTheme,
            )
        } else {
            null
        }

        return types
            .map { type ->
                recordTypeViewDataMapper.mapFiltered(
                    recordType = type,
                    isFiltered = type.id in typeIdsFiltered,
                    numberOfCards = numberOfCards,
                    isDarkTheme = isDarkTheme,
                    isChecked = null,
                )
            }
            .takeUnless { it.isEmpty() }
            ?.run { if (untrackedItem != null) plus(untrackedItem) else this }
            ?: recordTypeViewDataMapper.mapToEmpty()
    }

    suspend fun loadCategoriesViewData(
        categories: List<Category>,
        categoryIdsFiltered: List<Long>,
    ): List<ViewHolderType> {
        val isDarkTheme = prefsInteractor.getDarkMode()
        val showUntracked = prefsInteractor.getShowUntrackedInStatistics()
        val untrackedItem = if (showUntracked) {
            categoryViewDataMapper.mapToCategoryUntrackedItem(
                isFiltered = UNTRACKED_ITEM_ID in categoryIdsFiltered,
                isDarkTheme = isDarkTheme,
            )
        } else {
            null
        }

        return categories
            .map { category ->
                categoryViewDataMapper.mapCategory(
                    category = category,
                    isDarkTheme = isDarkTheme,
                    isFiltered = category.id in categoryIdsFiltered,
                )
            }
            .takeUnless { it.isEmpty() }
            ?.run { if (untrackedItem != null) plus(untrackedItem) else this }
            ?.plus(
                categoryViewDataMapper.mapToUncategorizedItem(
                    isFiltered = UNCATEGORIZED_ITEM_ID in categoryIdsFiltered,
                    isDarkTheme = isDarkTheme,
                ),
            )
            ?: categoryViewDataMapper.mapToCategoriesEmpty().let(::listOf)
    }

    suspend fun loadTagsViewData(
        tags: List<RecordTag>,
        types: List<RecordType>,
        recordTagsFiltered: List<Long>,
    ): List<ViewHolderType> {
        val typesMap = types.associateBy(RecordType::id)
        val isDarkTheme = prefsInteractor.getDarkMode()
        val showUntracked = prefsInteractor.getShowUntrackedInStatistics()
        val untrackedItem = if (showUntracked) {
            categoryViewDataMapper.mapToTagUntrackedItem(
                isFiltered = UNTRACKED_ITEM_ID in recordTagsFiltered,
                isDarkTheme = isDarkTheme,
            )
        } else {
            null
        }

        return tags
            .map { tag ->
                categoryViewDataMapper.mapRecordTag(
                    tag = tag,
                    type = typesMap[tag.iconColorSource],
                    isDarkTheme = isDarkTheme,
                    isFiltered = tag.id in recordTagsFiltered,
                )
            }
            .takeUnless { it.isEmpty() }
            ?.run { if (untrackedItem != null) plus(untrackedItem) else this }
            ?.plus(
                categoryViewDataMapper.mapToUntaggedItem(
                    isFiltered = UNCATEGORIZED_ITEM_ID in recordTagsFiltered,
                    isDarkTheme = isDarkTheme,
                ),
            )
            ?: categoryViewDataMapper.mapToRecordTagsEmpty().let(::listOf)
    }
}