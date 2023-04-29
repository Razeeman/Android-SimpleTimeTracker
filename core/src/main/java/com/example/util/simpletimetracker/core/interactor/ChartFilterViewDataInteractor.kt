package com.example.util.simpletimetracker.core.interactor

import com.example.util.simpletimetracker.core.mapper.ChartFilterViewDataMapper
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.model.Category
import com.example.util.simpletimetracker.domain.model.RecordTag
import com.example.util.simpletimetracker.domain.model.RecordType
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import javax.inject.Inject

class ChartFilterViewDataInteractor @Inject constructor(
    private val chartFilterViewDataMapper: ChartFilterViewDataMapper,
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
                chartFilterViewDataMapper
                    .mapRecordType(type, typeIdsFiltered, numberOfCards, isDarkTheme)
            }
            .takeUnless { it.isEmpty() }
            ?.plus(
                chartFilterViewDataMapper
                    .mapToUntrackedItem(typeIdsFiltered, numberOfCards, isDarkTheme)
            )
            ?: chartFilterViewDataMapper.mapTypesEmpty()
    }

    suspend fun loadCategoriesViewData(
        categories: List<Category>,
        categoryIdsFiltered: List<Long>,
    ): List<ViewHolderType> {
        val isDarkTheme = prefsInteractor.getDarkMode()

        return categories
            .map { category ->
                chartFilterViewDataMapper
                    .mapCategory(category, categoryIdsFiltered, isDarkTheme)
            }
            .takeUnless { it.isEmpty() }
            ?.plus(
                chartFilterViewDataMapper
                    .mapToCategoryUntrackedItem(categoryIdsFiltered, isDarkTheme)
            )
            ?.plus(
                chartFilterViewDataMapper
                    .mapToUncategorizedItem(categoryIdsFiltered, isDarkTheme)
            )
            ?: chartFilterViewDataMapper.mapCategoriesEmpty()
    }

    suspend fun loadTagsViewData(
        tags: List<RecordTag>,
        types: Map<Long, RecordType>,
        recordTagsFiltered: List<Long>,
    ): List<ViewHolderType> {
        val isDarkTheme = prefsInteractor.getDarkMode()

        return tags
            .map { tag ->
                chartFilterViewDataMapper
                    .mapTag(tag, types[tag.typeId], recordTagsFiltered, isDarkTheme)
            }
            .takeUnless { it.isEmpty() }
            ?.plus(
                chartFilterViewDataMapper
                    .mapToTagUntrackedItem(recordTagsFiltered, isDarkTheme)
            )
            ?.plus(
                chartFilterViewDataMapper
                    .mapToUntaggedItem(recordTagsFiltered, isDarkTheme)
            )
            ?: chartFilterViewDataMapper.mapTagsEmpty()
    }
}