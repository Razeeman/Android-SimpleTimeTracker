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

        return types
            .map { type ->
                recordTypeViewDataMapper.mapFiltered(
                    recordType = type,
                    isFiltered = type.id in typeIdsFiltered,
                    numberOfCards = numberOfCards,
                    isDarkTheme = isDarkTheme
                )
            }
            .takeUnless { it.isEmpty() }
            ?.plus(
                chartFilterViewDataMapper.mapToUntrackedItem(
                    typeIdsFiltered = typeIdsFiltered,
                    numberOfCards = numberOfCards,
                    isDarkTheme = isDarkTheme
                )
            )
            ?: recordTypeViewDataMapper.mapToEmpty()
    }

    suspend fun loadCategoriesViewData(
        categories: List<Category>,
        categoryIdsFiltered: List<Long>,
    ): List<ViewHolderType> {
        val isDarkTheme = prefsInteractor.getDarkMode()

        return categories
            .map { category ->
                categoryViewDataMapper.mapCategory(
                    category = category,
                    isDarkTheme = isDarkTheme,
                    isFiltered = category.id in categoryIdsFiltered
                )
            }
            .takeUnless { it.isEmpty() }
            ?.plus(
                chartFilterViewDataMapper.mapToCategoryUntrackedItem(
                    isFiltered = UNTRACKED_ITEM_ID in categoryIdsFiltered,
                    isDarkTheme = isDarkTheme
                )
            )
            ?.plus(
                categoryViewDataMapper.mapToUncategorizedItem(
                    isFiltered = UNCATEGORIZED_ITEM_ID in categoryIdsFiltered,
                    isDarkTheme = isDarkTheme
                )
            )
            ?: categoryViewDataMapper.mapToCategoriesEmpty().let(::listOf)
    }

    suspend fun loadTagsViewData(
        tags: List<RecordTag>,
        types: List<RecordType>,
        recordTagsFiltered: List<Long>,
    ): List<ViewHolderType> {
        val isDarkTheme = prefsInteractor.getDarkMode()
        val typesMap = types.associateBy(RecordType::id)

        return tags
            .sortedBy { tag ->
                val type = types.firstOrNull { it.id == tag.typeId } ?: 0
                types.indexOf(type)
            }
            .map { tag ->
                categoryViewDataMapper.mapRecordTag(
                    tag = tag,
                    type = typesMap[tag.typeId],
                    isDarkTheme = tag.id in recordTagsFiltered,
                    isFiltered = isDarkTheme
                )
            }
            .takeUnless { it.isEmpty() }
            ?.plus(
                chartFilterViewDataMapper.mapToTagUntrackedItem(
                    isFiltered = UNTRACKED_ITEM_ID in recordTagsFiltered,
                    isDarkTheme = isDarkTheme
                )
            )
            ?.plus(
                categoryViewDataMapper.mapToUntaggedItem(
                    isFiltered = UNCATEGORIZED_ITEM_ID in recordTagsFiltered,
                    isDarkTheme = isDarkTheme
                )
            )
            ?: categoryViewDataMapper.mapToRecordTagsEmpty().let(::listOf)
    }
}