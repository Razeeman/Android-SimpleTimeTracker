package com.example.util.simpletimetracker.domain.repo

import com.example.util.simpletimetracker.domain.model.Category

interface CategoryRepo {

    suspend fun getAll(): List<Category>

    suspend fun add(category: Category)

    suspend fun clear()
}