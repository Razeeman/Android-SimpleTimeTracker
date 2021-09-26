package com.example.util.simpletimetracker.domain.interactor

import com.example.util.simpletimetracker.domain.model.RecordTag
import com.example.util.simpletimetracker.domain.model.RecordToRecordTag
import com.example.util.simpletimetracker.domain.repo.RecordToRecordTagRepo
import javax.inject.Inject

class RecordToRecordTagInteractor @Inject constructor(
    private val repo: RecordToRecordTagRepo,
) {

    suspend fun getAll(): List<RecordToRecordTag> {
        return repo.getAll()
    }

    suspend fun getTagsByRecordId(recordId: Long): List<RecordTag> {
        return repo.getTagsByRecordId(recordId)
    }

    suspend fun getTagIdsByRecordId(recordId: Long): List<Long> {
        return repo.getTagIdsByRecordId(recordId)
    }

    suspend fun getRecordIdsByTagId(tagId: Long): List<Long> {
        return repo.getRecordIdsByTagId(tagId)
    }

    suspend fun add(recordToRecordTag: RecordToRecordTag) {
        repo.add(recordToRecordTag)
    }

    suspend fun addRecordTags(recordId: Long, tagIds: List<Long>) {
        repo.addRecordTags(recordId, tagIds)
    }

    suspend fun removeRecordTags(recordId: Long, tagIds: List<Long>) {
        repo.removeRecordTags(recordId, tagIds)
    }

    suspend fun removeAllByTagId(tagId: Long) {
        repo.removeAllByTagId(tagId)
    }

    suspend fun removeAllByRecordId(recordId: Long) {
        repo.removeAllByRecordId(recordId)
    }

    suspend fun clear() {
        repo.clear()
    }
}