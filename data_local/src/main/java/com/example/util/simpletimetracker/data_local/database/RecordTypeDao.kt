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

    @Query("SELECT * FROM recordTypes WHERE id = :id LIMIT 1")
    suspend fun get(id: Long): RecordTypeDBO?

    @Query("SELECT * FROM recordTypes WHERE name = :name LIMIT 1")
    suspend fun get(name: String): RecordTypeDBO?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: RecordTypeDBO): Long

    @Query("UPDATE recordTypes SET hidden = 1 WHERE id = :id")
    suspend fun archive(id: Long)

    @Query("UPDATE recordTypes SET hidden = 0 WHERE id = :id")
    suspend fun restore(id: Long)

    @Query("DELETE FROM recordTypes")
    suspend fun clear()
}