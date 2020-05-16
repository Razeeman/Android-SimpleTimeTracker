package com.example.util.simpletimetracker.data_local.repo

import com.example.util.simpletimetracker.data_local.database.RunningRecordDao
import com.example.util.simpletimetracker.data_local.mapper.RunningRecordDataLocalMapper
import com.example.util.simpletimetracker.domain.model.RunningRecord
import com.example.util.simpletimetracker.domain.repo.RunningRecordRepo
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RunningRecordRepoImpl @Inject constructor(
    private val runningRecordDao: RunningRecordDao,
    private val runningRunningRecordLocalMapper: RunningRecordDataLocalMapper
) : RunningRecordRepo {

    override suspend fun getAll(): List<RunningRecord> {
        return runningRecordDao.getAll().map(runningRunningRecordLocalMapper::map)
    }

    override suspend fun get(id: Long): RunningRecord? {
        return runningRecordDao.get(id)?.let(runningRunningRecordLocalMapper::map)
    }

    override suspend fun add(runningRecord: RunningRecord) {
        runningRecordDao.insert(
            runningRecord.let(runningRunningRecordLocalMapper::map)
        )
    }

    override suspend fun remove(id: Long) {
        runningRecordDao.delete(id)
    }

    override suspend fun clear() {
        runningRecordDao.clear()
    }
}