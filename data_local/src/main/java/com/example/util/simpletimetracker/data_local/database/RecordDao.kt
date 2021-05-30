package com.example.util.simpletimetracker.data_local.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.util.simpletimetracker.data_local.model.RecordDBO

@Dao
interface RecordDao {

    @Query("SELECT * FROM records")
    suspend fun getAll(): List<RecordDBO>

    @Query("SELECT * FROM records WHERE type_id IN (:typesIds)")
    suspend fun getByType(typesIds: List<Long>): List<RecordDBO>

    @Query("SELECT * FROM records WHERE tag_id IN (:tagIds)")
    suspend fun getByTag(tagIds: List<Long>): List<RecordDBO>

    @Query("SELECT * FROM records WHERE id = :id LIMIT 1")
    suspend fun get(id: Long): RecordDBO?

    @Query("SELECT * FROM records WHERE time_started < :end AND time_ended > :start")
    suspend fun getFromRange(start: Long, end: Long): List<RecordDBO>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: RecordDBO)

    @Query("DELETE FROM records WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("DELETE FROM records WHERE type_id = :typeId")
    suspend fun deleteByType(typeId: Long)

    @Query("UPDATE records SET tag_id = 0 WHERE tag_id = :tagId")
    suspend fun removeTag(tagId: Long)

    @Query("DELETE FROM records")
    suspend fun clear()
}