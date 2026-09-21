package com.example.util.simpletimetracker.data_local.recordTag

import com.example.util.simpletimetracker.data_local.base.logDataAccess
import com.example.util.simpletimetracker.domain.recordTag.model.RecordToRecordTag
import com.example.util.simpletimetracker.domain.recordTag.repo.RecordToRecordTagRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecordToRecordTagRepoImpl @Inject constructor(
    private val dao: RecordToRecordTagDao,
    private val mapper: RecordToRecordTagDataLocalMapper,
) : RecordToRecordTagRepo {

    override suspend fun getAll(): List<RecordToRecordTag> =
        withContext(Dispatchers.IO) {
            logDataAccess("get all")
            dao.getAll().map(mapper::map)
        }

    override suspend fun getRecordIdsByTagId(tagId: Long): List<Long> =
        withContext(Dispatchers.IO) {
            logDataAccess("get record ids")
            dao.getRecordIdsByTagId(tagId)
        }

    override suspend fun add(recordToRecordTag: RecordToRecordTag) =
        withContext(Dispatchers.IO) {
            logDataAccess("add")
            recordToRecordTag
                .let(mapper::map)
                .let {
                    dao.insert(listOf(it))
                }
        }
}