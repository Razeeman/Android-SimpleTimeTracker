package com.example.util.simpletimetracker.data_local.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.util.simpletimetracker.data_local.model.RecordTypeDBO

@Dao
interface RecordTypeDao {

    @Query("SELECT * FROM recordTypes")
    suspend fun getAll(): List<RecordTypeDBO>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: RecordTypeDBO)

    @Query("DELETE FROM recordTypes WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("DELETE FROM recordTypes")
    suspend fun clear()
}