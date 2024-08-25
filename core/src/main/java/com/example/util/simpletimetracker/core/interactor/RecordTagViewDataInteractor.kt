package com.example.util.simpletimetracker.core.interactor

import com.example.util.simpletimetracker.core.mapper.CategoryViewDataMapper
import com.example.util.simpletimetracker.core.mapper.CommonViewDataMapper
import com.example.util.simpletimetracker.domain.interactor.GetSelectableTagsInteractor
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTagInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.divider.DividerViewData
import com.example.util.simpletimetracker.feature_base_adapter.info.InfoViewData
import javax.inject.Inject

class RecordTagViewDataInteractor @Inject constructor(
    private val prefsInteractor: PrefsInteractor,
    private val recordTypeInteractor: RecordTypeInteractor,
    private val recordTagInteractor: RecordTagInteractor,
    private val getSelectableTagsInteractor: GetSelectableTagsInteractor,
    private val categoryViewDataMapper: CategoryViewDataMapper,
    private val commonViewDataMapper: CommonViewDataMapper,
) {

    suspend fun getViewData(
        selectedTags: List<Long>,
        typeId: Long,
        multipleChoiceAvailable: Boolean,
        showAddButton: Boolean,
        showArchived: Boolean,
        showUntaggedButton: Boolean,
    ): Result {
        val isDarkTheme = prefsInteractor.getDarkMode()
        val recordTags = getSelectableTagsInteractor.execute(typeId)
        val types = recordTypeInteractor.getAll().associateBy { it.id }
        val data = recordTags
            .let { tags -> if (showArchived) tags else tags.filterNot { it.archived } }

        return if (data.isNotEmpty()) {
            val selected = data.filter { it.id in selectedTags }
            val available = data.filter { it.id !in selectedTags }

            val viewData = mutableListOf<ViewHolderType>()

            listOf(
                categoryViewDataMapper.mapToRecordTagHint(),
                DividerViewData(1),
            ).takeIf { showAddButton }?.let(viewData::addAll)

            commonViewDataMapper.mapSelectedHint(
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

            categoryViewDataMapper.groupToTagGroups(available).forEach { (groupName, tags) ->
                if (groupName.isNotEmpty()) {
                    viewData.add(InfoViewData(text = groupName))
                }

                tags.map {
                    categoryViewDataMapper.mapRecordTag(
                        tag = it,
                        type = types[it.iconColorSource],
                        isDarkTheme = isDarkTheme,
                    )
                }.let(viewData::addAll)
            }

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

            Result(
                selectedCount = selected.size,
                data = viewData,
            )
        } else {
            Result(
                selectedCount = 0,
                data = listOfNotNull(
                    if (showAddButton && recordTagInteractor.isEmpty()) {
                        categoryViewDataMapper.mapToTagsFirstHint()
                    } else {
                        categoryViewDataMapper.mapToRecordTagsEmpty()
                    },
                    categoryViewDataMapper.mapToRecordTagAddItem(isDarkTheme)
                        .takeIf { showAddButton },
                ),
            )
        }
    }

    data class Result(
        val selectedCount: Int,
        val data: List<ViewHolderType>,
    )
}