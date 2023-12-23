package com.example.util.simpletimetracker.domain.repo

import com.example.util.simpletimetracker.domain.model.Category
import com.example.util.simpletimetracker.domain.model.RecordTypeCategory

interface RecordTypeCategoryRepo {

    suspend fun getAll(): List<RecordTypeCategory>

    suspend fun getCategoriesByType(typeId: Long): List<Category>

    suspend fun getCategoryIdsByType(typeId: Long): Set<Long>

    suspend fun getTypeIdsByCategory(categoryId: Long): Set<Long>

    suspend fun add(recordTypeCategory: RecordTypeCategory)

    suspend fun addCategories(typeId: Long, categoryIds: List<Long>)

    suspend fun removeCategories(typeId: Long, categoryIds: List<Long>)

    suspend fun addTypes(categoryId: Long, typeIds: List<Long>)

    suspend fun removeTypes(categoryId: Long, typeIds: List<Long>)

    suspend fun removeAll(categoryId: Long)

    suspend fun removeAllByType(typeId: Long)

    suspend fun clear()
}