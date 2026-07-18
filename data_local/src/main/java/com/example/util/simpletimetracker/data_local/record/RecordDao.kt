package com.example.util.simpletimetracker.data_local.record

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction

@Dao
interface RecordDao {

    @Transaction
    @Query("select exists(select 1 from records)")
    suspend fun isEmpty(): Long

    @Transaction
    @Query("SELECT * FROM records")
    suspend fun getAll(): List<RecordWithRecordTagsDBO>

    @Transaction
    @Query("SELECT * FROM records WHERE type_id IN (:typesIds)")
    suspend fun getByType(typesIds: Set<Long>): List<RecordWithRecordTagsDBO>

    @Transaction
    @Query("SELECT * FROM records WHERE type_id IN (:typesIds) AND comment != \"\"")
    suspend fun getByTypeWithAnyComment(typesIds: Set<Long>): List<RecordWithRecordTagsDBO>

    @Transaction
    @Query("SELECT * FROM records WHERE instr(lower(comment), lower(:text)) > 0")
    suspend fun searchComment(text: String): List<RecordWithRecordTagsDBO>

    @Query(
        "SELECT comment FROM records " +
            "WHERE instr(lower(comment), lower(:text)) > 0 AND comment != :text " +
            "GROUP BY comment " +
            "ORDER BY MAX(time_started) DESC " +
            "LIMIT :limit",
    )
    suspend fun searchSimilarComments(text: String, limit: Int): List<String>

    @Query(
        "SELECT comment FROM (" +
            "SELECT comment, time_started FROM records " +
            "WHERE type_id = :typeId AND comment != \"\" " +
            "UNION ALL " +
            "SELECT comment, time_started FROM runningRecords " +
            "WHERE id = :typeId AND comment != \"\"" +
            ") " +
            "GROUP BY comment " +
            "ORDER BY MAX(time_started) DESC " +
            "LIMIT :limit",
    )
    suspend fun getRecentComments(typeId: Long, limit: Int): List<String>

    @Transaction
    @Query("SELECT * FROM records WHERE type_id IN (:typesIds) AND instr(lower(comment), lower(:text)) > 0")
    suspend fun searchByTypeWithComment(typesIds: Set<Long>, text: String): List<RecordWithRecordTagsDBO>

    @Transaction
    @Query("SELECT * FROM records WHERE comment != \"\"")
    suspend fun searchAnyComments(): List<RecordWithRecordTagsDBO>

    @Transaction
    @Query("SELECT * FROM records WHERE EXISTS(SELECT 1 FROM recordToRecordTag WHERE record_id = records.id AND record_tag_id IN (:tagIds))")
    suspend fun getTagged(tagIds: Set<Long>): List<RecordWithRecordTagsDBO>

    @Transaction
    @Query("SELECT * FROM records WHERE NOT EXISTS(SELECT 1 FROM recordToRecordTag WHERE record_id = records.id)")
    suspend fun getUntagged(): List<RecordWithRecordTagsDBO>

    @Transaction
    @Query("SELECT * FROM records WHERE id = :id LIMIT 1")
    suspend fun get(id: Long): RecordWithRecordTagsDBO?

    @Transaction
    @Query("SELECT * FROM records WHERE time_started < :end AND time_ended > :start")
    suspend fun getFromRange(start: Long, end: Long): List<RecordWithRecordTagsDBO>

    @Transaction
    @Query("SELECT * FROM records WHERE type_id IN (:typesIds) AND time_started < :end AND time_ended > :start")
    suspend fun getFromRangeByType(typesIds: Set<Long>, start: Long, end: Long): List<RecordWithRecordTagsDBO>

    @Transaction
    @Query(
        "SELECT * FROM records " +
            "WHERE time_ended <= :timeStarted AND type_id NOT IN (:ignoreTypeIds) " +
            "ORDER BY time_ended DESC LIMIT 1",
    )
    suspend fun getPrev(timeStarted: Long, ignoreTypeIds: List<Long>): RecordWithRecordTagsDBO?

    @Transaction
    @Query("SELECT * FROM records WHERE time_started >= :timeEnded ORDER BY time_started ASC LIMIT 1")
    suspend fun getNext(timeEnded: Long): RecordWithRecordTagsDBO?

    @Transaction
    @Query("SELECT time_started FROM records WHERE time_started < :fromTimestamp ORDER BY time_started DESC LIMIT 1")
    suspend fun getPrevTimeStarted(fromTimestamp: Long): Long?

    @Transaction
    @Query("SELECT time_started FROM records WHERE time_started > :fromTimestamp ORDER BY time_started ASC LIMIT 1")
    suspend fun getNextTimeStarted(fromTimestamp: Long): Long?

    @Transaction
    @Query("SELECT time_ended FROM records WHERE time_ended < :fromTimestamp ORDER BY time_ended DESC LIMIT 1")
    suspend fun getPrevTimeEnded(fromTimestamp: Long): Long?

    @Transaction
    @Query("SELECT time_ended FROM records WHERE time_ended > :fromTimestamp ORDER BY time_ended ASC LIMIT 1")
    suspend fun getNextTimeEnded(fromTimestamp: Long): Long?

    @Transaction
    @Query("SELECT * FROM records WHERE time_started = :timeStarted")
    suspend fun getByTimeStarted(timeStarted: Long): List<RecordWithRecordTagsDBO>

    @Transaction
    @Query("SELECT * FROM records WHERE time_ended = :timeEnded")
    suspend fun getByTimeEnded(timeEnded: Long): List<RecordWithRecordTagsDBO>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: RecordDBO): Long

    @Query("UPDATE records SET type_id = :typeId, comment = :comment WHERE id = :recordId")
    suspend fun update(
        recordId: Long,
        typeId: Long,
        comment: String,
    )

    @Query("UPDATE records SET time_ended = :timeEnded WHERE id = :recordId")
    suspend fun updateTimeEnded(recordId: Long, timeEnded: Long)

    @Query("DELETE FROM records WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("DELETE FROM records WHERE type_id = :typeId")
    suspend fun deleteByType(typeId: Long)

    @Query("DELETE FROM records")
    suspend fun clear()
}
