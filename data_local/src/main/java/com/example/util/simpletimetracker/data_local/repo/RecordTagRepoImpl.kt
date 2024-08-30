package com.example.util.simpletimetracker.data_local.repo

import com.example.util.simpletimetracker.data_local.database.RecordTagDao
import com.example.util.simpletimetracker.data_local.utils.withLockedCache
import com.example.util.simpletimetracker.data_local.mapper.RecordTagDataLocalMapper
import com.example.util.simpletimetracker.data_local.utils.removeIf
import com.example.util.simpletimetracker.domain.model.RecordTag
import com.example.util.simpletimetracker.domain.repo.RecordTagRepo
import kotlinx.coroutines.sync.Mutex
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecordTagRepoImpl @Inject constructor(
    private val dao: RecordTagDao,
    private val mapper: RecordTagDataLocalMapper,
) : RecordTagRepo {

    private var cache: List<RecordTag>? = null
    private val mutex: Mutex = Mutex()

    override suspend fun isEmpty(): Boolean = mutex.withLockedCache(
        logMessage = "isEmpty",
        accessCache = { cache?.isEmpty() },
        accessSource = { dao.isEmpty() == 0L },
    )

    override suspend fun getAll(): List<RecordTag> = mutex.withLockedCache(
        logMessage = "getAll",
        accessCache = { cache },
        accessSource = { dao.getAll().map(mapper::map) },
        afterSourceAccess = { cache = it },
    )

    override suspend fun get(id: Long): RecordTag? = mutex.withLockedCache(
        logMessage = "get",
        accessCache = { cache?.firstOrNull { it.id == id } },
        accessSource = { dao.get(id)?.let(mapper::map) },
    )

    override suspend fun get(name: String): RecordTag? = mutex.withLockedCache(
        logMessage = "get name",
        accessCache = { cache?.firstOrNull { it.name == name } },
        accessSource = { dao.get(name)?.let(mapper::map) },
    )

    override suspend fun getByType(typeId: Long): List<RecordTag> = mutex.withLockedCache(
        logMessage = "getByType",
        accessCache = { cache?.filter { it.iconColorSource == typeId } },
        accessSource = { dao.getByType(typeId).map(mapper::map) },
    )

    override suspend fun add(tag: RecordTag): Long = mutex.withLockedCache(
        logMessage = "add",
        accessSource = { dao.insert(tag.let(mapper::map)) },
        afterSourceAccess = { cache = null },
    )

    override suspend fun archive(id: Long) = mutex.withLockedCache(
        logMessage = "archive",
        accessSource = { dao.archive(id) },
        afterSourceAccess = { cache = cache?.map { if (it.id == id) it.copy(archived = true) else it } },
    )

    override suspend fun restore(id: Long) = mutex.withLockedCache(
        logMessage = "restore",
        accessSource = { dao.restore(id) },
        afterSourceAccess = { cache = cache?.map { if (it.id == id) it.copy(archived = false) else it } },
    )

    override suspend fun remove(id: Long) = mutex.withLockedCache(
        logMessage = "remove",
        accessSource = { dao.delete(id) },
        afterSourceAccess = { cache = cache?.removeIf { it.id == id } },
    )

    override suspend fun clear() = mutex.withLockedCache(
        logMessage = "clear",
        accessSource = { dao.clear() },
        afterSourceAccess = { cache = null },
    )
}