package com.example.util.simpletimetracker.domain.interactor

import com.example.util.simpletimetracker.domain.mapper.UntrackedRecordMapper
import com.example.util.simpletimetracker.domain.model.Record
import com.example.util.simpletimetracker.domain.repo.RecordCacheRepo
import com.example.util.simpletimetracker.domain.repo.RecordRepo
import javax.inject.Inject

class RecordInteractor @Inject constructor(
    private val recordRepo: RecordRepo,
    private val recordCacheRepo: RecordCacheRepo,
    private val untrackedRecordMapper: UntrackedRecordMapper
) {

    suspend fun getAll(): List<Record> {
        return recordRepo.getAll()
    }

    suspend fun getByType(typeIds: List<Long>): List<Record> {
        return recordRepo.getByType(typeIds)
    }

    suspend fun get(id: Long): Record? {
        return recordRepo.get(id)
    }

    suspend fun getFromRange(start: Long, end: Long): List<Record> {
        return recordCacheRepo.getFromRange(start, end)
            ?: recordRepo.getFromRange(start, end)
                .also { recordCacheRepo.putWithRange(start, end, it) }
    }

    suspend fun getUntrackedFromRange(start: Long, end: Long): List<Record> {
        return getFromRange(start, end)
            .let { untrackedRecordMapper.mapToUntrackedRecords(it, start, end) }
    }

    suspend fun add(typeId: Long, timeStarted: Long) {
        Record(
            typeId = typeId,
            timeStarted = timeStarted,
            timeEnded = System.currentTimeMillis()
        ).let {
            recordRepo.add(it)
            recordCacheRepo.clear()
        }
    }

    suspend fun add(record: Record) {
        recordRepo.add(record)
        recordCacheRepo.clear()
    }

    suspend fun remove(id: Long) {
        recordRepo.remove(id)
        recordCacheRepo.clear()
    }

    suspend fun clear() {
        recordRepo.clear()
        recordCacheRepo.clear()
    }
}