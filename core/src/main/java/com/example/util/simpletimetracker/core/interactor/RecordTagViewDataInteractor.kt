package com.example.util.simpletimetracker.core.interactor

import com.example.util.simpletimetracker.core.mapper.CategoriesViewDataMapper
import com.example.util.simpletimetracker.core.mapper.CategoryViewDataMapper
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTagInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.divider.DividerViewData
import javax.inject.Inject

class RecordTagViewDataInteractor @Inject constructor(
    private val prefsInteractor: PrefsInteractor,
    private val recordTypeInteractor: RecordTypeInteractor,
    private val recordTagInteractor: RecordTagInteractor,
    private val categoryViewDataMapper: CategoryViewDataMapper,
    private val categoriesViewDataMapper: CategoriesViewDataMapper,
) {

    suspend fun getViewData(
        selectedTags: List<Long>,
        typeId: Long,
        multipleChoiceAvailable: Boolean,
        showHint: Boolean,
        showAddButton: Boolean,
        showArchived: Boolean,
        showUntaggedButton: Boolean,
    ): List<ViewHolderType> {
        val isDarkTheme = prefsInteractor.getDarkMode()
        val type = recordTypeInteractor.get(typeId)
        val recordTags = if (typeId != 0L) {
            recordTagInteractor.getByType(typeId)
        } else {
            emptyList()
        } + recordTagInteractor.getUntyped()

        return recordTags
            .let { tags -> if (showArchived) tags else tags.filterNot { it.archived } }
            .takeUnless { it.isEmpty() }
            ?.let { tags ->
                val selected = tags.filter { it.id in selectedTags }
                val available = tags.filter { it.id !in selectedTags }
                selected to available
            }
            ?.let { (selected, available) ->
                val viewData = mutableListOf<ViewHolderType>()

                categoriesViewDataMapper.mapToRecordTagHint()
                    .takeIf { showHint }?.let(viewData::add)

                categoryViewDataMapper.mapSelectedCategoriesHint(
                    isEmpty = selected.isEmpty()
                ).takeIf { multipleChoiceAvailable }?.let(viewData::add)

                selected.map {
                    categoryViewDataMapper.mapRecordTag(
                        tag = it,
                        type = type,
                        isDarkTheme = isDarkTheme
                    )
                }.let(viewData::addAll)

                DividerViewData(1)
                    .takeUnless { available.isEmpty() }
                    .takeIf { multipleChoiceAvailable }
                    ?.let(viewData::add)

                available.map {
                    categoryViewDataMapper.mapRecordTag(
                        tag = it,
                        type = type,
                        isDarkTheme = isDarkTheme
                    )
                }.let(viewData::addAll)

                if (showUntaggedButton) {
                    if (selected.isNotEmpty() || available.isNotEmpty()) {
                        DividerViewData(2)
                            .takeIf { multipleChoiceAvailable }
                            ?.let(viewData::add)
                        categoryViewDataMapper.mapToUntaggedItem(
                            isDarkTheme = isDarkTheme,
                            isFiltered = false,
                        ).let(viewData::add)
                    }
                }

                categoriesViewDataMapper.mapToRecordTagAddItem(isDarkTheme)
                    .takeIf { showAddButton }
                    ?.let(viewData::add)

                viewData
            }
            ?: listOfNotNull(
                categoryViewDataMapper.mapToRecordTagsEmpty(),
                categoriesViewDataMapper.mapToRecordTagAddItem(isDarkTheme)
                    .takeIf { showAddButton }
            )
    }
}