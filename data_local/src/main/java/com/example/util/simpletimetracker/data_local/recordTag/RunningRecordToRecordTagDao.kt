package com.example.util.simpletimetracker.data_local.recordTag

import androidx.room.Dao
import androidx.room.Query

@Dao
interface RunningRecordToRecordTagDao {

    @Query("DELETE FROM runningRecordToRecordTag WHERE record_tag_id = :tagId")
    suspend fun deleteAllByTagId(tagId: Long)
}