package com.example.util.simpletimetracker.data_local.repo

import com.example.util.simpletimetracker.data_local.database.RunningRecordDao
import com.example.util.simpletimetracker.data_local.mapper.RunningRecordDataLocalMapper
import com.example.util.simpletimetracker.data_local.utils.removeIf
import com.example.util.simpletimetracker.data_local.utils.withLockedCache
import com.example.util.simpletimetracker.domain.model.RunningRecord
import com.example.util.simpletimetracker.domain.repo.RunningRecordRepo
import kotlinx.coroutines.sync.Mutex
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RunningRecordRepoImpl @Inject constructor(
    private val runningRecordDao: RunningRecordDao,
    private val runningRunningRecordLocalMapper: RunningRecordDataLocalMapper,
) : RunningRecordRepo {

    private var cache: List<RunningRecord>? = null
    private val mutex: Mutex = Mutex()

    override suspend fun isEmpty(): Boolean = mutex.withLockedCache(
        logMessage = "isEmpty",
        accessCache = { cache?.isEmpty() },
        accessSource = { runningRecordDao.isEmpty() == 0L },
    )

    override suspend fun getAll(): List<RunningRecord> = mutex.withLockedCache(
        logMessage = "getAll",
        accessCache = { cache },
        accessSource = { runningRecordDao.getAll().map(runningRunningRecordLocalMapper::map) },
        afterSourceAccess = { cache = it },
    )

    override suspend fun get(id: Long): RunningRecord? = mutex.withLockedCache(
        logMessage = "get",
        accessCache = { cache?.firstOrNull { it.id == id } },
        accessSource = { runningRecordDao.get(id)?.let(runningRunningRecordLocalMapper::map) },
    )

    override suspend fun add(runningRecord: RunningRecord): Long = mutex.withLockedCache(
        logMessage = "add",
        accessSource = { runningRecordDao.insert(runningRecord.let(runningRunningRecordLocalMapper::map)) },
        afterSourceAccess = { cache = null },
    )

    override suspend fun remove(id: Long) = mutex.withLockedCache(
        logMessage = "remove",
        accessSource = { runningRecordDao.delete(id) },
        afterSourceAccess = { cache = cache?.removeIf { it.id == id } },
    )

    override suspend fun clear() = mutex.withLockedCache(
        logMessage = "clear",
        accessSource = { runningRecordDao.clear() },
        afterSourceAccess = { cache = null },
    )
}