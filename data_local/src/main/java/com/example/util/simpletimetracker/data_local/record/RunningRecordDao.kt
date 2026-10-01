package com.example.util.simpletimetracker.data_local.record

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.util.simpletimetracker.data_local.recordTag.RunningRecordToRecordTagDBO

@Dao
abstract class RunningRecordDao {

    @Transaction
    @Query("select exists(select 1 from runningRecords)")
    abstract suspend fun isEmpty(): Long

    @Transaction
    @Query("SELECT * FROM runningRecords")
    abstract suspend fun getAll(): List<RunningRecordWithRecordTagsDBO>

    @Transaction
    @Query("SELECT * FROM runningRecords WHERE id = :id LIMIT 1")
    abstract suspend fun get(id: Long): RunningRecordWithRecordTagsDBO?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    protected abstract suspend fun insertRecord(record: RunningRecordDBO): Long

    @Query("DELETE FROM runningRecords WHERE id = :id")
    protected abstract suspend fun deleteRecord(id: Long)

    @Query("DELETE FROM runningRecords")
    protected abstract suspend fun clearRecords()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    protected abstract suspend fun insertRecordTags(recordTags: List<RunningRecordToRecordTagDBO>)

    @Query("DELETE FROM runningRecordToRecordTag WHERE running_record_id = :recordId")
    protected abstract suspend fun deleteRecordTags(recordId: Long)

    @Query("DELETE FROM runningRecordToRecordTag")
    protected abstract suspend fun clearRecordTags()

    @Transaction
    open suspend fun insert(
        record: RunningRecordDBO,
        recordTags: List<RunningRecordToRecordTagDBO>,
    ): Long {
        // In case record was removed.
        val recordId = insertRecord(record)
        deleteRecordTags(recordId)
        insertRecordTags(recordTags.map { it.copy(runningRecordId = recordId) })
        return recordId
    }

    @Transaction
    open suspend fun delete(id: Long) {
        deleteRecordTags(id)
        deleteRecord(id)
    }

    @Transaction
    open suspend fun clear() {
        clearRecordTags()
        clearRecords()
    }
}
