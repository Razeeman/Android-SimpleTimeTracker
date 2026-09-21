package com.example.util.simpletimetracker.data_local.recordTag

import com.example.util.simpletimetracker.data_local.base.logDataAccess
import com.example.util.simpletimetracker.domain.recordTag.repo.RunningRecordToRecordTagRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RunningRecordToRecordTagRepoImpl @Inject constructor(
    private val dao: RunningRecordToRecordTagDao,
) : RunningRecordToRecordTagRepo {

    override suspend fun removeAllByTagId(tagId: Long) =
        withContext(Dispatchers.IO) {
            logDataAccess("remove all by tagId")
            dao.deleteAllByTagId(tagId)
        }
}