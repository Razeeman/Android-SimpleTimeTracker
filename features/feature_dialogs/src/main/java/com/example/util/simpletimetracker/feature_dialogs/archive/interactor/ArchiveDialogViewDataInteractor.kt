package com.example.util.simpletimetracker.feature_dialogs.archive.interactor

import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.core.mapper.CategoryViewDataMapper
import com.example.util.simpletimetracker.core.mapper.RecordTypeViewDataMapper
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTagInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordToRecordTagInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.feature_dialogs.R
import com.example.util.simpletimetracker.feature_dialogs.archive.viewData.ArchiveDialogButtonsViewData
import com.example.util.simpletimetracker.feature_dialogs.archive.viewData.ArchiveDialogInfoViewData
import com.example.util.simpletimetracker.feature_dialogs.archive.viewData.ArchiveDialogTitleViewData
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
            isChecked = null,
        )
        val recordsCount = recordInteractor.getByType(listOf(typeId)).size
        val recordTagCount = recordTagInteractor.getByType(typeId).size

        return mutableListOf<ViewHolderType>().apply {
            item.let(::add)

            resourceRepo.getString(R.string.archive_activity_deletion_message)
                .let(::ArchiveDialogTitleViewData).let(::add)

            ArchiveDialogInfoViewData(
                name = resourceRepo.getString(R.string.archive_records_count),
                text = recordsCount.toString(),
            ).let(::add)

            ArchiveDialogInfoViewData(
                name = resourceRepo.getString(R.string.archive_record_tags_count),
                text = recordTagCount.toString(),
            ).let(::add)

            ArchiveDialogButtonsViewData.let(::add)
        }
    }

    suspend fun getRecordTagViewData(tagId: Long): List<ViewHolderType> {
        val isDarkTheme = prefsInteractor.getDarkMode()
        val tag = recordTagInteractor.get(tagId) ?: return emptyList()
        val type = recordTypeInteractor.get(tag.typeId)

        val item = categoryViewDataMapper.mapRecordTag(
            tag = tag,
            type = type,
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