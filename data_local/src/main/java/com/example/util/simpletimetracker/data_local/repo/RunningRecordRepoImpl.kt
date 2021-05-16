package com.example.util.simpletimetracker.data_local.repo

import com.example.util.simpletimetracker.data_local.database.RunningRecordDao
import com.example.util.simpletimetracker.data_local.mapper.RunningRecordDataLocalMapper
import com.example.util.simpletimetracker.domain.model.RunningRecord
import com.example.util.simpletimetracker.domain.repo.RunningRecordRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RunningRecordRepoImpl @Inject constructor(
    private val runningRecordDao: RunningRecordDao,
    private val runningRunningRecordLocalMapper: RunningRecordDataLocalMapper
) : RunningRecordRepo {

    override suspend fun getAll(): List<RunningRecord> = withContext(Dispatchers.IO) {
        Timber.d("getAll")
        runningRecordDao.getAll().map(runningRunningRecordLocalMapper::map)
    }

    override suspend fun get(id: Long): RunningRecord? = withContext(Dispatchers.IO) {
        Timber.d("get")
        runningRecordDao.get(id)?.let(runningRunningRecordLocalMapper::map)
    }

    override suspend fun add(runningRecord: RunningRecord) = withContext(Dispatchers.IO) {
        Timber.d("add")
        runningRecordDao.insert(
            runningRecord.let(runningRunningRecordLocalMapper::map)
        )
    }

    override suspend fun remove(id: Long) = withContext(Dispatchers.IO) {
        Timber.d("remove")
        runningRecordDao.delete(id)
    }

    override suspend fun removeTag(tagId: Long) = withContext(Dispatchers.IO) {
        Timber.d("removeTag")
        runningRecordDao.removeTag(tagId)
    }

    override suspend fun clear() = withContext(Dispatchers.IO) {
        Timber.d("clear")
        runningRecordDao.clear()
    }
}