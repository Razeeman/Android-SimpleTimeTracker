package com.example.util.simpletimetracker.data_local.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.util.simpletimetracker.data_local.model.RunningRecordToRecordTagDBO

@Dao
interface RunningRecordToRecordTagDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(runningRecordToRecordTagDBO: List<RunningRecordToRecordTagDBO>)

    @Query("DELETE FROM runningRecordToRecordTag WHERE record_tag_id = :tagId")
    suspend fun deleteAllByTagId(tagId: Long)

    @Query("DELETE FROM runningRecordToRecordTag WHERE running_record_id = :runningRecordId")
    suspend fun deleteAllByRecordId(runningRecordId: Long)

    @Query("DELETE FROM runningRecordToRecordTag")
    suspend fun clear()
}