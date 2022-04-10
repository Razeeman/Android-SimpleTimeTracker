package com.example.util.simpletimetracker.domain.repo

import com.example.util.simpletimetracker.domain.model.Record

interface RecordRepo {

    suspend fun getAll(): List<Record>

    suspend fun getByType(typeIds: List<Long>): List<Record>

    suspend fun getByTypeWithComment(typeIds: List<Long>): List<Record>

    suspend fun searchComments(typeIds: List<Long>, text: String): List<Record>

    suspend fun get(id: Long): Record?

    suspend fun getFromRange(start: Long, end: Long): List<Record>

    suspend fun add(record: Record): Long

    suspend fun remove(id: Long)

    suspend fun removeByType(typeId: Long)

    suspend fun clear()
}