package com.example.util.simpletimetracker.data_local.record

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.util.simpletimetracker.data_local.recordTag.RecordToRecordTagDBO

@Dao
abstract class RecordDao {

    @Transaction
    @Query("select exists(select 1 from records)")
    abstract suspend fun isEmpty(): Long

    @Transaction
    @Query("SELECT * FROM records")
    abstract suspend fun getAll(): List<RecordWithRecordTagsDBO>

    @Transaction
    @Query("SELECT * FROM records WHERE id > :id ORDER BY id LIMIT :limit")
    abstract suspend fun getAfterId(id: Long, limit: Int): List<RecordWithRecordTagsDBO>

    @Transaction
    @Query("SELECT * FROM records WHERE type_id IN (:typesIds)")
    abstract suspend fun getByType(typesIds: Set<Long>): List<RecordWithRecordTagsDBO>

    @Transaction
    @Query("SELECT * FROM records WHERE type_id IN (:typesIds) AND comment != \"\"")
    abstract suspend fun getByTypeWithAnyComment(typesIds: Set<Long>): List<RecordWithRecordTagsDBO>

    @Transaction
    @Query("SELECT * FROM records WHERE instr(lower(comment), lower(:text)) > 0")
    abstract suspend fun searchComment(text: String): List<RecordWithRecordTagsDBO>

    @Query(
        "SELECT comment FROM records " +
            "WHERE instr(lower(comment), lower(:text)) > 0 AND comment != :text " +
            "GROUP BY comment " +
            "ORDER BY MAX(time_started) DESC " +
            "LIMIT :limit",
    )
    abstract suspend fun searchSimilarComments(text: String, limit: Int): List<String>

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
    abstract suspend fun getRecentComments(typeId: Long, limit: Int): List<String>

    @Transaction
    @Query("SELECT * FROM records WHERE type_id IN (:typesIds) AND instr(lower(comment), lower(:text)) > 0")
    abstract suspend fun searchByTypeWithComment(typesIds: Set<Long>, text: String): List<RecordWithRecordTagsDBO>

    @Transaction
    @Query("SELECT * FROM records WHERE comment != \"\"")
    abstract suspend fun searchAnyComments(): List<RecordWithRecordTagsDBO>

    @Transaction
    @Query("SELECT * FROM records WHERE EXISTS(SELECT 1 FROM recordToRecordTag WHERE record_id = records.id AND record_tag_id IN (:tagIds))")
    abstract suspend fun getTagged(tagIds: Set<Long>): List<RecordWithRecordTagsDBO>

    @Transaction
    @Query("SELECT * FROM records WHERE NOT EXISTS(SELECT 1 FROM recordToRecordTag WHERE record_id = records.id)")
    abstract suspend fun getUntagged(): List<RecordWithRecordTagsDBO>

    @Transaction
    @Query("SELECT * FROM records WHERE id = :id LIMIT 1")
    abstract suspend fun get(id: Long): RecordWithRecordTagsDBO?

    @Transaction
    @Query("SELECT * FROM records WHERE time_started < :end AND time_ended > :start")
    abstract suspend fun getFromRange(start: Long, end: Long): List<RecordWithRecordTagsDBO>

    @Transaction
    @Query("SELECT * FROM records WHERE type_id IN (:typesIds) AND time_started < :end AND time_ended > :start")
    abstract suspend fun getFromRangeByType(typesIds: Set<Long>, start: Long, end: Long): List<RecordWithRecordTagsDBO>

    @Transaction
    @Query(
        "SELECT * FROM records " +
            "WHERE time_ended <= :timeStarted AND type_id NOT IN (:ignoreTypeIds) " +
            "ORDER BY time_ended DESC LIMIT 1",
    )
    abstract suspend fun getPrev(timeStarted: Long, ignoreTypeIds: List<Long>): RecordWithRecordTagsDBO?

    @Transaction
    @Query("SELECT * FROM records WHERE time_started >= :timeEnded ORDER BY time_started ASC LIMIT 1")
    abstract suspend fun getNext(timeEnded: Long): RecordWithRecordTagsDBO?

    @Transaction
    @Query("SELECT time_started FROM records WHERE time_started < :fromTimestamp ORDER BY time_started DESC LIMIT 1")
    abstract suspend fun getPrevTimeStarted(fromTimestamp: Long): Long?

    @Transaction
    @Query("SELECT time_started FROM records WHERE time_started > :fromTimestamp ORDER BY time_started ASC LIMIT 1")
    abstract suspend fun getNextTimeStarted(fromTimestamp: Long): Long?

    @Transaction
    @Query("SELECT time_ended FROM records WHERE time_ended < :fromTimestamp ORDER BY time_ended DESC LIMIT 1")
    abstract suspend fun getPrevTimeEnded(fromTimestamp: Long): Long?

    @Transaction
    @Query("SELECT time_ended FROM records WHERE time_ended > :fromTimestamp ORDER BY time_ended ASC LIMIT 1")
    abstract suspend fun getNextTimeEnded(fromTimestamp: Long): Long?

    @Transaction
    @Query("SELECT * FROM records WHERE time_started = :timeStarted")
    abstract suspend fun getByTimeStarted(timeStarted: Long): List<RecordWithRecordTagsDBO>

    @Transaction
    @Query("SELECT * FROM records WHERE time_ended = :timeEnded")
    abstract suspend fun getByTimeEnded(timeEnded: Long): List<RecordWithRecordTagsDBO>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    protected abstract suspend fun insertRecord(record: RecordDBO): Long

    @Query("UPDATE records SET type_id = :typeId, comment = :comment WHERE id = :recordId")
    protected abstract suspend fun updateRecord(
        recordId: Long,
        typeId: Long,
        comment: String,
    ): Int

    @Query("UPDATE records SET time_ended = :timeEnded WHERE id = :recordId")
    abstract suspend fun updateTimeEnded(recordId: Long, timeEnded: Long)

    @Query("DELETE FROM records WHERE id = :id")
    protected abstract suspend fun deleteRecord(id: Long)

    @Query("DELETE FROM records WHERE type_id = :typeId")
    protected abstract suspend fun deleteRecordsByType(typeId: Long)

    @Query("DELETE FROM records")
    protected abstract suspend fun clearRecords()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    protected abstract suspend fun insertRecordTags(recordTags: List<RecordToRecordTagDBO>)

    @Query("DELETE FROM recordToRecordTag WHERE record_id = :recordId")
    protected abstract suspend fun deleteRecordTags(recordId: Long)

    @Query("DELETE FROM recordToRecordTag WHERE record_id IN (SELECT id FROM records WHERE type_id = :typeId)")
    protected abstract suspend fun deleteRecordTagsByType(typeId: Long)

    @Query("DELETE FROM recordToRecordTag")
    protected abstract suspend fun clearRecordTags()

    @Transaction
    open suspend fun insert(
        record: RecordDBO,
        recordTags: List<RecordToRecordTagDBO>,
    ): Long {
        val recordId = insertRecord(record)
        deleteRecordTags(recordId)
        insertRecordTags(recordTags.map { it.copy(recordId = recordId) })
        return recordId
    }

    @Transaction
    open suspend fun update(
        recordId: Long,
        typeId: Long,
        comment: String,
        recordTags: List<RecordToRecordTagDBO>,
    ) {
        // In case record was removed.
        if (updateRecord(recordId, typeId, comment) == 0) return
        deleteRecordTags(recordId)
        insertRecordTags(recordTags.map { it.copy(recordId = recordId) })
    }

    @Transaction
    open suspend fun delete(id: Long) {
        deleteRecordTags(id)
        deleteRecord(id)
    }

    @Transaction
    open suspend fun deleteByType(typeId: Long) {
        deleteRecordTagsByType(typeId)
        deleteRecordsByType(typeId)
    }

    @Transaction
    open suspend fun clear() {
        clearRecordTags()
        clearRecords()
    }
}
