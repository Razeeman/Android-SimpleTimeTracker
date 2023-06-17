package com.example.util.simpletimetracker.domain.repo

import com.example.util.simpletimetracker.domain.model.FavouriteComment

interface FavouriteCommentRepo {

    suspend fun getAll(): List<FavouriteComment>

    suspend fun get(id: Long): FavouriteComment?

    suspend fun get(text: String): FavouriteComment?

    suspend fun add(comment: FavouriteComment): Long

    suspend fun remove(id: Long)

    suspend fun clear()
}