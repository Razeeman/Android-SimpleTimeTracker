package com.example.util.simpletimetracker.data_local.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.util.simpletimetracker.data_local.model.RunningRecordDBO
import com.example.util.simpletimetracker.data_local.model.RunningRecordWithRecordTagsDBO

@Dao
interface RunningRecordDao {

    @Transaction
    @Query("select exists(select 1 from runningRecords)")
    suspend fun isEmpty(): Long

    @Transaction
    @Query("SELECT * FROM runningRecords")
    suspend fun getAll(): List<RunningRecordWithRecordTagsDBO>

    @Transaction
    @Query("SELECT * FROM runningRecords WHERE id = :id LIMIT 1")
    suspend fun get(id: Long): RunningRecordWithRecordTagsDBO?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: RunningRecordDBO): Long

    @Query("DELETE FROM runningRecords WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("DELETE FROM runningRecords")
    suspend fun clear()
}