package com.example.util.simpletimetracker.data_local.recordType

import com.example.util.simpletimetracker.data_local.base.logDataAccess
import com.example.util.simpletimetracker.data_local.base.withLockedCache
import com.example.util.simpletimetracker.domain.extension.removeIf
import com.example.util.simpletimetracker.domain.extension.replaceWith
import com.example.util.simpletimetracker.domain.recordType.model.RecordTypeGoal
import com.example.util.simpletimetracker.domain.recordType.model.RecordTypeGoal.IdData
import com.example.util.simpletimetracker.domain.recordType.repo.RecordTypeGoalRepo
import javax.inject.Inject
import kotlinx.coroutines.sync.Mutex

class RecordTypeGoalRepoImpl @Inject constructor(
    private val dao: RecordTypeGoalDao,
    private val mapper: RecordTypeGoalDataLocalMapper,
) : RecordTypeGoalRepo {

    private var cache: List<RecordTypeGoal>? = null
    private val mutex: Mutex = Mutex()

    override suspend fun getAll(): List<RecordTypeGoal> = mutex.withLockedCache(
        logMessage = "getAll",
        accessCache = { cache },
        accessSource = { dao.getAll().map(mapper::map) },
        afterSourceAccess = { cache = it },
    )

    override suspend fun get(id: Long): RecordTypeGoal? = mutex.withLockedCache(
        logMessage = "get",
        accessCache = { cache?.firstOrNull { it.id == id } },
        accessSource = { dao.get(id)?.let(mapper::map) },
    )

    override suspend fun getAllTypeGoals(): List<RecordTypeGoal> = mutex.withLockedCache(
        logMessage = "getAllTypeGoals",
        accessCache = { cache?.filter { it.isType() && it.idData.value != 0L } },
        accessSource = { dao.getAllTypeGoals().map(mapper::map) },
        afterSourceAccess = { initializeCache() },
    )

    override suspend fun getAllCategoryGoals(): List<RecordTypeGoal> = mutex.withLockedCache(
        logMessage = "getAllCategoryGoals",
        accessCache = { cache?.filter { it.isCategory() && it.idData.value != 0L } },
        accessSource = { dao.getAllCategoryGoals().map(mapper::map) },
        afterSourceAccess = { initializeCache() },
    )

    override suspend fun getAllTagGoals(): List<RecordTypeGoal> = mutex.withLockedCache(
        logMessage = "getAllTagGoals",
        accessCache = { cache?.filter { it.isTag() && it.idData.value != 0L } },
        accessSource = { dao.getAllTagGoals().map(mapper::map) },
        afterSourceAccess = { initializeCache() },
    )

    override suspend fun getByType(typeId: Long): List<RecordTypeGoal> = mutex.withLockedCache(
        logMessage = "getByType",
        accessCache = { cache?.filter { it.isType() && it.idData.value == typeId } },
        accessSource = { dao.getByType(typeId).map(mapper::map) },
        afterSourceAccess = { initializeCache() },
    )

    override suspend fun getByCategory(categoryId: Long): List<RecordTypeGoal> = mutex.withLockedCache(
        logMessage = "getByCategory",
        accessCache = { cache?.filter { it.isCategory() && it.idData.value == categoryId } },
        accessSource = { dao.getByCategory(categoryId).map(mapper::map) },
        afterSourceAccess = { initializeCache() },
    )

    override suspend fun getByTag(tagId: Long): List<RecordTypeGoal> = mutex.withLockedCache(
        logMessage = "getByTag",
        accessCache = { cache?.filter { it.isTag() && it.idData.value == tagId } },
        accessSource = { dao.getByTag(tagId).map(mapper::map) },
        afterSourceAccess = { initializeCache() },
    )

    override suspend fun add(recordTypeGoal: RecordTypeGoal): Long = mutex.withLockedCache(
        logMessage = "add",
        accessSource = { dao.insert(recordTypeGoal.let(mapper::map)) },
        afterSourceAccess = { id ->
            cache = cache?.replaceWith(recordTypeGoal.copy(id = id)) { it.id == id }
        },
    )

    override suspend fun remove(id: Long) = mutex.withLockedCache(
        logMessage = "remove",
        accessSource = { dao.delete(id) },
        afterSourceAccess = { cache = cache?.removeIf { it.id == id } },
    )

    override suspend fun removeByType(typeId: Long) = mutex.withLockedCache(
        logMessage = "removeByType",
        accessSource = { dao.deleteByType(typeId) },
        afterSourceAccess = { cache = cache?.removeIf { it.isType() && it.idData.value == typeId } },
    )

    override suspend fun removeByCategory(categoryId: Long) = mutex.withLockedCache(
        logMessage = "removeByCategory",
        accessSource = { dao.deleteByCategory(categoryId) },
        afterSourceAccess = { cache = cache?.removeIf { it.isCategory() && it.idData.value == categoryId } },
    )

    override suspend fun removeByTag(tagId: Long) = mutex.withLockedCache(
        logMessage = "removeByTag",
        accessSource = { dao.deleteByTag(tagId) },
        afterSourceAccess = { cache = cache?.removeIf { it.isTag() && it.idData.value == tagId } },
    )

    override suspend fun clear() = mutex.withLockedCache(
        logMessage = "clear",
        accessSource = { dao.clear() },
        afterSourceAccess = { cache = null },
    )

    private suspend fun initializeCache() {
        logDataAccess("initializeCache")
        cache = dao.getAll().map(mapper::map)
    }

    private fun RecordTypeGoal.isType(): Boolean {
        return idData is IdData.Type
    }

    private fun RecordTypeGoal.isCategory(): Boolean {
        return idData is IdData.Category
    }

    private fun RecordTypeGoal.isTag(): Boolean {
        return idData is IdData.Tag
    }
}