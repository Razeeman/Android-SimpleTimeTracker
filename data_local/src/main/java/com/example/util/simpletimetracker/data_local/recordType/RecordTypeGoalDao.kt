package com.example.util.simpletimetracker.data_local.recordType

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction

@Dao
interface RecordTypeGoalDao {

    @Query("SELECT * FROM recordTypeGoals")
    suspend fun getAll(): List<RecordTypeGoalDBO>

    @Query("SELECT * FROM recordTypeGoals WHERE id = :id LIMIT 1")
    suspend fun get(id: Long): RecordTypeGoalDBO?

    @Query("SELECT * FROM recordTypeGoals WHERE owner_type == 0")
    suspend fun getAllTypeGoals(): List<RecordTypeGoalDBO>

    @Query("SELECT * FROM recordTypeGoals WHERE owner_type == 1")
    suspend fun getAllCategoryGoals(): List<RecordTypeGoalDBO>

    // TODO TAG GOAL remove owner_type checking, should not depend on it.
    @Query("SELECT * FROM recordTypeGoals WHERE owner_type == 2")
    suspend fun getAllTagGoals(): List<RecordTypeGoalDBO>

    @Transaction
    @Query("SELECT * FROM recordTypeGoals WHERE owner_type == 0 AND owner_id = :typeId")
    suspend fun getByType(typeId: Long): List<RecordTypeGoalDBO>

    @Transaction
    @Query("SELECT * FROM recordTypeGoals WHERE owner_type == 1 AND owner_id = :categoryId")
    suspend fun getByCategory(categoryId: Long): List<RecordTypeGoalDBO>

    @Transaction
    @Query("SELECT * FROM recordTypeGoals WHERE owner_type == 2 AND owner_id = :tagId")
    suspend fun getByTag(tagId: Long): List<RecordTypeGoalDBO>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(recordTypeGoal: RecordTypeGoalDBO): Long

    @Query("DELETE FROM recordTypeGoals WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("DELETE FROM recordTypeGoals WHERE owner_type == 0 AND owner_id = :typeId")
    suspend fun deleteByType(typeId: Long)

    @Query("DELETE FROM recordTypeGoals WHERE owner_type == 1 AND owner_id = :categoryId")
    suspend fun deleteByCategory(categoryId: Long)

    @Query("DELETE FROM recordTypeGoals WHERE owner_type == 2 AND owner_id = :tagId")
    suspend fun deleteByTag(tagId: Long)

    @Query("DELETE FROM recordTypeGoals")
    suspend fun clear()
}