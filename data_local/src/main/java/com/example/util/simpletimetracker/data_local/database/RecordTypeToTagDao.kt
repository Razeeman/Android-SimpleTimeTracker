package com.example.util.simpletimetracker.data_local.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.util.simpletimetracker.data_local.model.RecordTypeToTagDBO

@Dao
interface RecordTypeToTagDao {

    @Query("SELECT * FROM recordTypeToTag")
    suspend fun getAll(): List<RecordTypeToTagDBO>

    @Query("SELECT record_tag_id FROM recordTypeToTag WHERE record_type_id = :typeId")
    suspend fun getTagIdsByType(typeId: Long): List<Long>

    @Query("SELECT record_type_id FROM recordTypeToTag WHERE record_tag_id = :tagId")
    suspend fun getTypeIdsByTag(tagId: Long): List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(recordTypeToTagDBO: List<RecordTypeToTagDBO>)

    @Delete
    suspend fun delete(recordTypeToTagDBO: List<RecordTypeToTagDBO>)

    @Query("DELETE FROM recordTypeToTag WHERE record_tag_id = :tagId")
    suspend fun deleteAll(tagId: Long)

    @Query("DELETE FROM recordTypeToTag WHERE record_type_id = :typeId")
    suspend fun deleteAllByType(typeId: Long)

    @Query("DELETE FROM recordTypeToTag")
    suspend fun clear()
}