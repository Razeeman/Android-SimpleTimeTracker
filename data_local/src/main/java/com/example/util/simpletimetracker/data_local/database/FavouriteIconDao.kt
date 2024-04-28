package com.example.util.simpletimetracker.data_local.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.util.simpletimetracker.data_local.model.FavouriteIconDBO
import com.example.util.simpletimetracker.data_local.model.RecordTypeDBO

@Dao
interface FavouriteIconDao {

    @Query("SELECT * FROM favouriteIcons")
    suspend fun getAll(): List<FavouriteIconDBO>

    @Query("SELECT * FROM favouriteIcons WHERE id = :id LIMIT 1")
    suspend fun get(id: Long): FavouriteIconDBO?

    @Query("SELECT * FROM favouriteIcons WHERE icon = :icon LIMIT 1")
    suspend fun get(icon: String): FavouriteIconDBO?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: FavouriteIconDBO): Long

    @Query("DELETE FROM favouriteIcons WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("DELETE FROM favouriteIcons")
    suspend fun clear()
}