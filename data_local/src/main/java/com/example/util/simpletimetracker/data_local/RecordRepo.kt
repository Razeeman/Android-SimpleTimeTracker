package com.example.util.simpletimetracker.data_local

import com.example.util.simpletimetracker.domain.BaseRecordRepo
import com.example.util.simpletimetracker.domain.Record
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecordRepo @Inject constructor(
    private val recordDao: RecordDao,
    private val recordDataLocalMapper: RecordDataLocalMapper
) : BaseRecordRepo {

    override suspend fun getAll(): List<Record> {
        return recordDao.getAll().map(recordDataLocalMapper::map)
    }

    override suspend fun add(record: Record) {
        recordDao.insert(
            record.let(recordDataLocalMapper::map)
        )
    }

    override suspend fun clear() {
        recordDao.clear()
    }
}