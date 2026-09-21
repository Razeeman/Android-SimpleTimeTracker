package com.example.util.simpletimetracker.data_local.record

import com.example.util.simpletimetracker.data_local.base.logDataAccess
import com.example.util.simpletimetracker.data_local.base.withLockedCache
import com.example.util.simpletimetracker.data_local.recordTag.RunningRecordToRecordTagDataLocalMapper
import com.example.util.simpletimetracker.data_local.recordTag.RunningRecordToRecordTagDao
import com.example.util.simpletimetracker.domain.extension.dropMillis
import com.example.util.simpletimetracker.domain.extension.removeIf
import com.example.util.simpletimetracker.domain.extension.replaceWith
import com.example.util.simpletimetracker.domain.record.model.RunningRecord
import com.example.util.simpletimetracker.domain.record.repo.RunningRecordRepo
import kotlinx.coroutines.sync.Mutex
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RunningRecordRepoImpl @Inject constructor(
    private val dao: RunningRecordDao,
    private val runningRecordToRecordTagDao: RunningRecordToRecordTagDao,
    private val mapper: RunningRecordDataLocalMapper,
    private val runningRecordToRecordTagDataLocalMapper: RunningRecordToRecordTagDataLocalMapper,
) : RunningRecordRepo {

    private var cache: List<RunningRecord>? = null
    private val mutex: Mutex = Mutex()

    override suspend fun isEmpty(): Boolean = mutex.withLockedCache(
        logMessage = "isEmpty",
        accessCache = { cache?.isEmpty() },
        accessSource = { dao.isEmpty() == 0L },
    )

    override suspend fun getAll(): List<RunningRecord> = mutex.withLockedCache(
        logMessage = "getAll",
        accessCache = { cache },
        accessSource = { dao.getAll().map(mapper::map) },
        afterSourceAccess = { cache = it },
    )

    override suspend fun get(id: Long): RunningRecord? = mutex.withLockedCache(
        logMessage = "get",
        accessCache = { cache?.firstOrNull { it.id == id } },
        accessSource = { dao.get(id)?.let(mapper::map) },
    )

    override suspend fun has(id: Long): Boolean = mutex.withLockedCache(
        logMessage = "has",
        accessCache = { cache?.any { it.id == id } },
        accessSource = { dao.get(id) != null },
        afterSourceAccess = { initializeCache() },
    )

    override suspend fun add(runningRecord: RunningRecord): Long = mutex.withLockedCache(
        logMessage = "add",
        accessSource = {
            dao.insert(
                record = runningRecord.let(mapper::map),
                recordTags = runningRecord.tags.map {
                    runningRecordToRecordTagDataLocalMapper.map(runningRecord.id, it)
                },
            )
        },
        afterSourceAccess = { id ->
            val new = runningRecord.copy(
                id = id,
                timeStarted = runningRecord.timeStarted.dropMillis(),
            )
            cache = cache?.replaceWith(new) { it.id == id }
        },
    )

    override suspend fun remove(id: Long) = mutex.withLockedCache(
        logMessage = "remove",
        accessSource = { dao.delete(id) },
        afterSourceAccess = { cache = cache?.removeIf { it.id == id } },
    )

    override suspend fun removeTagFromAll(tagId: Long) = mutex.withLockedCache(
        logMessage = "removeTagFromAll",
        accessSource = { runningRecordToRecordTagDao.deleteAllByTagId(tagId) },
        afterSourceAccess = { cache = null },
    )

    override suspend fun clear() = mutex.withLockedCache(
        logMessage = "clear",
        accessSource = { dao.clear() },
        afterSourceAccess = { cache = null },
    )

    private suspend fun initializeCache() {
        logDataAccess("initializeCache")
        cache = dao.getAll().map(mapper::map)
    }
}