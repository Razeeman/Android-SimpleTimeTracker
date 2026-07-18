package com.example.util.simpletimetracker.data_local.record

import androidx.collection.LruCache
import com.example.util.simpletimetracker.data_local.base.logDataAccess
import com.example.util.simpletimetracker.data_local.base.withLockedCache
import com.example.util.simpletimetracker.domain.extension.dropMillis
import com.example.util.simpletimetracker.domain.record.model.Range
import com.example.util.simpletimetracker.domain.record.model.Record
import com.example.util.simpletimetracker.domain.record.repo.RecordRepo
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
    private var recordCache = LruCache<Long, Record>(1)
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
        recordDao.getAll().map(::mapItem)
    }

    override suspend fun getByType(typeIds: Set<Long>): List<Record> = withContext(Dispatchers.IO) {
        logDataAccess("getByType")
        recordDao.getByType(typeIds).map(::mapItem)
    }

    override suspend fun getByTypeWithAnyComment(typeIds: Set<Long>): List<Record> = withContext(Dispatchers.IO) {
        logDataAccess("getByTypeWithAnyComment")
        recordDao.getByTypeWithAnyComment(typeIds).map(::mapItem)
    }

    override suspend fun searchComment(
        text: String,
    ): List<Record> = withContext(Dispatchers.IO) {
        logDataAccess("searchComment")
        recordDao.searchComment(text).map(::mapItem)
    }

    override suspend fun searchSimilarComments(
        text: String,
        limit: Int,
    ): List<String> = withContext(Dispatchers.IO) {
        logDataAccess("searchSimilarComments")
        recordDao.searchSimilarComments(text, limit)
    }

    override suspend fun getRecentComments(
        typeId: Long,
        limit: Int,
    ): List<String> = withContext(Dispatchers.IO) {
        logDataAccess("getRecentComments")
        recordDao.getRecentComments(typeId, limit)
    }

    override suspend fun searchByTypeWithComment(
        typeIds: Set<Long>,
        text: String,
    ): List<Record> = withContext(Dispatchers.IO) {
        logDataAccess("searchByTypeWithComment")
        recordDao.searchByTypeWithComment(typeIds, text).map(::mapItem)
    }

    override suspend fun searchAnyComments(): List<Record> = withContext(Dispatchers.IO) {
        logDataAccess("searchAnyComments")
        recordDao.searchAnyComments().map(::mapItem)
    }

    override suspend fun getTagged(tagIds: Set<Long>): List<Record> = withContext(Dispatchers.IO) {
        logDataAccess("getTagged")
        recordDao.getTagged(tagIds).map(::mapItem)
    }

    override suspend fun getUntagged(): List<Record> = withContext(Dispatchers.IO) {
        logDataAccess("getUntagged")
        recordDao.getUntagged().map(::mapItem)
    }

    override suspend fun get(id: Long): Record? = mutex.withLockedCache(
        logMessage = "get",
        accessCache = { recordCache[id] },
        accessSource = { recordDao.get(id)?.let(::mapItem) },
        afterSourceAccess = { it?.let { recordCache.put(id, it) } },
    )

    override suspend fun getFromRange(range: Range): List<Record> {
        val cacheKey = GetFromRangeKey(range)
        return mutex.withLockedCache(
            logMessage = "getFromRange",
            accessCache = { getFromRangeCache[cacheKey] },
            accessSource = {
                recordDao.getFromRange(
                    start = range.timeStarted,
                    end = range.timeEnded,
                ).map(::mapItem)
            },
            afterSourceAccess = { getFromRangeCache.put(cacheKey, it) },
        )
    }

    override suspend fun getFromRangeByType(typeIds: Set<Long>, range: Range): List<Record> {
        val cacheKey = GetFromRangeByTypeKey(typeIds, range)
        return mutex.withLockedCache(
            logMessage = "getFromRangeByType",
            accessCache = { getFromRangeByTypeCache[cacheKey] },
            accessSource = {
                recordDao.getFromRangeByType(
                    typesIds = typeIds,
                    start = range.timeStarted,
                    end = range.timeEnded,
                ).map(::mapItem)
            },
            afterSourceAccess = { getFromRangeByTypeCache.put(cacheKey, it) },
        )
    }

    override suspend fun getPrev(
        timeStarted: Long,
        ignoreTypeIds: List<Long>,
    ): Record? = withContext(Dispatchers.IO) {
        logDataAccess("getPrev")
        recordDao.getPrev(timeStarted, ignoreTypeIds)?.let(::mapItem)
    }

    override suspend fun getNext(timeEnded: Long): Record? = withContext(Dispatchers.IO) {
        logDataAccess("getNext")
        recordDao.getNext(timeEnded)?.let(::mapItem)
    }

    override suspend fun getPrevTimeStarted(fromTimestamp: Long): Long? = withContext(Dispatchers.IO) {
        logDataAccess("getPrevTimeStarted")
        recordDao.getPrevTimeStarted(fromTimestamp)
    }

    override suspend fun getNextTimeStarted(fromTimestamp: Long): Long? = withContext(Dispatchers.IO) {
        logDataAccess("getNextTimeStarted")
        recordDao.getNextTimeStarted(fromTimestamp)
    }

    override suspend fun getPrevTimeEnded(fromTimestamp: Long): Long? = withContext(Dispatchers.IO) {
        logDataAccess("getPrevTimeEnded")
        recordDao.getPrevTimeEnded(fromTimestamp)
    }

    override suspend fun getNextTimeEnded(fromTimestamp: Long): Long? = withContext(Dispatchers.IO) {
        logDataAccess("getNextTimeEnded")
        recordDao.getNextTimeEnded(fromTimestamp)
    }

    override suspend fun getByTimeStarted(timeStarted: Long): List<Record> = withContext(Dispatchers.IO) {
        logDataAccess("getByTimeStarted")
        recordDao.getByTimeStarted(timeStarted).map(::mapItem)
    }

    override suspend fun getByTimeEnded(timeEnded: Long): List<Record> = withContext(Dispatchers.IO) {
        logDataAccess("getByTimeEnded")
        recordDao.getByTimeEnded(timeEnded).map(::mapItem)
    }

    override suspend fun add(record: Record): Long = mutex.withLockedCache(
        logMessage = "add",
        accessSource = {
            recordDao.insert(record.let(recordDataLocalMapper::map))
        },
        afterSourceAccess = { clearCache() },
    )

    override suspend fun update(
        recordId: Long,
        typeId: Long,
        comment: String,
    ) = mutex.withLockedCache(
        logMessage = "update",
        accessSource = {
            recordDao.update(
                recordId = recordId,
                typeId = typeId,
                comment = comment,
            )
        },
        afterSourceAccess = { clearCache() },
    )

    override suspend fun updateTimeEnded(
        recordId: Long,
        timeEnded: Long,
    ) = mutex.withLockedCache(
        logMessage = "updateTimeEnded",
        accessSource = {
            recordDao.updateTimeEnded(
                recordId = recordId,
                timeEnded = timeEnded.dropMillis(),
            )
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
        recordCache.evictAll()
        isEmpty = null
    }

    private fun mapItem(
        dbo: RecordWithRecordTagsDBO,
    ): Record {
        return recordDataLocalMapper.map(dbo)
    }

    private data class GetFromRangeByTypeKey(
        val typeIds: Set<Long>,
        val range: Range,
    )

    private data class GetFromRangeKey(
        val range: Range,
    )
}
