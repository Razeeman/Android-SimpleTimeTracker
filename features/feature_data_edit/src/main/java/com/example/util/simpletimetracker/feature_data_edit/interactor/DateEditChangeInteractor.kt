package com.example.util.simpletimetracker.feature_data_edit.interactor

import com.example.util.simpletimetracker.core.interactor.RecordFilterInteractor
import com.example.util.simpletimetracker.domain.interactor.AddRecordMediator
import com.example.util.simpletimetracker.domain.interactor.NotificationGoalTimeInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTagInteractor
import com.example.util.simpletimetracker.domain.model.RecordsFilter
import com.example.util.simpletimetracker.feature_data_edit.model.DataEditChangeActivityState
import com.example.util.simpletimetracker.feature_data_edit.model.DataEditChangeCommentState
import javax.inject.Inject

class DateEditChangeInteractor @Inject constructor(
    private val recordInteractor: RecordInteractor,
    private val addRecordMediator: AddRecordMediator,
    private val recordFilterInteractor: RecordFilterInteractor,
    private val recordTagInteractor: RecordTagInteractor,
    private val notificationGoalTimeInteractor: NotificationGoalTimeInteractor,
) {

    suspend fun changeData(
        typeState: DataEditChangeActivityState,
        commentState: DataEditChangeCommentState,
        filters: List<RecordsFilter>
    ) {
        if (filters.isEmpty()) return
        val newTypeId = (typeState as? DataEditChangeActivityState.Enabled)?.viewData?.id
        val newComment = (commentState as? DataEditChangeCommentState.Enabled)?.viewData
        if (newTypeId == null && newComment == null) return

        val records = recordFilterInteractor.getByFilter(filters)
        val tags = recordTagInteractor.getAll().associateBy { it.id }
        val oldTypeIds = mutableSetOf<Long>()
        var newTagIds: List<Long>? = null

        records.forEach { record ->
            if (newTypeId != null && record.typeId != newTypeId) {
                // Remove all typed tags.
                newTagIds = record.tagIds.filter { tagId ->
                    tags[tagId]?.typeId == 0L
                }
                // Save old typeId before change to update data later.
                oldTypeIds.add(record.typeId)
            }

            // Change activity
            record.copy(
                typeId = newTypeId ?: record.typeId,
                comment = newComment ?: record.comment,
                tagIds = newTagIds ?: record.tagIds
            ).let {
                recordInteractor.add(it)
            }
        }

        // Check goal time and statistics widget consistency.
        if (newTypeId != null) {
            oldTypeIds.forEach { typeId ->
                notificationGoalTimeInteractor.checkAndReschedule(typeId)
            }
            addRecordMediator.doAfterAdd(newTypeId)
        }
    }
}