package com.example.util.simpletimetracker.core.interactor

import com.example.util.simpletimetracker.core.R
import com.example.util.simpletimetracker.core.mapper.CategoryViewDataMapper
import com.example.util.simpletimetracker.core.mapper.CommonViewDataMapper
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.extension.plusAssign
import com.example.util.simpletimetracker.domain.prefs.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.record.model.RecordBase
import com.example.util.simpletimetracker.domain.recordTag.interactor.GetSelectableTagsInteractor
import com.example.util.simpletimetracker.domain.recordTag.interactor.RecordTagInteractor
import com.example.util.simpletimetracker.domain.recordTag.model.RecordTag
import com.example.util.simpletimetracker.domain.recordType.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.divider.DividerViewData
import com.example.util.simpletimetracker.feature_base_adapter.hint.HintViewData
import com.example.util.simpletimetracker.feature_base_adapter.info.InfoViewData
import javax.inject.Inject

class RecordTagViewDataInteractor @Inject constructor(
    private val resourceRepo: ResourceRepo,
    private val prefsInteractor: PrefsInteractor,
    private val recordTypeInteractor: RecordTypeInteractor,
    private val recordTagInteractor: RecordTagInteractor,
    private val getSelectableTagsInteractor: GetSelectableTagsInteractor,
    private val categoryViewDataMapper: CategoryViewDataMapper,
    private val commonViewDataMapper: CommonViewDataMapper,
) {

    // typeId is empty - show all tags.
    suspend fun getViewData(
        selectedTags: List<RecordBase.Tag>,
        typeIds: List<Long>,
        showAllTags: Boolean,
        multipleChoiceAvailable: Boolean,
        showAddButton: Boolean,
        showArchived: Boolean,
        showUntaggedButton: Boolean,
        showAllTagsButton: Boolean,
    ): Result {
        fun List<RecordTag>.filterArchived(): List<RecordTag> {
            return if (showArchived) this else this.filterNot { it.archived }
        }

        val isDarkTheme = prefsInteractor.getDarkMode()
        val allTags = recordTagInteractor.getAll().filterArchived()
        val recordTags = getSelectableTagsInteractor.execute(*typeIds.toLongArray()).filterArchived()
        val recordTagIds = recordTags.map { it.id }
        val tagsFromOtherActivities = allTags.filter { it.id !in recordTagIds }
        val hasMoreTags = tagsFromOtherActivities.isNotEmpty()
        val types = recordTypeInteractor.getAll().associateBy { it.id }

        return if (allTags.isNotEmpty()) {
            val selectedTagsMap = selectedTags.associateBy { it.tagId }
            val selectedTagIds = selectedTagsMap.keys
            val selected = allTags.filter { it.id in selectedTagIds }
            val available = recordTags.filter { it.id !in selectedTagIds }
            val availableFromOtherActivities = if (showAllTags) {
                tagsFromOtherActivities.filter { it.id !in selectedTagIds }
            } else {
                emptyList()
            }

            val viewData = mutableListOf<ViewHolderType>()
            val buttonsViewData = mutableListOf<ViewHolderType>()

            if (showAddButton) {
                viewData += listOf(
                    categoryViewDataMapper.mapToRecordTagHint(),
                    DividerViewData("divider_hint".hashCode().toLong()),
                )
            }

            if (multipleChoiceAvailable) {
                viewData += commonViewDataMapper.mapSelectedHint(
                    isEmpty = selected.isEmpty(),
                )
            }

            viewData += selected.map {
                categoryViewDataMapper.mapRecordTagWithValue(
                    tag = it,
                    tagData = selectedTagsMap[it.id],
                    types = types,
                    isDarkTheme = isDarkTheme,
                )
            }

            if (multipleChoiceAvailable && available.isNotEmpty()) {
                viewData += DividerViewData("divider_available".hashCode().toLong())
            }

            categoryViewDataMapper.groupToTagGroups(available).forEach { (groupName, tags) ->
                if (groupName.isNotEmpty()) {
                    viewData += InfoViewData(text = groupName)
                }

                viewData += tags.map {
                    categoryViewDataMapper.mapRecordTag(
                        tag = it,
                        types = types,
                        isDarkTheme = isDarkTheme,
                    )
                }
            }

            if (availableFromOtherActivities.isNotEmpty()) {
                viewData += DividerViewData("divider_from_other".hashCode().toLong())

                viewData += HintViewData(
                    text = resourceRepo.getString(R.string.change_record_tag_from_other_activity),
                )

                viewData += availableFromOtherActivities.map {
                    categoryViewDataMapper.mapRecordTag(
                        tag = it,
                        types = types,
                        isDarkTheme = isDarkTheme,
                    )
                }
            }

            if (showUntaggedButton) {
                buttonsViewData += categoryViewDataMapper.mapToUntaggedItem(
                    isDarkTheme = isDarkTheme,
                    isFiltered = false,
                )
            }

            if (showAllTagsButton && !showAllTags && hasMoreTags) {
                buttonsViewData += categoryViewDataMapper.mapToRecordTagShowAllItem(
                    isDarkTheme = isDarkTheme,
                )
            }

            if (showAddButton) {
                buttonsViewData += categoryViewDataMapper.mapToRecordTagAddItem(isDarkTheme)
            }

            if (buttonsViewData.isNotEmpty()) {
                if (multipleChoiceAvailable || availableFromOtherActivities.isNotEmpty()) {
                    viewData += DividerViewData("divider_buttons".hashCode().toLong())
                }
                viewData += buttonsViewData
            }

            Result(
                selectedCount = selected.size,
                data = viewData,
            )
        } else {
            val viewData = mutableListOf<ViewHolderType>()
            viewData += if (showAddButton && recordTagInteractor.isEmpty()) {
                categoryViewDataMapper.mapToTagsFirstHint()
            } else {
                categoryViewDataMapper.mapToRecordTagsEmpty()
            }
            if (showAddButton) {
                viewData += categoryViewDataMapper.mapToRecordTagAddItem(isDarkTheme)
            }
            Result(
                selectedCount = 0,
                data = viewData,
            )
        }
    }

    data class Result(
        val selectedCount: Int,
        val data: List<ViewHolderType>,
    )
}