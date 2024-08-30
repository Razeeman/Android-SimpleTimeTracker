package com.example.util.simpletimetracker.data_local.repo

import com.example.util.simpletimetracker.data_local.database.CategoryDao
import com.example.util.simpletimetracker.data_local.utils.withLockedCache
import com.example.util.simpletimetracker.data_local.mapper.CategoryDataLocalMapper
import com.example.util.simpletimetracker.data_local.utils.removeIf
import com.example.util.simpletimetracker.domain.model.Category
import com.example.util.simpletimetracker.domain.model.RecordType
import com.example.util.simpletimetracker.domain.repo.CategoryRepo
import kotlinx.coroutines.sync.Mutex
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CategoryRepoImpl @Inject constructor(
    private val categoryDao: CategoryDao,
    private val categoryDataLocalMapper: CategoryDataLocalMapper,
) : CategoryRepo {

    private var cache: List<Category>? = null
    private val mutex: Mutex = Mutex()

    override suspend fun getAll(): List<Category> = mutex.withLockedCache(
        logMessage = "getAll",
        accessCache = { cache },
        accessSource = { categoryDao.getAll().map(categoryDataLocalMapper::map) },
        afterSourceAccess = { cache = it },
    )

    override suspend fun get(id: Long): Category? = mutex.withLockedCache(
        logMessage = "get id",
        accessCache = { cache?.firstOrNull { it.id == id } },
        accessSource = { categoryDao.get(id)?.let(categoryDataLocalMapper::map) },
    )

    override suspend fun get(name: String): Category? = mutex.withLockedCache(
        logMessage = "get name",
        accessCache = { cache?.firstOrNull { it.name == name } },
        accessSource = { categoryDao.get(name)?.let(categoryDataLocalMapper::map) },
    )

    override suspend fun add(category: Category): Long = mutex.withLockedCache(
        logMessage = "add",
        accessSource = { categoryDao.insert(category.let(categoryDataLocalMapper::map)) },
        afterSourceAccess = { cache = null },
    )

    override suspend fun remove(id: Long) = mutex.withLockedCache(
        logMessage = "remove",
        accessSource = { categoryDao.delete(id) },
        afterSourceAccess = { cache = cache?.removeIf { it.id == id } },
    )

    override suspend fun clear() = mutex.withLockedCache(
        logMessage = "clear",
        accessSource = { categoryDao.clear() },
        afterSourceAccess = { cache = null },
    )
}