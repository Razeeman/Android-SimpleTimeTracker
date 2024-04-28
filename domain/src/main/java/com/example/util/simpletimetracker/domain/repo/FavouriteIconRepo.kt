package com.example.util.simpletimetracker.domain.repo

import com.example.util.simpletimetracker.domain.model.FavouriteIcon

interface FavouriteIconRepo {

    suspend fun getAll(): List<FavouriteIcon>

    suspend fun get(id: Long): FavouriteIcon?

    suspend fun get(icon: String): FavouriteIcon?

    suspend fun add(favouriteIcon: FavouriteIcon): Long

    suspend fun remove(id: Long)

    suspend fun clear()
}