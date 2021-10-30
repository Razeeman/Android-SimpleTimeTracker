package com.example.util.simpletimetracker.data_local.repo

import com.example.util.simpletimetracker.data_local.database.RecordToRecordTagDao
import com.example.util.simpletimetracker.data_local.mapper.RecordToRecordTagDataLocalMapper
import com.example.util.simpletimetracker.domain.model.RecordToRecordTag
import com.example.util.simpletimetracker.domain.repo.RecordToRecordTagRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecordToRecordTagRepoImpl @Inject constructor(
    private val dao: RecordToRecordTagDao,
    private val mapper: RecordToRecordTagDataLocalMapper,
) : RecordToRecordTagRepo {

    override suspend fun getAll(): List<RecordToRecordTag> =
        withContext(Dispatchers.IO) {
            Timber.d("get all")
            dao.getAll().map(mapper::map)
        }

    override suspend fun getTagIdsByRecordId(recordId: Long): List<Long> =
        withContext(Dispatchers.IO) {
            Timber.d("get tag ids")
            dao.getTagIdsByRecordId(recordId)
        }

    override suspend fun getRecordIdsByTagId(tagId: Long): List<Long> =
        withContext(Dispatchers.IO) {
            Timber.d("get record ids")
            dao.getRecordIdsByTagId(tagId)
        }

    override suspend fun add(recordToRecordTag: RecordToRecordTag) =
        withContext(Dispatchers.IO) {
            Timber.d("add")
            recordToRecordTag
                .let(mapper::map)
                .let {
                    dao.insert(listOf(it))
                }
        }

    override suspend fun addRecordTags(recordId: Long, tagIds: List<Long>) =
        withContext(Dispatchers.IO) {
            Timber.d("add record tags")
            tagIds.map {
                mapper.map(recordId = recordId, recordTagId = it)
            }.let {
                dao.insert(it)
            }
        }

    override suspend fun removeRecordTags(recordId: Long, tagIds: List<Long>) =
        withContext(Dispatchers.IO) {
            Timber.d("remove record tags")
            tagIds.map {
                mapper.map(recordId = recordId, recordTagId = it)
            }.let {
                dao.delete(it)
            }
        }

    override suspend fun removeAllByTagId(tagId: Long) =
        withContext(Dispatchers.IO) {
            Timber.d("remove all by tagId")
            dao.deleteAllByTagId(tagId)
        }

    override suspend fun removeAllByRecordId(recordId: Long) =
        withContext(Dispatchers.IO) {
            Timber.d("remove all by recordId")
            dao.deleteAllByRecordId(recordId)
        }

    override suspend fun clear() =
        withContext(Dispatchers.IO) {
            Timber.d("clear")
            dao.clear()
        }
}