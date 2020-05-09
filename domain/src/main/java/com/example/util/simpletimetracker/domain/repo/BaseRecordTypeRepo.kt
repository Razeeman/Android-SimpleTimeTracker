package com.example.util.simpletimetracker.domain.repo

import com.example.util.simpletimetracker.domain.model.RecordType

interface BaseRecordTypeRepo {

    suspend fun getAll(): List<RecordType>

    suspend fun add(recordType: RecordType)

    suspend fun remove(id: Long)

    suspend fun clear()
}