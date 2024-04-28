package com.example.util.simpletimetracker.data_local.repo

import androidx.collection.LruCache
import com.example.util.simpletimetracker.data_local.database.RecordDao
import com.example.util.simpletimetracker.data_local.mapper.RecordDataLocalMapper
import com.example.util.simpletimetracker.data_local.model.RecordWithRecordTagsDBO
import com.example.util.simpletimetracker.data_local.utils.logDataAccess
import com.example.util.simpletimetracker.data_local.utils.withLockedCache
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
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
    private val prefsInteractor: PrefsInteractor,
) : RecordRepo {

    // Put only adjusted records to cache, unadjusted come from source.
    private var getFromRangeCache = LruCache<GetFromRangeKey, List<Record>>(10)
    private var getFromRangeByTypeCache = LruCache<GetFromRangeByTypeKey, List<Record>>(1)
    private var isEmpty: Boolean? = null
    private var showSecondsCache: Boolean? = null
    private val mutex: Mutex = Mutex()

    override suspend fun isEmpty(): Boolean = mutex.withLockedCache(
        logMessage = "isEmpty",
        accessCache = { isEmpty },
        accessSource = { recordDao.isEmpty() == 0L },
        afterSourceAccess = { isEmpty = it },
    )

    override suspend fun getAll(adjusted: Boolean): List<Record> = withContext(Dispatchers.IO) {
        logDataAccess("getAll")
        recordDao.getAll().map { mapItem(it, adjusted) }
    }

    override suspend fun getByType(typeIds: List<Long>): List<Record> = withContext(Dispatchers.IO) {
        logDataAccess("getByType")
        recordDao.getByType(typeIds).map { mapItem(it) }
    }

    override suspend fun getByTypeWithAnyComment(typeIds: List<Long>): List<Record> = withContext(Dispatchers.IO) {
        logDataAccess("getByTypeWithAnyComment")
        recordDao.getByTypeWithAnyComment(typeIds).map { mapItem(it) }
    }

    override suspend fun searchComment(
        text: String,
    ): List<Record> = withContext(Dispatchers.IO) {
        logDataAccess("searchComment")
        recordDao.searchComment(text).map { mapItem(it) }
    }

    override suspend fun searchByTypeWithComment(
        typeIds: List<Long>,
        text: String,
    ): List<Record> = withContext(Dispatchers.IO) {
        logDataAccess("searchByTypeWithComment")
        recordDao.searchByTypeWithComment(typeIds, text).map { mapItem(it) }
    }

    override suspend fun searchAnyComments(): List<Record> = withContext(Dispatchers.IO) {
        logDataAccess("searchAnyComments")
        recordDao.searchAnyComments().map { mapItem(it) }
    }

    override suspend fun get(id: Long, adjusted: Boolean): Record? = withContext(Dispatchers.IO) {
        logDataAccess("get")
        recordDao.get(id)?.let { mapItem(it, adjusted) }
    }

    override suspend fun getFromRange(range: Range, adjusted: Boolean): List<Record> {
        val cacheKey = GetFromRangeKey(range)
        return mutex.withLockedCache(
            logMessage = "getFromRange",
            accessCache = { if (adjusted) getFromRangeCache.get(cacheKey) else null },
            accessSource = {
                recordDao.getFromRange(
                    start = range.timeStarted,
                    end = range.timeEnded,
                ).map { mapItem(it, adjusted) }
            },
            afterSourceAccess = { if (adjusted) getFromRangeCache.put(cacheKey, it) },
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
                ).map { mapItem(it) }
            },
            afterSourceAccess = { getFromRangeByTypeCache.put(cacheKey, it) },
        )
    }

    override suspend fun getPrev(
        timeStarted: Long,
        limit: Long,
        adjusted: Boolean,
    ): List<Record> = withContext(Dispatchers.IO) {
        logDataAccess("getPrev")
        recordDao.getPrev(timeStarted, limit).map { mapItem(it, adjusted) }
    }

    override suspend fun getNext(
        timeEnded: Long,
        adjusted: Boolean,
    ): Record? = withContext(Dispatchers.IO) {
        logDataAccess("getNext")
        recordDao.getNext(timeEnded)?.let { mapItem(it, adjusted) }
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

    override fun clearCache() {
        getFromRangeCache.evictAll()
        getFromRangeByTypeCache.evictAll()
        isEmpty = null
        showSecondsCache = null
    }

    private suspend fun mapItem(
        dbo: RecordWithRecordTagsDBO,
        adjusted: Boolean = true,
    ): Record {
        val showSeconds = if (adjusted) {
            showSecondsCache ?: prefsInteractor.getShowSeconds()
                .also { showSecondsCache = it }
        } else {
            true
        }

        return recordDataLocalMapper.map(dbo, showSeconds)
    }

    private data class GetFromRangeByTypeKey(
        val typeIds: List<Long>,
        val range: Range,
    )

    private data class GetFromRangeKey(
        val range: Range,
    )
}