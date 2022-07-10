package com.example.util.simpletimetracker.domain.interactor

import com.example.util.simpletimetracker.domain.model.Category
import com.example.util.simpletimetracker.domain.repo.CategoryRepo
import com.example.util.simpletimetracker.domain.repo.RecordTypeCategoryRepo
import java.util.Locale
import javax.inject.Inject

class CategoryInteractor @Inject constructor(
    private val categoryRepo: CategoryRepo,
    private val recordTypeCategoryRepo: RecordTypeCategoryRepo,
) {

    suspend fun getAll(): List<Category> {
        return categoryRepo.getAll()
            .sortedBy { it.name.lowercase(Locale.getDefault()) }
    }

    suspend fun get(id: Long): Category? {
        return categoryRepo.get(id)
    }

    suspend fun add(category: Category): Long {
        return categoryRepo.add(category)
    }

    suspend fun remove(id: Long) {
        categoryRepo.remove(id)
        recordTypeCategoryRepo.removeAll(id)
    }

    suspend fun clear() {
        categoryRepo.clear()
    }
}