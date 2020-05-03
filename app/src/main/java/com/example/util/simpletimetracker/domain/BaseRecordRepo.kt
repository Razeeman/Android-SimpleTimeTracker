package com.example.util.simpletimetracker.domain

interface BaseRecordRepo {

    suspend fun getAll(): List<Record>

    suspend fun add(record: Record)

    suspend fun clear()
}