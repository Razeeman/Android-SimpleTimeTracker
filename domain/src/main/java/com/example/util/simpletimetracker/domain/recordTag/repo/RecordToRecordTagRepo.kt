package com.example.util.simpletimetracker.domain.recordTag.repo

import com.example.util.simpletimetracker.domain.recordTag.model.RecordToRecordTag

interface RecordToRecordTagRepo {

    suspend fun getAll(): List<RecordToRecordTag>

    suspend fun getRecordIdsByTagId(tagId: Long): List<Long>

    suspend fun getRecordCountsByTag(): Map<Long, Int>

    suspend fun add(recordToRecordTag: RecordToRecordTag)
}