package com.example.util.simpletimetracker.data_local.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.util.simpletimetracker.data_local.model.RecordDBO

@Dao
interface RecordDao {

    @Query("SELECT * FROM records")
    suspend fun getAll(): List<RecordDBO>

    @Query("SELECT * FROM records WHERE id = :id LIMIT 1")
    suspend fun get(id: Long): RecordDBO?

    @Insert
    suspend fun insert(record: RecordDBO)

    @Query("DELETE FROM records WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("DELETE FROM records")
    suspend fun clear()
}