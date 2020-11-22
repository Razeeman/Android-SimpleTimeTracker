package com.example.util.simpletimetracker.data_local.repo

import com.example.util.simpletimetracker.data_local.database.RecordTypeCategoryDao
import com.example.util.simpletimetracker.data_local.mapper.CategoryDataLocalMapper
import com.example.util.simpletimetracker.data_local.mapper.RecordTypeCategoryDataLocalMapper
import com.example.util.simpletimetracker.domain.model.Category
import com.example.util.simpletimetracker.domain.model.RecordTypeCategory
import com.example.util.simpletimetracker.domain.repo.RecordTypeCategoryRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecordTypeCategoryRepoImpl @Inject constructor(
    private val recordTypeCategoryDao: RecordTypeCategoryDao,
    private val categoryDataLocalMapper: CategoryDataLocalMapper,
    private val recordTypeCategoryDataLocalMapper: RecordTypeCategoryDataLocalMapper
) : RecordTypeCategoryRepo {

    override suspend fun getAll(): List<RecordTypeCategory>  = withContext(Dispatchers.IO) {
        Timber.d("get all")
        recordTypeCategoryDao.getAll()
            .map(recordTypeCategoryDataLocalMapper::map)
    }

    override suspend fun getCategoriesByType(typeId: Long): List<Category> = withContext(Dispatchers.IO) {
        Timber.d("get categories")
        recordTypeCategoryDao.getTypeWithCategories(typeId)
            ?.categories
            ?.map(categoryDataLocalMapper::map)
            .orEmpty()
    }

    override suspend fun getCategoryIdsByType(typeId: Long): List<Long> = withContext(Dispatchers.IO) {
        Timber.d("get category ids")
        recordTypeCategoryDao.getCategoryIdsByType(typeId)
    }

    override suspend fun addCategories(typeId: Long, categoryIds: List<Long>) = withContext(Dispatchers.IO) {
        Timber.d("add categories")
        categoryIds.map {
            recordTypeCategoryDataLocalMapper.map(typeId = typeId, categoryId = it)
        }.let {
            recordTypeCategoryDao.insert(it)
        }
    }

    override suspend fun removeCategories(typeId: Long, categoryIds: List<Long>) = withContext(Dispatchers.IO) {
        Timber.d("remove categories")
        categoryIds.map {
            recordTypeCategoryDataLocalMapper.map(typeId = typeId, categoryId = it)
        }.let {
            recordTypeCategoryDao.delete(it)
        }
    }

    override suspend fun getTypeIdsByCategory(categoryId: Long): List<Long> = withContext(Dispatchers.IO) {
        Timber.d("get type ids")
        recordTypeCategoryDao.getTypeIdsByCategory(categoryId)
    }

    override suspend fun addTypes(categoryId: Long, typeIds: List<Long>) = withContext(Dispatchers.IO) {
        Timber.d("add types")
        typeIds.map {
            recordTypeCategoryDataLocalMapper.map(typeId = it, categoryId = categoryId)
        }.let {
            recordTypeCategoryDao.insert(it)
        }
    }

    override suspend fun removeTypes(categoryId: Long, typeIds: List<Long>) = withContext(Dispatchers.IO) {
        Timber.d("remove types")
        typeIds.map {
            recordTypeCategoryDataLocalMapper.map(typeId = it, categoryId = categoryId)
        }.let {
            recordTypeCategoryDao.delete(it)
        }
    }

    override suspend fun removeAll(categoryId: Long) = withContext(Dispatchers.IO) {
        Timber.d("removeAll")
        recordTypeCategoryDao.deleteAll(categoryId)
    }

    override suspend fun clear() = withContext(Dispatchers.IO) {
        Timber.d("clear")
        recordTypeCategoryDao.clear()
    }
}