package com.example.util.simpletimetracker.data_local.repo

import com.example.util.simpletimetracker.domain.model.Record
import com.example.util.simpletimetracker.domain.repo.RecordCacheRepo
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecordCacheRepoImpl @Inject constructor() : RecordCacheRepo {

    private val recordsByRange: MutableMap<Pair<Long, Long>, MutableList<Record>> =
        mutableMapOf()

    override fun getFromRange(start: Long, end: Long): List<Record>? {
        Timber.d("getFromRange")
        // TODO cache is disabled to avoid synchronization issues, especially in tests
        return null
        return recordsByRange[start to end]
    }

    override fun putWithRange(start: Long, end: Long, records: List<Record>) {
        Timber.d("putWithRange")
        recordsByRange[start to end] = records.toMutableList()
    }

    override fun clear() {
        Timber.d("clear")
        recordsByRange.clear()
    }
}