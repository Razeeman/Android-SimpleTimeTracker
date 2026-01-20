package com.example.util.simpletimetracker.feature_archive.interactor

import com.example.util.simpletimetracker.core.mapper.CategoryViewDataMapper
import com.example.util.simpletimetracker.core.mapper.RecordTypeViewDataMapper
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.prefs.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.recordTag.interactor.RecordTagInteractor
import com.example.util.simpletimetracker.domain.recordTag.model.RecordTag
import com.example.util.simpletimetracker.domain.recordType.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.domain.recordType.model.RecordType
import com.example.util.simpletimetracker.feature_archive.R
import com.example.util.simpletimetracker.feature_archive.viewData.ArchiveViewData
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.divider.DividerViewData
import com.example.util.simpletimetracker.feature_base_adapter.emptySpace.EmptySpaceViewData
import com.example.util.simpletimetracker.feature_base_adapter.hint.HintViewData
import com.example.util.simpletimetracker.feature_views.GoalCheckmarkView
import javax.inject.Inject

class ArchiveViewDataInteractor @Inject constructor(
    private val resourceRepo: ResourceRepo,
    // interactors
    private val prefsInteractor: PrefsInteractor,
    private val recordTypeInteractor: RecordTypeInteractor,
    private val recordTagInteractor: RecordTagInteractor,
    // mappers
    private val recordTypeViewDataMapper: RecordTypeViewDataMapper,
    private val categoryViewDataMapper: CategoryViewDataMapper,
) {

    suspend fun getHintViewData(): Boolean {
        val archivedTypes = recordTypeInteractor.getAll().filter { it.hidden }
        val archivedRecordTags = recordTagInteractor.getAll().filter { it.archived }

        return archivedTypes.isNotEmpty() || archivedRecordTags.isNotEmpty()
    }

    suspend fun getViewData(
        navBarHeightDp: Int,
        searchEnabled: Boolean,
        searchText: String,
    ): ArchiveViewData {
        val result: MutableList<ViewHolderType> = mutableListOf()
        val numberOfCards = prefsInteractor.getNumberOfCards()
        val isDarkTheme = prefsInteractor.getDarkMode()
        val isSearching: Boolean = searchEnabled && searchText.isNotEmpty()

        val types = recordTypeInteractor.getAll().associateBy { it.id }
        val archivedTypes = types.values.filter { it.hidden }
        val archivedRecordTags = recordTagInteractor.getAll().filter { it.archived }

        if (archivedTypes.isNotEmpty()) {
            val typesViewData = searchTypes(
                types = archivedTypes,
                isSearching = isSearching,
                searchText = searchText,
            ).map { type ->
                recordTypeViewDataMapper.mapFiltered(
                    recordType = type,
                    numberOfCards = numberOfCards,
                    isDarkTheme = isDarkTheme,
                    isFiltered = false,
                    checkState = GoalCheckmarkView.CheckState.HIDDEN,
                    isComplete = false,
                )
            }
            result += HintViewData(resourceRepo.getString(R.string.activity_hint))
            result += if (typesViewData.isEmpty() && isSearching) {
                mapSearchEmpty()
            } else {
                typesViewData
            }
        }

        if (archivedRecordTags.isNotEmpty()) {
            val recordTagsViewData = searchTags(
                tags = archivedRecordTags,
                isSearching = isSearching,
                searchText = searchText,
            ).map { tag ->
                categoryViewDataMapper.mapRecordTag(
                    tag = tag,
                    types = types,
                    isDarkTheme = isDarkTheme,
                    isFiltered = false,
                )
            }
            if (archivedTypes.isNotEmpty()) result += DividerViewData(1)
            result += HintViewData(resourceRepo.getString(R.string.record_tag_hint))
            result += if (recordTagsViewData.isEmpty() && isSearching) {
                mapSearchEmpty()
            } else {
                recordTagsViewData
            }
        }

        if (result.isEmpty()) {
            result += HintViewData(resourceRepo.getString(R.string.archive_empty))
        }

        result += getBottomEmptySpace(navBarHeightDp)

        return ArchiveViewData(result)
    }

    private fun getBottomEmptySpace(
        navBarHeightDp: Int,
    ): ViewHolderType {
        val optionsButtonHeight = resourceRepo.getDimenInDp(R.dimen.button_height)
        val size = optionsButtonHeight + navBarHeightDp
        return EmptySpaceViewData(
            id = "archive_bottom_space".hashCode().toLong(),
            height = EmptySpaceViewData.ViewDimension.ExactSizeDp(size),
            wrapBefore = true,
        )
    }

    private fun mapSearchEmpty(): List<ViewHolderType> {
        return HintViewData(text = resourceRepo.getString(R.string.widget_load_error))
            .let(::listOf)
    }

    private fun searchTypes(
        types: List<RecordType>,
        isSearching: Boolean,
        searchText: String,
    ): List<RecordType> {
        return if (isSearching) {
            types.filter { it.name.lowercase().contains(searchText) }
        } else {
            types
        }
    }

    private fun searchTags(
        tags: List<RecordTag>,
        isSearching: Boolean,
        searchText: String,
    ): List<RecordTag> {
        return if (isSearching) {
            tags.filter { it.name.lowercase().contains(searchText) }
        } else {
            tags
        }
    }
}