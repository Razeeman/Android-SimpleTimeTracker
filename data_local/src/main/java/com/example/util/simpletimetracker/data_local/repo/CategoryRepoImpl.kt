package com.example.util.simpletimetracker.data_local.repo

import com.example.util.simpletimetracker.data_local.database.CategoryDao
import com.example.util.simpletimetracker.data_local.mapper.CategoryDataLocalMapper
import com.example.util.simpletimetracker.domain.model.Category
import com.example.util.simpletimetracker.domain.repo.CategoryRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CategoryRepoImpl @Inject constructor(
    private val categoryDao: CategoryDao,
    private val categoryDataLocalMapper: CategoryDataLocalMapper
) : CategoryRepo {

    override suspend fun getAll(): List<Category> = withContext(Dispatchers.IO) {
        Timber.d("getAll")
        categoryDao.getAll()
            .map(categoryDataLocalMapper::map)
    }

    override suspend fun get(id: Long): Category? = withContext(Dispatchers.IO) {
        Timber.d("get id")
        categoryDao.get(id)?.let(categoryDataLocalMapper::map)
    }

    override suspend fun add(category: Category) = withContext(Dispatchers.IO) {
        Timber.d("add")
        categoryDao.insert(
            category.let(categoryDataLocalMapper::map)
        )
    }

    override suspend fun remove(id: Long) = withContext(Dispatchers.IO) {
        Timber.d("remove")
        categoryDao.delete(id)
    }

    override suspend fun clear() = withContext(Dispatchers.IO) {
        Timber.d("clear")
        categoryDao.clear()
    }
}