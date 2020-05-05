package com.example.util.simpletimetracker.data_local.repo

import com.example.util.simpletimetracker.data_local.database.RunningRecordDao
import com.example.util.simpletimetracker.data_local.mapper.RunningRecordDataLocalMapper
import com.example.util.simpletimetracker.domain.model.RunningRecord
import com.example.util.simpletimetracker.domain.repo.BaseRunningRecordRepo
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RunningRecordRepo @Inject constructor(
    private val runningRecordDao: RunningRecordDao,
    private val runningRunningRecordLocalMapper: RunningRecordDataLocalMapper
) : BaseRunningRecordRepo {

    override suspend fun getAll(): List<RunningRecord> {
        return runningRecordDao.getAll().map(runningRunningRecordLocalMapper::map)
    }

    override suspend fun add(runningRecord: RunningRecord) {
        runningRecordDao.insert(
            runningRecord.let(runningRunningRecordLocalMapper::map)
        )
    }

    override suspend fun remove(name: String) {
        runningRecordDao.delete(name)
    }

    override suspend fun clear() {
        runningRecordDao.clear()
    }
}