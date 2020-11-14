package com.example.util.simpletimetracker.domain.interactor

import com.example.util.simpletimetracker.domain.model.Category
import com.example.util.simpletimetracker.domain.repo.CategoryRepo
import javax.inject.Inject

class CategoryInteractor @Inject constructor(
    private val categoryRepo: CategoryRepo
) {

    suspend fun getAll(): List<Category> {
        return categoryRepo.getAll()
    }

    suspend fun add(category: Category) {
        categoryRepo.add(category)
    }

    suspend fun clear() {
        categoryRepo.clear()
    }
}