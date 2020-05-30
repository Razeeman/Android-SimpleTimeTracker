package com.example.util.simpletimetracker.domain.repo

import com.example.util.simpletimetracker.domain.model.RecordType

interface RecordTypeCacheRepo {

    fun getAll(): List<RecordType>

    fun addAll(recordTypes: List<RecordType>)

    fun clear()
}