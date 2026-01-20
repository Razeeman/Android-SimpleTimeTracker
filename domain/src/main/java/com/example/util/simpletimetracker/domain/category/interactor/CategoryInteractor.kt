package com.example.util.simpletimetracker.domain.category.interactor

import com.example.util.simpletimetracker.domain.category.model.Category
import com.example.util.simpletimetracker.domain.category.repo.CategoryRepo
import com.example.util.simpletimetracker.domain.category.repo.RecordTypeCategoryRepo
import com.example.util.simpletimetracker.domain.recordType.model.CardOrder
import com.example.util.simpletimetracker.domain.prefs.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.recordType.interactor.RecordTypeGoalInteractor
import com.example.util.simpletimetracker.domain.recordType.interactor.SortCardsInteractor
import javax.inject.Inject

class CategoryInteractor @Inject constructor(
    private val categoryRepo: CategoryRepo,
    private val recordTypeCategoryRepo: RecordTypeCategoryRepo,
    private val recordTypeGoalInteractor: RecordTypeGoalInteractor,
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

    suspend fun get(name: String): List<Category> {
        return categoryRepo.get(name)
    }

    suspend fun add(category: Category): Long {
        return categoryRepo.add(category)
    }

    suspend fun remove(id: Long) {
        prefsInteractor.getFilteredCategories().toMutableList()
            .apply { remove(id) }
            .let { prefsInteractor.setFilteredCategories(it) }
        prefsInteractor.getFilteredCategoriesOnList().toMutableList()
            .apply { remove(id) }
            .let { prefsInteractor.setFilteredCategoriesOnList(it) }
        prefsInteractor.getSelectedPredefinedFilters().toMutableList()
            .apply { remove(id) }
            .let { prefsInteractor.setSelectedPredefinedFilters(it) }
        recordTypeCategoryRepo.removeAll(id)
        recordTypeGoalInteractor.removeByCategory(id)
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