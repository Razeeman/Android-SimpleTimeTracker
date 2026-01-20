package com.example.util.simpletimetracker.feature_data_edit.interactor

import com.example.util.simpletimetracker.core.interactor.RecordFilterInteractor
import com.example.util.simpletimetracker.domain.record.interactor.AddRecordMediator
import com.example.util.simpletimetracker.domain.backup.interactor.BackupInteractor
import com.example.util.simpletimetracker.domain.backup.interactor.ClearDataInteractor
import com.example.util.simpletimetracker.domain.recordTag.interactor.FilterSelectableTagsInteractor
import com.example.util.simpletimetracker.domain.record.interactor.RecordInteractor
import com.example.util.simpletimetracker.domain.recordTag.interactor.RecordTypeToTagInteractor
import com.example.util.simpletimetracker.domain.record.interactor.RemoveRecordMediator
import com.example.util.simpletimetracker.domain.notifications.interactor.UpdateExternalViewsInteractor
import com.example.util.simpletimetracker.domain.record.model.Record
import com.example.util.simpletimetracker.domain.record.model.RecordBase
import com.example.util.simpletimetracker.domain.record.model.RecordsFilter
import com.example.util.simpletimetracker.feature_base_adapter.category.CategoryViewData
import com.example.util.simpletimetracker.feature_data_edit.model.DataEditAddTagsState
import com.example.util.simpletimetracker.feature_data_edit.model.DataEditChangeActivityState
import com.example.util.simpletimetracker.feature_data_edit.model.DataEditChangeCommentState
import com.example.util.simpletimetracker.feature_data_edit.model.DataEditDeleteRecordsState
import com.example.util.simpletimetracker.feature_data_edit.model.DataEditRemoveTagsState
import javax.inject.Inject

class DateEditChangeInteractor @Inject constructor(
    private val recordInteractor: RecordInteractor,
    private val addRecordMediator: AddRecordMediator,
    private val removeRecordMediator: RemoveRecordMediator,
    private val recordFilterInteractor: RecordFilterInteractor,
    private val recordTypeToTagInteractor: RecordTypeToTagInteractor,
    private val filterSelectableTagsInteractor: FilterSelectableTagsInteractor,
    private val clearDataInteractor: ClearDataInteractor,
    private val backupInteractor: BackupInteractor,
    private val externalViewsInteractor: UpdateExternalViewsInteractor,
) {

    suspend fun changeData(
        typeState: DataEditChangeActivityState,
        commentState: DataEditChangeCommentState,
        addTagState: DataEditAddTagsState,
        removeTagState: DataEditRemoveTagsState,
        deleteRecordsState: DataEditDeleteRecordsState,
        filters: List<RecordsFilter>,
    ) {
        if (filters.isEmpty()) return

        val newTypeId = (typeState as? DataEditChangeActivityState.Enabled)
            ?.viewData?.id
        val newComment = (commentState as? DataEditChangeCommentState.Enabled)
            ?.viewData
        val addTags = (addTagState as? DataEditAddTagsState.Enabled)
            ?.tags
        val removeTags = (removeTagState as? DataEditRemoveTagsState.Enabled)
            ?.viewData?.map(CategoryViewData.Record::id)
        val deleteRecord = deleteRecordsState is DataEditDeleteRecordsState.Enabled

        if (
            newTypeId == null &&
            newComment == null &&
            addTags == null &&
            removeTags == null &&
            !deleteRecord
        ) {
            return
        }

        val records = recordFilterInteractor.getByFilter(filters)
            .filterIsInstance<Record>()
        val typesToTags = recordTypeToTagInteractor.getAll()
        val removedTypeIds = mutableSetOf<Long>()
        val changedTypeIds = mutableSetOf<Long>()
        val removedTagIds = mutableSetOf<Long>()
        val changedTagIds = mutableSetOf<Long>()

        records.forEach { record ->
            if (deleteRecord) {
                removedTypeIds.add(record.typeId)
                removedTagIds.addAll(record.tags.map(RecordBase.Tag::tagId))
                recordInteractor.remove(record.id)
                return@forEach
            }

            val finalTypeId = newTypeId ?: record.typeId
            val finalComment = newComment ?: record.comment
            val finalTags: List<RecordBase.Tag> = record.tags
                .plus(addTags.orEmpty())
                .filter { it.tagId !in removeTags.orEmpty() }
                .let { tags ->
                    if (finalTypeId != record.typeId) {
                        val filteredIds = filterSelectableTagsInteractor.execute(
                            tagIds = tags.map { it.tagId },
                            typesToTags = typesToTags,
                            typeIds = listOf(finalTypeId),
                        )
                        tags.filter { it.tagId in filteredIds }
                    } else {
                        tags
                    }
                }
                .distinctBy { it.tagId }

            // Save old typeId before change to update data later.
            if (finalTypeId != record.typeId) {
                changedTypeIds.add(record.typeId)
            }
            val recordTagIds = record.tags.map(RecordBase.Tag::tagId)
            val finalTagIds = finalTags.map(RecordBase.Tag::tagId)
            recordTagIds
                .filter { it !in finalTagIds }
                .let(changedTagIds::addAll)
            finalTagIds
                .filter { it !in recordTagIds }
                .let(changedTagIds::addAll)

            // Change record
            recordInteractor.update(
                recordId = record.id,
                typeId = finalTypeId,
                comment = finalComment,
                tags = finalTags,
            )
        }

        if (deleteRecord) {
            removeRecordMediator.doAfterRemove(
                typeIds = removedTypeIds.toList(),
                tagIds = removedTagIds.toList(),
            )
        }
        // Check goal time and statistics widget consistency.
        if (newTypeId != null) {
            externalViewsInteractor.onRecordsChangeType(changedTypeIds)
        }
        if (newTypeId != null || changedTagIds.isNotEmpty()) {
            addRecordMediator.doAfterAdd(
                typeIds = listOfNotNull(newTypeId),
                tagIds = changedTagIds.toList(),
            )
        }
    }

    suspend fun deleteTodayRecords(ids: List<Long>) {
        ids.forEach { recordInteractor.remove(it) }
        backupInteractor.doAfterRestore()
    }

    suspend fun deleteAllRecords() {
        recordInteractor.removeAll()
        backupInteractor.doAfterRestore()
    }

    suspend fun deleteAllData() {
        clearDataInteractor.execute()
        backupInteractor.doAfterRestore()
    }
}