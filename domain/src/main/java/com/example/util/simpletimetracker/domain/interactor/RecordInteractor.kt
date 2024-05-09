package com.example.util.simpletimetracker.domain.interactor

import com.example.util.simpletimetracker.domain.model.Range
import com.example.util.simpletimetracker.domain.model.Record
import com.example.util.simpletimetracker.domain.model.RunningRecord
import com.example.util.simpletimetracker.domain.repo.RecordRepo
import com.example.util.simpletimetracker.domain.repo.RecordToRecordTagRepo
import javax.inject.Inject

class RecordInteractor @Inject constructor(
    private val recordRepo: RecordRepo,
    private val recordToRecordTagRepo: RecordToRecordTagRepo,
) {

    suspend fun isEmpty(): Boolean {
        return recordRepo.isEmpty()
    }

    suspend fun getAll(): List<Record> {
        return recordRepo.getAll()
    }

    suspend fun getByType(typeIds: List<Long>): List<Record> {
        return recordRepo.getByType(typeIds)
    }

    suspend fun getByTypeWithAnyComment(typeIds: List<Long>): List<Record> {
        return recordRepo.getByTypeWithAnyComment(typeIds)
    }

    suspend fun searchComment(text: String): List<Record> {
        return recordRepo.searchComment(text)
    }

    suspend fun searchByTypeWithComment(typeIds: List<Long>, text: String): List<Record> {
        return recordRepo.searchByTypeWithComment(typeIds, text)
    }

    suspend fun searchAnyComments(): List<Record> {
        return recordRepo.searchAnyComments()
    }

    suspend fun get(id: Long): Record? {
        return recordRepo.get(id)
    }

    suspend fun getPrev(timeStarted: Long, limit: Long = 1): List<Record> {
        return recordRepo.getPrev(
            timeStarted = timeStarted,
            limit = limit,
        )
    }

    suspend fun getNext(timeEnded: Long): Record? {
        return recordRepo.getNext(timeEnded)
    }

    suspend fun getFromRange(range: Range): List<Record> {
        return recordRepo.getFromRange(range)
    }

    suspend fun getFromRangeByType(typeIds: List<Long>, range: Range): List<Record> {
        return recordRepo.getFromRangeByType(typeIds, range)
    }

    suspend fun addFromRunning(
        runningRecord: RunningRecord,
    ) {
        Record(
            typeId = runningRecord.id,
            timeStarted = runningRecord.timeStarted,
            timeEnded = System.currentTimeMillis(),
            comment = runningRecord.comment,
            tagIds = runningRecord.tagIds,
        ).let {
            add(it)
        }
    }

    suspend fun add(record: Record) {
        val recordId = recordRepo.add(record)
        updateTags(recordId, record.tagIds)
    }

    suspend fun update(
        recordId: Long,
        typeId: Long,
        comment: String,
        tagIds: List<Long>,
    ) {
        recordRepo.update(
            recordId = recordId,
            typeId = typeId,
            comment = comment,
        )
        updateTags(recordId, tagIds)
    }

    suspend fun remove(id: Long) {
        recordToRecordTagRepo.removeAllByRecordId(id)
        recordRepo.remove(id)
    }

    private suspend fun updateTags(
        recordId: Long,
        tagIds: List<Long>,
    ) {
        recordToRecordTagRepo.removeAllByRecordId(recordId)
        recordToRecordTagRepo.addRecordTags(recordId, tagIds)
    }
}