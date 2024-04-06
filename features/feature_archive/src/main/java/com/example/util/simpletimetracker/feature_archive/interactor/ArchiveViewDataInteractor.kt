package com.example.util.simpletimetracker.feature_archive.interactor

import com.example.util.simpletimetracker.core.mapper.CategoryViewDataMapper
import com.example.util.simpletimetracker.core.mapper.RecordTypeViewDataMapper
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTagInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.feature_archive.R
import com.example.util.simpletimetracker.feature_archive.viewData.ArchiveViewData
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.divider.DividerViewData
import com.example.util.simpletimetracker.feature_base_adapter.hint.HintViewData
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

    suspend fun getViewData(): ArchiveViewData {
        val result: MutableList<ViewHolderType> = mutableListOf()
        val numberOfCards = prefsInteractor.getNumberOfCards()
        val isDarkTheme = prefsInteractor.getDarkMode()

        val types = recordTypeInteractor.getAll().associateBy { it.id }
        val archivedTypes = types.values.filter { it.hidden }
        val archivedRecordTags = recordTagInteractor.getAll().filter { it.archived }

        val typesViewData = archivedTypes.map { type ->
            recordTypeViewDataMapper.mapFiltered(
                recordType = type,
                numberOfCards = numberOfCards,
                isDarkTheme = isDarkTheme,
                isFiltered = false,
                isChecked = null,
            )
        }

        val recordTagsViewData = archivedRecordTags.map { tag ->
            categoryViewDataMapper.mapRecordTag(
                tag = tag,
                type = types[tag.iconColorSource],
                isDarkTheme = isDarkTheme,
                isFiltered = false,
            )
        }

        if (typesViewData.isNotEmpty()) {
            HintViewData(resourceRepo.getString(R.string.activity_hint)).let(result::add)
            typesViewData.let(result::addAll)
        }

        if (recordTagsViewData.isNotEmpty()) {
            if (typesViewData.isNotEmpty()) {
                DividerViewData(1).let(result::add)
            }
            HintViewData(resourceRepo.getString(R.string.record_tag_hint)).let(result::add)
            recordTagsViewData.let(result::addAll)
        }

        if (result.isEmpty()) {
            HintViewData(resourceRepo.getString(R.string.archive_empty)).let(result::add)
        }

        val showHint = archivedTypes.isNotEmpty() ||
            archivedRecordTags.isNotEmpty()

        return ArchiveViewData(
            items = result,
            showHint = showHint,
        )
    }
}