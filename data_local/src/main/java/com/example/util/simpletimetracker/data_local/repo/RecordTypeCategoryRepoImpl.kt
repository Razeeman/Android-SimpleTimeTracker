package com.example.util.simpletimetracker.data_local.repo

import com.example.util.simpletimetracker.data_local.database.RecordTypeCategoryDao
import com.example.util.simpletimetracker.data_local.mapper.RecordTypeCategoryDataLocalMapper
import com.example.util.simpletimetracker.data_local.utils.removeIf
import com.example.util.simpletimetracker.data_local.utils.withLockedCache
import com.example.util.simpletimetracker.domain.model.RecordTypeCategory
import com.example.util.simpletimetracker.domain.repo.RecordTypeCategoryRepo
import kotlinx.coroutines.sync.Mutex
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecordTypeCategoryRepoImpl @Inject constructor(
    private val recordTypeCategoryDao: RecordTypeCategoryDao,
    private val recordTypeCategoryDataLocalMapper: RecordTypeCategoryDataLocalMapper,
) : RecordTypeCategoryRepo {

    private var cache: List<RecordTypeCategory>? = null
    private val mutex: Mutex = Mutex()

    override suspend fun getAll(): List<RecordTypeCategory> = mutex.withLockedCache(
        logMessage = "get all",
        accessCache = { cache },
        accessSource = { recordTypeCategoryDao.getAll().map(recordTypeCategoryDataLocalMapper::map) },
        afterSourceAccess = { cache = it },
    )

    override suspend fun getCategoryIdsByType(typeId: Long): Set<Long> = mutex.withLockedCache(
        logMessage = "get category ids",
        accessCache = { cache?.filter { it.recordTypeId == typeId }?.map { it.categoryId }?.toSet() },
        accessSource = { recordTypeCategoryDao.getCategoryIdsByType(typeId).toSet() },
    )

    override suspend fun add(recordTypeCategory: RecordTypeCategory) = mutex.withLockedCache(
        logMessage = "add",
        accessSource = {
            recordTypeCategory
                .let(recordTypeCategoryDataLocalMapper::map)
                .let { recordTypeCategoryDao.insert(listOf(it)) }
        },
        afterSourceAccess = { cache = null },
    )

    override suspend fun addCategories(typeId: Long, categoryIds: List<Long>) = mutex.withLockedCache(
        logMessage = "add categories",
        accessSource = {
            categoryIds.map {
                recordTypeCategoryDataLocalMapper.map(typeId = typeId, categoryId = it)
            }.let { recordTypeCategoryDao.insert(it) }
        },
        afterSourceAccess = { cache = null },
    )

    override suspend fun removeCategories(typeId: Long, categoryIds: List<Long>) = mutex.withLockedCache(
        logMessage = "remove categories",
        accessSource = {
            categoryIds.map {
                recordTypeCategoryDataLocalMapper.map(typeId = typeId, categoryId = it)
            }.let { recordTypeCategoryDao.delete(it) }
        },
        afterSourceAccess = { cache = null },
    )

    override suspend fun getTypeIdsByCategory(categoryId: Long): Set<Long> = mutex.withLockedCache(
        logMessage = "get type ids",
        accessCache = { cache?.filter { it.categoryId == categoryId }?.map { it.recordTypeId }?.toSet() },
        accessSource = { recordTypeCategoryDao.getTypeIdsByCategory(categoryId).toSet() },
    )

    override suspend fun addTypes(categoryId: Long, typeIds: List<Long>) = mutex.withLockedCache(
        logMessage = "add types",
        accessSource = {
            typeIds.map {
                recordTypeCategoryDataLocalMapper.map(typeId = it, categoryId = categoryId)
            }.let { recordTypeCategoryDao.insert(it) }
        },
        afterSourceAccess = { cache = null },
    )

    override suspend fun removeTypes(categoryId: Long, typeIds: List<Long>) = mutex.withLockedCache(
        logMessage = "remove types",
        accessSource = {
            typeIds.map {
                recordTypeCategoryDataLocalMapper.map(typeId = it, categoryId = categoryId)
            }.let { recordTypeCategoryDao.delete(it) }
        },
        afterSourceAccess = { cache = null },
    )

    override suspend fun removeAll(categoryId: Long) = mutex.withLockedCache(
        logMessage = "removeAll",
        accessSource = { recordTypeCategoryDao.deleteAll(categoryId) },
        afterSourceAccess = { cache = cache?.removeIf { it.categoryId == categoryId } },
    )

    override suspend fun removeAllByType(typeId: Long) = mutex.withLockedCache(
        logMessage = "removeAllByType",
        accessSource = { recordTypeCategoryDao.deleteAllByType(typeId) },
        afterSourceAccess = { cache = cache?.removeIf { it.recordTypeId == typeId } },
    )

    override suspend fun clear() = mutex.withLockedCache(
        logMessage = "clear",
        accessSource = { recordTypeCategoryDao.clear() },
        afterSourceAccess = { cache = null },
    )
}