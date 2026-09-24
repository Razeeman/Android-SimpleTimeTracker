package com.example.util.simpletimetracker.data_local.recordTag

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface RecordToRecordTagDao {

    @Query("SELECT * FROM recordToRecordTag")
    suspend fun getAll(): List<RecordToRecordTagDBO>

    @Query(
        "SELECT * FROM recordToRecordTag " +
            "WHERE record_id > :recordId " +
            "OR (record_id = :recordId AND record_tag_id > :recordTagId) " +
            "ORDER BY record_id, record_tag_id LIMIT :limit",
    )
    suspend fun getAfter(
        recordId: Long,
        recordTagId: Long,
        limit: Int,
    ): List<RecordToRecordTagDBO>

    @Query("SELECT record_id FROM recordToRecordTag WHERE record_tag_id = :tagId")
    suspend fun getRecordIdsByTagId(tagId: Long): List<Long>

    @Query(
        "SELECT record_tag_id, COUNT(*) AS record_count " +
            "FROM recordToRecordTag GROUP BY record_tag_id",
    )
    suspend fun getRecordCountsByTag(): List<RecordToRecordTagCountDBO>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(recordToRecordTagDBO: List<RecordToRecordTagDBO>)

    @Query("DELETE FROM recordToRecordTag WHERE record_tag_id = :tagId")
    suspend fun deleteAllByTagId(tagId: Long)
}