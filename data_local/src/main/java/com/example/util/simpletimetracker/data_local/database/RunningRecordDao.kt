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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: RunningRecordDBO)

    @Query("DELETE FROM runningRecords WHERE name = :name")
    suspend fun delete(name: String)

    @Query("DELETE FROM runningRecords")
    suspend fun clear()
}