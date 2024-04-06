package com.example.util.simpletimetracker.data_local.repo

import com.example.util.simpletimetracker.data_local.database.RecordTypeToTagDao
import com.example.util.simpletimetracker.data_local.mapper.RecordTypeToTagDataLocalMapper
import com.example.util.simpletimetracker.data_local.utils.removeIf
import com.example.util.simpletimetracker.data_local.utils.withLockedCache
import com.example.util.simpletimetracker.domain.model.RecordTypeCategory
import com.example.util.simpletimetracker.domain.model.RecordTypeToTag
import com.example.util.simpletimetracker.domain.repo.RecordTypeToTagRepo
import kotlinx.coroutines.sync.Mutex
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecordTypeToTagRepoImpl @Inject constructor(
    private val dao: RecordTypeToTagDao,
    private val mapper: RecordTypeToTagDataLocalMapper,
) : RecordTypeToTagRepo {

    private var cache: List<RecordTypeToTag>? = null
    private val mutex: Mutex = Mutex()

    override suspend fun getAll(): List<RecordTypeToTag> = mutex.withLockedCache(
        logMessage = "getAll",
        accessCache = { cache },
        accessSource = { dao.getAll().map(mapper::map) },
        afterSourceAccess = { cache = it },
    )

    override suspend fun getTagIdsByType(typeId: Long): Set<Long> = mutex.withLockedCache(
        logMessage = "getTagIdsByType",
        accessCache = { cache?.filter { it.recordTypeId == typeId }?.map { it.tagId }?.toSet() },
        accessSource = { dao.getTagIdsByType(typeId).toSet() },
    )

    override suspend fun getTypeIdsByTag(tagId: Long): Set<Long> = mutex.withLockedCache(
        logMessage = "getTypeIdsByTag",
        accessCache = { cache?.filter { it.tagId == tagId }?.map { it.recordTypeId }?.toSet() },
        accessSource = { dao.getTypeIdsByTag(tagId).toSet() },
    )

    override suspend fun add(recordTypeToTag: RecordTypeToTag) = mutex.withLockedCache(
        logMessage = "add",
        accessSource = {
            recordTypeToTag
                .let(mapper::map)
                .let { dao.insert(listOf(it)) }
        },
        afterSourceAccess = { cache = null },
    )

    override suspend fun addTypes(tagId: Long, typeIds: List<Long>) = mutex.withLockedCache(
        logMessage = "addTypes",
        accessSource = {
            typeIds.map {
                mapper.map(typeId = it, tagId = tagId)
            }.let { dao.insert(it) }
        },
        afterSourceAccess = { cache = null },
    )

    override suspend fun removeTypes(tagId: Long, typeIds: List<Long>) = mutex.withLockedCache(
        logMessage = "removeTypes",
        accessSource = {
            typeIds.map {
                mapper.map(typeId = it, tagId = tagId)
            }.let { dao.delete(it) }
        },
        afterSourceAccess = { cache = null },
    )

    override suspend fun removeAll(tagId: Long) = mutex.withLockedCache(
        logMessage = "removeAll",
        accessSource = { dao.deleteAll(tagId) },
        afterSourceAccess = { cache = cache?.removeIf { it.tagId == tagId } },
    )

    override suspend fun removeAllByType(typeId: Long) = mutex.withLockedCache(
        logMessage = "removeAllByType",
        accessSource = { dao.deleteAllByType(typeId) },
        afterSourceAccess = { cache = cache?.removeIf { it.recordTypeId == typeId } },
    )

    override suspend fun clear() = mutex.withLockedCache(
        logMessage = "clear",
        accessSource = { dao.clear() },
        afterSourceAccess = { cache = null },
    )
}