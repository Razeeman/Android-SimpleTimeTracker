package com.example.util.simpletimetracker.feature_data_edit.interactor

import com.example.util.simpletimetracker.core.interactor.RecordFilterInteractor
import com.example.util.simpletimetracker.domain.interactor.AddRecordMediator
import com.example.util.simpletimetracker.domain.interactor.RecordInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTagInteractor
import com.example.util.simpletimetracker.domain.model.RecordsFilter
import javax.inject.Inject

class DateEditChangeInteractor @Inject constructor(
    private val recordInteractor: RecordInteractor,
    private val addRecordMediator: AddRecordMediator,
    private val recordFilterInteractor: RecordFilterInteractor,
    private val recordTagInteractor: RecordTagInteractor,
) {

    suspend fun changeData(
        newTypeId: Long?,
        filters: List<RecordsFilter>
    ) {
        if (filters.isEmpty()) return
        if (newTypeId == null) return

        val records = recordFilterInteractor.getByFilter(filters)
        val tags = recordTagInteractor.getAll().associateBy { it.id }

        records.forEach { record ->
            // Remove all typed tags.
            val newTagIds = record.tagIds.filter { tagId -> tags[tagId]?.typeId == 0L }
            // Change activity
            record.copy(
                typeId = newTypeId,
                tagIds = newTagIds
            ).let {
                recordInteractor.add(it)
            }
            addRecordMediator.doAfterAdd(newTypeId)
        }
    }
}