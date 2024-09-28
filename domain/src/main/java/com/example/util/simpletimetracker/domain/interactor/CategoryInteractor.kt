package com.example.util.simpletimetracker.domain.interactor

import com.example.util.simpletimetracker.domain.model.CardOrder
import com.example.util.simpletimetracker.domain.model.Category
import com.example.util.simpletimetracker.domain.repo.CategoryRepo
import com.example.util.simpletimetracker.domain.repo.RecordTypeCategoryRepo
import com.example.util.simpletimetracker.domain.repo.RecordTypeGoalRepo
import javax.inject.Inject

class CategoryInteractor @Inject constructor(
    private val categoryRepo: CategoryRepo,
    private val recordTypeCategoryRepo: RecordTypeCategoryRepo,
    private val recordTypeGoalRepo: RecordTypeGoalRepo,
    private val prefsInteractor: PrefsInteractor,
    private val sortCardsInteractor: SortCardsInteractor,
) {

    suspend fun getAll(cardOrder: CardOrder? = null): List<Category> {
        return sortCardsInteractor.sort(
            cardOrder = cardOrder ?: prefsInteractor.getCategoryOrder(),
            manualOrderProvider = { prefsInteractor.getCategoryOrderManual() },
            data = categoryRepo.getAll().map(::mapForSort),
        ).map { it.data }
    }

    suspend fun get(id: Long): Category? {
        return categoryRepo.get(id)
    }

    suspend fun get(name: String): Category? {
        return categoryRepo.get(name)
    }

    suspend fun add(category: Category): Long {
        return categoryRepo.add(category)
    }

    suspend fun remove(id: Long) {
        recordTypeCategoryRepo.removeAll(id)
        recordTypeGoalRepo.removeByCategory(id)
        categoryRepo.remove(id)
    }

    fun mapForSort(
        data: Category,
    ): SortCardsInteractor.DataHolder<Category> {
        return SortCardsInteractor.DataHolder(
            id = data.id,
            name = data.name,
            color = data.color,
            data = data,
        )
    }
}