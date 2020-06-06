package com.example.util.simpletimetracker.data_local.repo

import com.example.util.simpletimetracker.data_local.database.RecordTypeDao
import com.example.util.simpletimetracker.data_local.mapper.RecordTypeDataLocalMapper
import com.example.util.simpletimetracker.domain.model.RecordType
import com.example.util.simpletimetracker.domain.repo.RecordTypeRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecordTypeRepoImpl @Inject constructor(
    private val recordTypeDao: RecordTypeDao,
    private val recordTypeDataLocalMapper: RecordTypeDataLocalMapper
) : RecordTypeRepo {

    override suspend fun getAll(): List<RecordType> = withContext(Dispatchers.IO) {
        Timber.d("getAll")
        recordTypeDao.getAll().map(recordTypeDataLocalMapper::map)
    }

    override suspend fun get(id: Long): RecordType? = withContext(Dispatchers.IO) {
        Timber.d("get id")
        recordTypeDao.get(id)?.let(recordTypeDataLocalMapper::map)
    }

    override suspend fun get(name: String): RecordType? = withContext(Dispatchers.IO) {
        Timber.d("get name")
        recordTypeDao.get(name)?.let(recordTypeDataLocalMapper::map)
    }

    override suspend fun add(recordType: RecordType) = withContext(Dispatchers.IO) {
        Timber.d("add")
        recordTypeDao.insert(
            recordType.let(recordTypeDataLocalMapper::map)
        )
    }

    override suspend fun remove(id: Long) = withContext(Dispatchers.IO) {
        Timber.d("remove")
        recordTypeDao.delete(id)
    }

    override suspend fun clear() = withContext(Dispatchers.IO) {
        Timber.d("clear")
        recordTypeDao.clear()
    }
}