package com.example.util.simpletimetracker.domain.repo

import com.example.util.simpletimetracker.domain.model.RecordType

interface RecordTypeRepo {

    suspend fun getAll(): List<RecordType>

    suspend fun get(id: Long): RecordType?

    suspend fun get(name: String): RecordType?

    suspend fun add(recordType: RecordType): Long

    suspend fun archive(id: Long)

    suspend fun restore(id: Long)

    suspend fun remove(id: Long)

    suspend fun clear()
}