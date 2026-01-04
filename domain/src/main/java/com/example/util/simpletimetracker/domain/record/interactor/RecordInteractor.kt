package com.example.util.simpletimetracker.domain.record.interactor

import com.example.util.simpletimetracker.domain.record.model.Range
import com.example.util.simpletimetracker.domain.record.model.Record
import com.example.util.simpletimetracker.domain.record.model.RecordBase
import com.example.util.simpletimetracker.domain.record.repo.RecordRepo
import com.example.util.simpletimetracker.domain.record.model.RunningRecord
import com.example.util.simpletimetracker.domain.recordTag.repo.RecordToRecordTagRepo
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

    suspend fun getPrev(
        timeStarted: Long,
        ignoreTypeIds: List<Long> = emptyList(),
    ): Record? {
        return recordRepo.getPrev(timeStarted, ignoreTypeIds)
    }

    // Can return several records ended at the same time.
    suspend fun getAllPrev(timeStarted: Long): List<Record> {
        val prev = recordRepo.getPrev(timeStarted) ?: return emptyList()
        return recordRepo.getByTimeEnded(prev.timeEnded)
    }

    suspend fun getNext(timeEnded: Long): Record? {
        return recordRepo.getNext(timeEnded)
    }

    // Can return several records ended at the same time.
    suspend fun getAllNext(timeStarted: Long): List<Record> {
        val prev = recordRepo.getNext(timeStarted) ?: return emptyList()
        return recordRepo.getByTimeStarted(prev.timeStarted)
    }

    suspend fun getFromRange(range: Range): List<Record> {
        return recordRepo.getFromRange(range)
    }

    suspend fun getFromRangeByType(typeIds: List<Long>, range: Range): List<Record> {
        return recordRepo.getFromRangeByType(typeIds, range)
    }

    suspend fun addFromRunning(
        runningRecord: RunningRecord,
        timeEnded: Long,
    ) {
        Record(
            typeId = runningRecord.id,
            timeStarted = runningRecord.timeStarted,
            timeEnded = timeEnded,
            comment = runningRecord.comment,
            tags = runningRecord.tags,
        ).let {
            add(it)
        }
    }

    suspend fun add(record: Record) {
        val recordId = recordRepo.add(record)
        updateTags(recordId, record.tags)
    }

    suspend fun update(
        recordId: Long,
        typeId: Long,
        comment: String,
        tags: List<RecordBase.Tag>,
    ) {
        recordRepo.update(
            recordId = recordId,
            typeId = typeId,
            comment = comment,
        )
        updateTags(recordId, tags)
    }

    suspend fun updateTimeEnded(recordId: Long, timeEnded: Long) {
        recordRepo.updateTimeEnded(
            recordId = recordId,
            timeEnded = timeEnded,
        )
    }

    suspend fun remove(id: Long) {
        recordToRecordTagRepo.removeAllByRecordId(id)
        recordRepo.remove(id)
    }

    suspend fun removeAll() {
        recordToRecordTagRepo.clear()
        recordRepo.clear()
    }

    private suspend fun updateTags(
        recordId: Long,
        tags: List<RecordBase.Tag>,
    ) {
        recordToRecordTagRepo.removeAllByRecordId(recordId)
        recordToRecordTagRepo.addRecordTags(recordId, tags)
    }
}