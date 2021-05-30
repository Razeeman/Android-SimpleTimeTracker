package com.example.util.simpletimetracker.domain.repo

import com.example.util.simpletimetracker.domain.model.Record

interface RecordRepo {

    suspend fun getAll(): List<Record>

    suspend fun getByType(typeIds: List<Long>): List<Record>

    suspend fun getByTag(tagIds: List<Long>): List<Record>

    suspend fun get(id: Long): Record?

    suspend fun getFromRange(start: Long, end: Long): List<Record>

    suspend fun add(record: Record)

    suspend fun remove(id: Long)

    suspend fun removeByType(typeId: Long)

    suspend fun removeTag(tagId: Long)

    suspend fun clear()
}