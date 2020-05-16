package com.example.util.simpletimetracker.data_local.repo

import com.example.util.simpletimetracker.data_local.database.RecordTypeDao
import com.example.util.simpletimetracker.data_local.mapper.RecordTypeDataLocalMapper
import com.example.util.simpletimetracker.domain.model.RecordType
import com.example.util.simpletimetracker.domain.repo.RecordTypeRepo
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecordTypeRepoImpl @Inject constructor(
    private val recordTypeDao: RecordTypeDao,
    private val recordTypeDataLocalMapper: RecordTypeDataLocalMapper
) : RecordTypeRepo {

    override suspend fun getAll(): List<RecordType> {
        return recordTypeDao.getAll().map(recordTypeDataLocalMapper::map)
    }

    override suspend fun get(id: Long): RecordType? {
        return recordTypeDao.get(id)?.let(recordTypeDataLocalMapper::map)
    }

    override suspend fun add(recordType: RecordType) {
        recordTypeDao.insert(
            recordType.let(recordTypeDataLocalMapper::map)
        )
    }

    override suspend fun remove(id: Long) {
        recordTypeDao.delete(id)
    }

    override suspend fun clear() {
        recordTypeDao.clear()
    }
}