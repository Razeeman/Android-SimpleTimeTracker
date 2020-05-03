package com.example.util.simpletimetracker.data_local.repo

import com.example.util.simpletimetracker.data_local.database.RecordDao
import com.example.util.simpletimetracker.data_local.mapper.RecordDataLocalMapper
import com.example.util.simpletimetracker.domain.repo.BaseRecordRepo
import com.example.util.simpletimetracker.domain.model.Record
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