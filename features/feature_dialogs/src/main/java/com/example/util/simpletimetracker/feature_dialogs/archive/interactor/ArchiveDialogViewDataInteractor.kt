package com.example.util.simpletimetracker.feature_dialogs.archive.interactor

import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.core.mapper.CategoryViewDataMapper
import com.example.util.simpletimetracker.core.mapper.RecordTypeViewDataMapper
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.prefs.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.record.interactor.RecordInteractor
import com.example.util.simpletimetracker.domain.recordTag.interactor.RecordTagInteractor
import com.example.util.simpletimetracker.domain.recordTag.interactor.RecordToRecordTagInteractor
import com.example.util.simpletimetracker.domain.recordType.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.feature_dialogs.R
import com.example.util.simpletimetracker.feature_dialogs.archive.viewData.ArchiveDialogButtonsViewData
import com.example.util.simpletimetracker.feature_dialogs.archive.viewData.ArchiveDialogInfoViewData
import com.example.util.simpletimetracker.feature_dialogs.archive.viewData.ArchiveDialogTitleViewData
import com.example.util.simpletimetracker.feature_views.GoalCheckmarkView
import javax.inject.Inject

class ArchiveDialogViewDataInteractor @Inject constructor(
    private val resourceRepo: ResourceRepo,
    private val recordTypeInteractor: RecordTypeInteractor,
    private val recordTagInteractor: RecordTagInteractor,
    private val recordInteractor: RecordInteractor,
    private val recordToRecordTagInteractor: RecordToRecordTagInteractor,
    private val prefsInteractor: PrefsInteractor,
    private val recordTypeViewDataMapper: RecordTypeViewDataMapper,
    private val categoryViewDataMapper: CategoryViewDataMapper,
) {

    suspend fun getActivityViewData(typeId: Long): List<ViewHolderType> {
        val numberOfCards: Int = prefsInteractor.getNumberOfCards()
        val isDarkTheme = prefsInteractor.getDarkMode()
        val type = recordTypeInteractor.get(typeId) ?: return emptyList()

        val item = recordTypeViewDataMapper.map(
            recordType = type,
            numberOfCards = numberOfCards,
            isDarkTheme = isDarkTheme,
            checkState = GoalCheckmarkView.CheckState.HIDDEN,
            isComplete = false,
        )
        val recordsCount = recordInteractor.getByType(listOf(typeId)).size

        return mutableListOf<ViewHolderType>().apply {
            item.let(::add)

            resourceRepo.getString(R.string.archive_activity_deletion_message)
                .let(::ArchiveDialogTitleViewData).let(::add)

            ArchiveDialogInfoViewData(
                name = resourceRepo.getString(R.string.archive_records_count),
                text = recordsCount.toString(),
            ).let(::add)

            ArchiveDialogButtonsViewData.let(::add)
        }
    }

    suspend fun getRecordTagViewData(tagId: Long): List<ViewHolderType> {
        val isDarkTheme = prefsInteractor.getDarkMode()
        val tag = recordTagInteractor.get(tagId) ?: return emptyList()
        val types = recordTypeInteractor.getAll().associateBy { it.id }

        val item = categoryViewDataMapper.mapRecordTag(
            tag = tag,
            types = types,
            isDarkTheme = isDarkTheme,
        )
        val recordsCount = recordToRecordTagInteractor.getRecordIdsByTagId(tagId).size

        return mutableListOf<ViewHolderType>().apply {
            item.let(::add)

            resourceRepo.getString(R.string.archive_tag_deletion_message)
                .let(::ArchiveDialogTitleViewData).let(::add)

            ArchiveDialogInfoViewData(
                name = resourceRepo.getString(R.string.archive_tagged_records_count),
                text = recordsCount.toString(),
            ).let(::add)

            ArchiveDialogButtonsViewData.let(::add)
        }
    }
}