package com.example.util.simpletimetracker.data_local.repo

import com.example.util.simpletimetracker.data_local.database.RecordTypeDao
import com.example.util.simpletimetracker.data_local.mapper.RecordTypeDataLocalMapper
import com.example.util.simpletimetracker.domain.model.RecordType
import com.example.util.simpletimetracker.domain.repo.BaseRecordTypeRepo
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecordTypeRepo @Inject constructor(
    private val recordTypeDao: RecordTypeDao,
    private val recordTypeDataLocalMapper: RecordTypeDataLocalMapper
) : BaseRecordTypeRepo {

    override suspend fun getAll(): List<RecordType> {
        return recordTypeDao.getAll().map(recordTypeDataLocalMapper::map)
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