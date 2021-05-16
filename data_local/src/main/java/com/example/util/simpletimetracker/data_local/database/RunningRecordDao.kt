package com.example.util.simpletimetracker.data_local.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.util.simpletimetracker.data_local.model.RunningRecordDBO

@Dao
interface RunningRecordDao {

    @Query("SELECT * FROM runningRecords")
    suspend fun getAll(): List<RunningRecordDBO>

    @Query("SELECT * FROM runningRecords WHERE id = :id LIMIT 1")
    suspend fun get(id: Long): RunningRecordDBO?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: RunningRecordDBO)

    @Query("DELETE FROM runningRecords WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("UPDATE runningRecords SET tag_id = 0 WHERE tag_id = :tagId")
    suspend fun removeTag(tagId: Long)

    @Query("DELETE FROM runningRecords")
    suspend fun clear()
}