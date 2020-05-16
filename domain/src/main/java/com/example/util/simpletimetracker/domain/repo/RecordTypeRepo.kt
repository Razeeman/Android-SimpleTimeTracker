package com.example.util.simpletimetracker.domain.repo

import com.example.util.simpletimetracker.domain.model.RecordType

interface RecordTypeRepo {

    suspend fun getAll(): List<RecordType>

    suspend fun get(id: Long): RecordType?

    suspend fun add(recordType: RecordType)

    suspend fun remove(id: Long)

    suspend fun clear()
}