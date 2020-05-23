package com.example.util.simpletimetracker.domain.repo

import com.example.util.simpletimetracker.domain.model.Record

interface RecordCacheRepo {

    fun getFromRange(start: Long, end: Long): List<Record>?

    fun putWithRange(start: Long, end: Long, records: List<Record>)

    fun clear()
}