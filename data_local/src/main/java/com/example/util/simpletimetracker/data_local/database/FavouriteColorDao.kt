package com.example.util.simpletimetracker.data_local.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.util.simpletimetracker.data_local.model.FavouriteColorDBO

@Dao
interface FavouriteColorDao {

    @Query("SELECT * FROM favouriteColors")
    suspend fun getAll(): List<FavouriteColorDBO>

    @Query("SELECT * FROM favouriteColors WHERE id = :id LIMIT 1")
    suspend fun get(id: Long): FavouriteColorDBO?

    @Query("SELECT * FROM favouriteColors WHERE color_int = :text LIMIT 1")
    suspend fun get(text: String): FavouriteColorDBO?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(comment: FavouriteColorDBO): Long

    @Query("DELETE FROM favouriteColors WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("DELETE FROM favouriteColors")
    suspend fun clear()
}