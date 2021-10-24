package com.example.util.simpletimetracker.domain.repo

interface RunningRecordToRecordTagRepo {

    suspend fun addRunningRecordTags(runningRecordId: Long, tagIds: List<Long>)

    suspend fun removeAllByTagId(tagId: Long)

    suspend fun removeAllByRunningRecordId(runningRecordId: Long)

    suspend fun clear()
}