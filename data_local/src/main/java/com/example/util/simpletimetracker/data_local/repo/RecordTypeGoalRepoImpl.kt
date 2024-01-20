package com.example.util.simpletimetracker.data_local.repo

import com.example.util.simpletimetracker.data_local.database.RecordTypeGoalDao
import com.example.util.simpletimetracker.data_local.mapper.RecordTypeGoalDataLocalMapper
import com.example.util.simpletimetracker.data_local.utils.logDataAccess
import com.example.util.simpletimetracker.data_local.utils.removeIf
import com.example.util.simpletimetracker.data_local.utils.replaceWith
import com.example.util.simpletimetracker.data_local.utils.withLockedCache
import com.example.util.simpletimetracker.domain.model.RecordTypeGoal
import com.example.util.simpletimetracker.domain.model.RecordTypeGoal.IdData
import com.example.util.simpletimetracker.domain.repo.RecordTypeGoalRepo
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

    override suspend fun add(recordTypeGoal: RecordTypeGoal): Long = mutex.withLockedCache(
        logMessage = "add",
        accessSource = { dao.insert(recordTypeGoal.let(mapper::map)) },
        afterSourceAccess = { cache = cache?.replaceWith(recordTypeGoal) { it.id == recordTypeGoal.id } },
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
}