package com.example.util.simpletimetracker.data_local.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.util.simpletimetracker.data_local.model.RecordTypeToDefaultTagDBO

@Dao
interface RecordTypeToDefaultTagDao {

    @Query("SELECT * FROM recordTypeToDefaultTag")
    suspend fun getAll(): List<RecordTypeToDefaultTagDBO>

    @Query("SELECT record_tag_id FROM recordTypeToDefaultTag WHERE record_type_id = :typeId")
    suspend fun getTagIdsByType(typeId: Long): List<Long>

    @Query("SELECT record_type_id FROM recordTypeToDefaultTag WHERE record_tag_id = :tagId")
    suspend fun getTypeIdsByTag(tagId: Long): List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(recordTypeToDefaultTagDBO: List<RecordTypeToDefaultTagDBO>)

    @Delete
    suspend fun delete(recordTypeToDefaultTagDBO: List<RecordTypeToDefaultTagDBO>)

    @Query("DELETE FROM recordTypeToDefaultTag WHERE record_tag_id = :tagId")
    suspend fun deleteAll(tagId: Long)

    @Query("DELETE FROM recordTypeToDefaultTag WHERE record_type_id = :typeId")
    suspend fun deleteAllByType(typeId: Long)

    @Query("DELETE FROM recordTypeToDefaultTag")
    suspend fun clear()
}