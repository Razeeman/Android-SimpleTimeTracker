package com.example.util.simpletimetracker.data_local.repo

import com.example.util.simpletimetracker.data_local.database.RunningRecordToRecordTagDao
import com.example.util.simpletimetracker.data_local.mapper.RunningRecordToRecordTagDataLocalMapper
import com.example.util.simpletimetracker.data_local.utils.logDataAccess
import com.example.util.simpletimetracker.domain.repo.RunningRecordToRecordTagRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RunningRecordToRecordTagRepoImpl @Inject constructor(
    private val dao: RunningRecordToRecordTagDao,
    private val mapper: RunningRecordToRecordTagDataLocalMapper,
) : RunningRecordToRecordTagRepo {

    override suspend fun addRunningRecordTags(runningRecordId: Long, tagIds: List<Long>) =
        withContext(Dispatchers.IO) {
            logDataAccess("add running record tags")
            tagIds.map {
                mapper.map(recordId = runningRecordId, recordTagId = it)
            }.let {
                dao.insert(it)
            }
        }

    override suspend fun removeAllByTagId(tagId: Long) =
        withContext(Dispatchers.IO) {
            logDataAccess("remove all by tagId")
            dao.deleteAllByTagId(tagId)
        }

    override suspend fun removeAllByRunningRecordId(runningRecordId: Long) =
        withContext(Dispatchers.IO) {
            logDataAccess("remove all by runningRecordId")
            dao.deleteAllByRecordId(runningRecordId)
        }

    override suspend fun clear() =
        withContext(Dispatchers.IO) {
            logDataAccess("clear")
            dao.clear()
        }
}