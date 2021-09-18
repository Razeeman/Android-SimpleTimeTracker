package com.example.util.simpletimetracker.domain.repo

import com.example.util.simpletimetracker.domain.model.RecordTag
import com.example.util.simpletimetracker.domain.model.RecordToRecordTag

interface RecordToRecordTagRepo {

    suspend fun getAll(): List<RecordToRecordTag>

    suspend fun getTagsByRecordId(recordId: Long): List<RecordTag>

    suspend fun getTagIdsByRecordId(recordId: Long): List<Long>

    suspend fun getRecordIdsByTagId(tagId: Long): List<Long>

    suspend fun add(recordToRecordTag: RecordToRecordTag)

    suspend fun addRecordTags(recordId: Long, tagIds: List<Long>)

    suspend fun removeRecordTags(recordId: Long, tagIds: List<Long>)

    suspend fun removeAllByTagId(tagId: Long)

    suspend fun removeAllByRecordId(recordId: Long)

    suspend fun clear()
}