package com.example.util.simpletimetracker.data_local.repo

import com.example.util.simpletimetracker.data_local.database.RecordTypeDao
import com.example.util.simpletimetracker.data_local.utils.withLockedCache
import com.example.util.simpletimetracker.data_local.mapper.RecordTypeDataLocalMapper
import com.example.util.simpletimetracker.data_local.utils.removeIf
import com.example.util.simpletimetracker.domain.model.RecordType
import com.example.util.simpletimetracker.domain.repo.RecordTypeRepo
import kotlinx.coroutines.sync.Mutex
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecordTypeRepoImpl @Inject constructor(
    private val recordTypeDao: RecordTypeDao,
    private val recordTypeDataLocalMapper: RecordTypeDataLocalMapper,
) : RecordTypeRepo {

    private var cache: List<RecordType>? = null
    private val mutex: Mutex = Mutex()

    override suspend fun getAll(): List<RecordType> = mutex.withLockedCache(
        logMessage = "getAll",
        accessCache = { cache },
        accessSource = { recordTypeDao.getAll().map(recordTypeDataLocalMapper::map) },
        afterSourceAccess = { cache = it },
    )

    override suspend fun get(id: Long): RecordType? = mutex.withLockedCache(
        logMessage = "get id",
        accessCache = { cache?.firstOrNull { it.id == id } },
        accessSource = { recordTypeDao.get(id)?.let(recordTypeDataLocalMapper::map) },
    )

    override suspend fun get(name: String): RecordType? = mutex.withLockedCache(
        logMessage = "get name",
        accessCache = { cache?.firstOrNull { it.name == name } },
        accessSource = { recordTypeDao.get(name)?.let(recordTypeDataLocalMapper::map) },
    )

    override suspend fun add(recordType: RecordType): Long = mutex.withLockedCache(
        logMessage = "add",
        accessSource = { recordTypeDao.insert(recordType.let(recordTypeDataLocalMapper::map)) },
        afterSourceAccess = { cache = null },
    )

    override suspend fun archive(id: Long) = mutex.withLockedCache(
        logMessage = "archive",
        accessSource = { recordTypeDao.archive(id) },
        afterSourceAccess = { cache = cache?.map { if (it.id == id) it.copy(hidden = true) else it } },
    )

    override suspend fun restore(id: Long) = mutex.withLockedCache(
        logMessage = "restore",
        accessSource = { recordTypeDao.restore(id) },
        afterSourceAccess = { cache = cache?.map { if (it.id == id) it.copy(hidden = false) else it } },
    )

    override suspend fun remove(id: Long) = mutex.withLockedCache(
        logMessage = "remove",
        accessSource = { recordTypeDao.delete(id) },
        afterSourceAccess = { cache = cache?.removeIf { it.id == id } },
    )

    override suspend fun clear() = mutex.withLockedCache(
        logMessage = "clear",
        accessSource = { recordTypeDao.clear() },
        afterSourceAccess = { cache = null },
    )
}