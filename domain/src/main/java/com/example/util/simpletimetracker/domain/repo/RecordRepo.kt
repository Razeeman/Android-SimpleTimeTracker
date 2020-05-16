package com.example.util.simpletimetracker.domain.repo

import com.example.util.simpletimetracker.domain.model.Record

interface RecordRepo {

    suspend fun getAll(): List<Record>

    suspend fun add(record: Record)

    suspend fun remove(id: Long)

    suspend fun clear()
}