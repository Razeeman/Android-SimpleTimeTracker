package com.example.util.simpletimetracker.core.interactor

import com.example.util.simpletimetracker.core.mapper.CategoryViewDataMapper
import com.example.util.simpletimetracker.domain.interactor.GetSelectableTagsInteractor
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
    private val getSelectableTagsInteractor: GetSelectableTagsInteractor,
    private val categoryViewDataMapper: CategoryViewDataMapper,
) {

    suspend fun getViewData(
        selectedTags: List<Long>,
        typeId: Long,
        multipleChoiceAvailable: Boolean,
        showAddButton: Boolean,
        showArchived: Boolean,
        showUntaggedButton: Boolean,
    ): List<ViewHolderType> {
        val isDarkTheme = prefsInteractor.getDarkMode()
        val recordTags = getSelectableTagsInteractor.execute(typeId)
        val types = recordTypeInteractor.getAll().associateBy { it.id }

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

                listOf(
                    categoryViewDataMapper.mapToRecordTagHint(),
                    DividerViewData(1),
                ).takeIf { showAddButton }?.let(viewData::addAll)

                categoryViewDataMapper.mapSelectedCategoriesHint(
                    isEmpty = selected.isEmpty(),
                ).takeIf { multipleChoiceAvailable }?.let(viewData::add)

                selected.map {
                    categoryViewDataMapper.mapRecordTag(
                        tag = it,
                        type = types[it.iconColorSource],
                        isDarkTheme = isDarkTheme,
                    )
                }.let(viewData::addAll)

                DividerViewData(2)
                    .takeUnless { available.isEmpty() }
                    .takeIf { multipleChoiceAvailable }
                    ?.let(viewData::add)

                available.map {
                    categoryViewDataMapper.mapRecordTag(
                        tag = it,
                        type = types[it.iconColorSource],
                        isDarkTheme = isDarkTheme,
                    )
                }.let(viewData::addAll)

                if (showUntaggedButton) {
                    if (selected.isNotEmpty() || available.isNotEmpty()) {
                        DividerViewData(3)
                            .takeIf { multipleChoiceAvailable }
                            ?.let(viewData::add)
                        categoryViewDataMapper.mapToUntaggedItem(
                            isDarkTheme = isDarkTheme,
                            isFiltered = false,
                        ).let(viewData::add)
                    }
                }

                categoryViewDataMapper.mapToRecordTagAddItem(isDarkTheme)
                    .takeIf { showAddButton }
                    ?.let(viewData::add)

                viewData
            }
            ?: listOfNotNull(
                if (showAddButton && recordTagInteractor.isEmpty()) {
                    categoryViewDataMapper.mapToTagsFirstHint()
                } else {
                    categoryViewDataMapper.mapToRecordTagsEmpty()
                },
                categoryViewDataMapper.mapToRecordTagAddItem(isDarkTheme)
                    .takeIf { showAddButton },
            )
    }
}