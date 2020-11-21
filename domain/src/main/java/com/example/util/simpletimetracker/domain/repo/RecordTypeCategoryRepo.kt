package com.example.util.simpletimetracker.domain.repo

import com.example.util.simpletimetracker.domain.model.Category

interface RecordTypeCategoryRepo {

    suspend fun getCategoriesByType(typeId: Long): List<Category>

    suspend fun getCategoryIdsByType(typeId: Long): List<Long>

    suspend fun add(typeId: Long, categoryIds: List<Long>)

    suspend fun remove(typeId: Long, categoryIds: List<Long>)

    suspend fun removeAll(categoryId: Long)

    suspend fun clear()
}