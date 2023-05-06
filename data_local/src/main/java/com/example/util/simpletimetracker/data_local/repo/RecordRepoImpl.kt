package com.example.util.simpletimetracker.data_local.repo

import com.example.util.simpletimetracker.data_local.database.RecordDao
import com.example.util.simpletimetracker.data_local.mapper.RecordDataLocalMapper
import com.example.util.simpletimetracker.domain.model.Record
import com.example.util.simpletimetracker.domain.repo.RecordRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecordRepoImpl @Inject constructor(
    private val recordDao: RecordDao,
    private val recordDataLocalMapper: RecordDataLocalMapper,
) : RecordRepo {

    override suspend fun getAll(): List<Record> = withContext(Dispatchers.IO) {
        Timber.d("getAll")
        recordDao.getAll()
            .map(recordDataLocalMapper::map)
    }

    override suspend fun getByType(typeIds: List<Long>): List<Record> = withContext(Dispatchers.IO) {
        Timber.d("getByType")
        recordDao.getByType(typeIds)
            .map(recordDataLocalMapper::map)
    }

    override suspend fun getByTypeWithAnyComment(typeIds: List<Long>): List<Record> = withContext(Dispatchers.IO) {
        Timber.d("getByTypeWithAnyComment")
        recordDao.getByTypeWithAnyComment(typeIds)
            .map(recordDataLocalMapper::map)
    }

    override suspend fun searchComment(
        text: String,
    ): List<Record> = withContext(Dispatchers.IO) {
        Timber.d("searchComment")
        recordDao.searchComment(text)
            .map(recordDataLocalMapper::map)
    }

    override suspend fun searchByTypeWithComment(
        typeIds: List<Long>,
        text: String,
    ): List<Record> = withContext(Dispatchers.IO) {
        Timber.d("searchByTypeWithComment")
        recordDao.searchByTypeWithComment(typeIds, text)
            .map(recordDataLocalMapper::map)
    }

    override suspend fun searchAnyComments(): List<Record> = withContext(Dispatchers.IO) {
        Timber.d("searchAnyComments")
        recordDao.searchAnyComments()
            .map(recordDataLocalMapper::map)
    }

    override suspend fun get(id: Long): Record? = withContext(Dispatchers.IO) {
        Timber.d("get")
        recordDao.get(id)
            ?.let(recordDataLocalMapper::map)
    }

    override suspend fun getFromRange(start: Long, end: Long): List<Record> =
        withContext(Dispatchers.IO) {
            Timber.d("getFromRange")
            recordDao.getFromRange(start, end)
                .map(recordDataLocalMapper::map)
        }

    override suspend fun getFromRangeByType(typeIds: List<Long>, start: Long, end: Long): List<Record> =
        withContext(Dispatchers.IO) {
            Timber.d("getFromRangeByType")
            recordDao.getFromRangeByType(typeIds, start, end)
                .map(recordDataLocalMapper::map)
        }

    override suspend fun getPrev(timeStarted: Long): Record? =
        withContext(Dispatchers.IO) {
            Timber.d("getPrev")
            recordDao.getPrev(timeStarted)
                ?.let(recordDataLocalMapper::map)
        }

    override suspend fun getNext(timeEnded: Long): Record? =
        withContext(Dispatchers.IO) {
            Timber.d("getNext")
            recordDao.getNext(timeEnded)
                ?.let(recordDataLocalMapper::map)
        }

    override suspend fun add(record: Record): Long = withContext(Dispatchers.IO) {
        Timber.d("add")
        return@withContext recordDao.insert(
            record.let(recordDataLocalMapper::map)
        )
    }

    override suspend fun remove(id: Long) = withContext(Dispatchers.IO) {
        Timber.d("remove")
        recordDao.delete(id)
    }

    override suspend fun removeByType(typeId: Long) = withContext(Dispatchers.IO) {
        Timber.d("removeByType")
        recordDao.deleteByType(typeId)
    }

    override suspend fun clear() = withContext(Dispatchers.IO) {
        Timber.d("clear")
        recordDao.clear()
    }
}