package com.example.util.simpletimetracker.data_local.favourite

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface RecordTypeToFavouriteCommentDao {

    @Query("SELECT * FROM recordTypeToFavouriteComment")
    suspend fun getAll(): List<RecordTypeToFavouriteCommentDBO>

    @Query("SELECT comment_id FROM recordTypeToFavouriteComment WHERE record_type_id = :typeId")
    suspend fun getCommentIdsByType(typeId: Long): List<Long>

    @Query("SELECT record_type_id FROM recordTypeToFavouriteComment WHERE comment_id = :commentId")
    suspend fun getTypeIdsByComment(commentId: Long): List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(recordTypeToFavouriteCommentDBO: List<RecordTypeToFavouriteCommentDBO>)

    @Delete
    suspend fun delete(recordTypeToFavouriteCommentDBO: List<RecordTypeToFavouriteCommentDBO>)

    @Query("DELETE FROM recordTypeToFavouriteComment WHERE comment_id = :commentId")
    suspend fun deleteAll(commentId: Long)

    @Query("DELETE FROM recordTypeToFavouriteComment WHERE record_type_id = :typeId")
    suspend fun deleteAllByType(typeId: Long)

    @Query("DELETE FROM recordTypeToFavouriteComment")
    suspend fun clear()
}