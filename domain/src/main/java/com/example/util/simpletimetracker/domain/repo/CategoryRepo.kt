package com.example.util.simpletimetracker.domain.repo

import com.example.util.simpletimetracker.domain.model.Category

interface CategoryRepo {

    suspend fun getAll(): List<Category>

    suspend fun get(id: Long): Category?

    suspend fun add(category: Category): Long

    suspend fun remove(id: Long)

    suspend fun clear()
}