package com.example.util.simpletimetracker.data_local.repo

import androidx.collection.LruCache
import com.example.util.simpletimetracker.data_local.database.RecordDao
import com.example.util.simpletimetracker.data_local.mapper.RecordDataLocalMapper
import com.example.util.simpletimetracker.data_local.utils.logDataAccess
import com.example.util.simpletimetracker.data_local.utils.withLockedCache
import com.example.util.simpletimetracker.domain.model.Range
import com.example.util.simpletimetracker.domain.model.Record
import com.example.util.simpletimetracker.domain.repo.RecordRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecordRepoImpl @Inject constructor(
    private val recordDao: RecordDao,
    private val recordDataLocalMapper: RecordDataLocalMapper,
) : RecordRepo {

    private var getFromRangeCache = LruCache<GetFromRangeKey, List<Record>>(10)
    private var getFromRangeByTypeCache = LruCache<GetFromRangeByTypeKey, List<Record>>(1)
    private var isEmpty: Boolean? = null
    private val mutex: Mutex = Mutex()

    override suspend fun isEmpty(): Boolean = mutex.withLockedCache(
        logMessage = "isEmpty",
        accessCache = { isEmpty },
        accessSource = { recordDao.isEmpty() == 0L },
        afterSourceAccess = { isEmpty = it },
    )

    override suspend fun getAll(): List<Record> = withContext(Dispatchers.IO) {
        logDataAccess("getAll")
        recordDao.getAll().map(recordDataLocalMapper::map)
    }

    override suspend fun getByType(typeIds: List<Long>): List<Record> = withContext(Dispatchers.IO) {
        logDataAccess("getByType")
        recordDao.getByType(typeIds).map(recordDataLocalMapper::map)
    }

    override suspend fun getByTypeWithAnyComment(typeIds: List<Long>): List<Record> = withContext(Dispatchers.IO) {
        logDataAccess("getByTypeWithAnyComment")
        recordDao.getByTypeWithAnyComment(typeIds).map(recordDataLocalMapper::map)
    }

    override suspend fun searchComment(
        text: String,
    ): List<Record> = withContext(Dispatchers.IO) {
        logDataAccess("searchComment")
        recordDao.searchComment(text).map(recordDataLocalMapper::map)
    }

    override suspend fun searchByTypeWithComment(
        typeIds: List<Long>,
        text: String,
    ): List<Record> = withContext(Dispatchers.IO) {
        logDataAccess("searchByTypeWithComment")
        recordDao.searchByTypeWithComment(typeIds, text).map(recordDataLocalMapper::map)
    }

    override suspend fun searchAnyComments(): List<Record> = withContext(Dispatchers.IO) {
        logDataAccess("searchAnyComments")
        recordDao.searchAnyComments().map(recordDataLocalMapper::map)
    }

    override suspend fun get(id: Long): Record? = withContext(Dispatchers.IO) {
        logDataAccess("get")
        recordDao.get(id)?.let(recordDataLocalMapper::map)
    }

    override suspend fun getFromRange(range: Range): List<Record> {
        val cacheKey = GetFromRangeKey(range)
        return mutex.withLockedCache(
            logMessage = "getFromRange",
            accessCache = { getFromRangeCache.get(cacheKey) },
            accessSource = {
                recordDao.getFromRange(
                    start = range.timeStarted,
                    end = range.timeEnded,
                ).map(recordDataLocalMapper::map)
            },
            afterSourceAccess = { getFromRangeCache.put(cacheKey, it) },
        )
    }

    override suspend fun getFromRangeByType(typeIds: List<Long>, range: Range): List<Record> {
        val cacheKey = GetFromRangeByTypeKey(typeIds, range)
        return mutex.withLockedCache(
            logMessage = "getFromRangeByType",
            accessCache = { getFromRangeByTypeCache.get(cacheKey) },
            accessSource = {
                recordDao.getFromRangeByType(
                    typesIds = typeIds,
                    start = range.timeStarted,
                    end = range.timeEnded,
                ).map(recordDataLocalMapper::map)
            },
            afterSourceAccess = { getFromRangeByTypeCache.put(cacheKey, it) },
        )
    }

    override suspend fun getPrev(timeStarted: Long, limit: Long): List<Record> =
        withContext(Dispatchers.IO) {
            logDataAccess("getPrev")
            recordDao.getPrev(timeStarted, limit)
                .map(recordDataLocalMapper::map)
        }

    override suspend fun getNext(timeEnded: Long): Record? =
        withContext(Dispatchers.IO) {
            logDataAccess("getNext")
            recordDao.getNext(timeEnded)
                ?.let(recordDataLocalMapper::map)
        }

    override suspend fun add(record: Record): Long = mutex.withLockedCache(
        logMessage = "add",
        accessSource = {
            // Drop milliseconds.
            val adjustedRecord = record.copy(
                timeStarted = record.timeStarted / 1000 * 1000,
                timeEnded = record.timeEnded / 1000 * 1000,
            )
            recordDao.insert(adjustedRecord.let(recordDataLocalMapper::map))
        },
        afterSourceAccess = { clearCache() },
    )

    override suspend fun remove(id: Long) = mutex.withLockedCache(
        logMessage = "remove",
        accessSource = { recordDao.delete(id) },
        afterSourceAccess = { clearCache() },
    )

    override suspend fun removeByType(typeId: Long) = mutex.withLockedCache(
        logMessage = "removeByType",
        accessSource = { recordDao.deleteByType(typeId) },
        afterSourceAccess = { clearCache() },
    )

    override suspend fun clear() = mutex.withLockedCache(
        logMessage = "clear",
        accessSource = { recordDao.clear() },
        afterSourceAccess = { clearCache() },
    )

    private fun clearCache() {
        getFromRangeCache.evictAll()
        getFromRangeByTypeCache.evictAll()
        isEmpty = null
    }

    private data class GetFromRangeByTypeKey(
        val typeIds: List<Long>,
        val range: Range,
    )

    private data class GetFromRangeKey(
        val range: Range,
    )
}