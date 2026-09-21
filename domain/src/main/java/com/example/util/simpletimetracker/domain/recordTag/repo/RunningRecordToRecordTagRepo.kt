package com.example.util.simpletimetracker.domain.recordTag.repo

interface RunningRecordToRecordTagRepo {

    suspend fun removeAllByTagId(tagId: Long)
}