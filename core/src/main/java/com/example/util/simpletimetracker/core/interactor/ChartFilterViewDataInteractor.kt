package com.example.util.simpletimetracker.core.interactor

import com.example.util.simpletimetracker.core.mapper.CategoryViewDataMapper
import com.example.util.simpletimetracker.core.mapper.ChartFilterViewDataMapper
import com.example.util.simpletimetracker.core.mapper.RecordTypeViewDataMapper
import com.example.util.simpletimetracker.domain.base.UNCATEGORIZED_ITEM_ID
import com.example.util.simpletimetracker.domain.base.UNTRACKED_ITEM_ID
import com.example.util.simpletimetracker.domain.category.model.Category
import com.example.util.simpletimetracker.domain.extension.plus
import com.example.util.simpletimetracker.domain.prefs.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.recordTag.model.RecordTag
import com.example.util.simpletimetracker.domain.recordType.model.RecordType
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_views.GoalCheckmarkView
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
        isArchivedShown: Boolean,
    ): List<ViewHolderType> {
        val numberOfCards = prefsInteractor.getNumberOfCards()
        val isDarkTheme = prefsInteractor.getDarkMode()
        val showUntracked = prefsInteractor.getShowUntrackedInStatistics()
        val showArchived = types.any { it.hidden }
        val untrackedItem = if (showUntracked) {
            chartFilterViewDataMapper.mapToUntrackedItem(
                typeIdsFiltered = typeIdsFiltered,
                numberOfCards = numberOfCards,
                isDarkTheme = isDarkTheme,
            )
        } else {
            null
        }
        val archivedItem = if (showArchived) {
            recordTypeViewDataMapper.mapToArchivedItem(
                isEnabled = isArchivedShown,
                numberOfCards = numberOfCards,
                isDarkTheme = isDarkTheme,
            )
        } else {
            null
        }

        return types
            .mapNotNull { type ->
                if (!isArchivedShown && type.hidden) {
                    return@mapNotNull null
                }
                recordTypeViewDataMapper.mapFiltered(
                    recordType = type,
                    isFiltered = type.id in typeIdsFiltered,
                    numberOfCards = numberOfCards,
                    isDarkTheme = isDarkTheme,
                    checkState = GoalCheckmarkView.CheckState.HIDDEN,
                    isComplete = false,
                )
            }
            .takeUnless { types.isEmpty() }
            ?.plus(untrackedItem)
            ?.plus(archivedItem)
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
        val untaggedItem = categoryViewDataMapper.mapToUncategorizedItem(
            isFiltered = UNCATEGORIZED_ITEM_ID in categoryIdsFiltered,
            isDarkTheme = isDarkTheme,
        )

        return categories
            .map { category ->
                categoryViewDataMapper.mapCategory(
                    category = category,
                    isDarkTheme = isDarkTheme,
                    isFiltered = category.id in categoryIdsFiltered,
                )
            }
            .takeUnless { categories.isEmpty() }
            ?.plus(untrackedItem)
            ?.plus(untaggedItem)
            ?: categoryViewDataMapper.mapToCategoriesEmpty().let(::listOf)
    }

    suspend fun loadTagsViewData(
        tags: List<RecordTag>,
        types: List<RecordType>,
        recordTagsFiltered: List<Long>,
        isArchivedShown: Boolean,
    ): List<ViewHolderType> {
        val typesMap = types.associateBy(RecordType::id)
        val isDarkTheme = prefsInteractor.getDarkMode()
        val showUntracked = prefsInteractor.getShowUntrackedInStatistics()
        val showArchived = tags.any { it.archived }
        val untrackedItem = if (showUntracked) {
            categoryViewDataMapper.mapToTagUntrackedItem(
                isFiltered = UNTRACKED_ITEM_ID in recordTagsFiltered,
                isDarkTheme = isDarkTheme,
            )
        } else {
            null
        }
        val untaggedItem = categoryViewDataMapper.mapToUntaggedItem(
            isFiltered = UNCATEGORIZED_ITEM_ID in recordTagsFiltered,
            isDarkTheme = isDarkTheme,
        )
        val archivedItem = if (showArchived) {
            categoryViewDataMapper.mapToTagArchiveItem(
                isEnabled = isArchivedShown,
                isDarkTheme = isDarkTheme,
            )
        } else {
            null
        }

        return tags
            .mapNotNull { tag ->
                if (!isArchivedShown && tag.archived) {
                    return@mapNotNull null
                }
                categoryViewDataMapper.mapRecordTag(
                    tag = tag,
                    types = typesMap,
                    isDarkTheme = isDarkTheme,
                    isFiltered = tag.id in recordTagsFiltered,
                )
            }
            .takeUnless { tags.isEmpty() }
            ?.plus(untrackedItem)
            ?.plus(untaggedItem)
            ?.plus(archivedItem)
            ?: categoryViewDataMapper.mapToRecordTagsEmpty().let(::listOf)
    }
}