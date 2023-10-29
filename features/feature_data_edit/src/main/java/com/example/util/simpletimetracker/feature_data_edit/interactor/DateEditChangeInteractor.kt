package com.example.util.simpletimetracker.feature_data_edit.interactor

import com.example.util.simpletimetracker.core.interactor.RecordFilterInteractor
import com.example.util.simpletimetracker.domain.interactor.AddRecordMediator
import com.example.util.simpletimetracker.domain.interactor.NotificationGoalTimeInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTagInteractor
import com.example.util.simpletimetracker.domain.interactor.RemoveRecordMediator
import com.example.util.simpletimetracker.domain.model.Record
import com.example.util.simpletimetracker.domain.model.RecordsFilter
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
    private val recordTagInteractor: RecordTagInteractor,
    private val notificationGoalTimeInteractor: NotificationGoalTimeInteractor,
) {

    suspend fun changeData(
        typeState: DataEditChangeActivityState,
        commentState: DataEditChangeCommentState,
        addTagState: DataEditAddTagsState,
        removeTagState: DataEditRemoveTagsState,
        deleteRecordsState: DataEditDeleteRecordsState,
        filters: List<RecordsFilter>
    ) {
        if (filters.isEmpty()) return

        val newTypeId = (typeState as? DataEditChangeActivityState.Enabled)
            ?.viewData?.id
        val newComment = (commentState as? DataEditChangeCommentState.Enabled)
            ?.viewData
        val addTags = (addTagState as? DataEditAddTagsState.Enabled)
            ?.viewData?.map(CategoryViewData.Record::id)
        val removeTags = (removeTagState as? DataEditRemoveTagsState.Enabled)
            ?.viewData?.map(CategoryViewData.Record::id)
        val deleteRecord = deleteRecordsState is DataEditDeleteRecordsState.Enabled

        if (
            newTypeId == null &&
            newComment == null &&
            addTags == null &&
            removeTags == null &&
            !deleteRecord
        ) return

        val records = recordFilterInteractor.getByFilter(filters)
            .filterIsInstance<Record>()
        val tags = recordTagInteractor.getAll().associateBy { it.id }
        val oldTypeIds = mutableSetOf<Long>()

        records.forEach { record ->
            if (deleteRecord) {
                oldTypeIds.add(record.typeId)
                recordInteractor.remove(record.id)
                return@forEach
            }

            val finalTypeId = newTypeId ?: record.typeId
            val finalComment = newComment ?: record.comment
            val finalTagIds: Set<Long> = record.tagIds
                .plus(addTags.orEmpty())
                .filter { it !in removeTags.orEmpty() }
                .filter { tagId ->
                    val tag = tags[tagId] ?: return@filter false
                    tag.typeId == 0L || tag.typeId == finalTypeId
                }
                .toSet()

            // Save old typeId before change to update data later.
            if (finalTypeId != record.typeId) {
                oldTypeIds.add(record.typeId)
            }

            // Change activity
            record.copy(
                typeId = finalTypeId,
                comment = finalComment,
                tagIds = finalTagIds.toList(),
            ).let {
                recordInteractor.add(it)
            }
        }

        if (deleteRecord) {
            oldTypeIds.forEach { typeId ->
                removeRecordMediator.doAfterRemove(typeId)
            }
        }
        // Check goal time and statistics widget consistency.
        if (newTypeId != null) {
            oldTypeIds.forEach { typeId ->
                notificationGoalTimeInteractor.checkAndReschedule(listOf(typeId))
            }
            addRecordMediator.doAfterAdd(newTypeId)
        }
    }
}