package com.example.util.simpletimetracker.feature_categories.interactor

import com.example.util.simpletimetracker.core.adapter.ViewHolderType
import com.example.util.simpletimetracker.core.mapper.CategoryViewDataMapper
import com.example.util.simpletimetracker.domain.interactor.CategoryInteractor
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.feature_categories.mapper.CategoriesViewDataMapper
import javax.inject.Inject

class CategoriesViewDataInteractor @Inject constructor(
    private val prefsInteractor: PrefsInteractor,
    private val categoryInteractor: CategoryInteractor,
    private val categoryViewDataMapper: CategoryViewDataMapper,
    private val categoriesViewDataMapper: CategoriesViewDataMapper
) {

    suspend fun getViewData(): List<ViewHolderType> {
        val categories = categoryInteractor.getAll()
        val isDarkTheme = prefsInteractor.getDarkMode()

        return categories
            .map {
                categoryViewDataMapper.map(
                    category = it,
                    isDarkTheme = isDarkTheme
                )
            }
            .plus(
                categoriesViewDataMapper.mapToAddItem(
                    isDarkTheme
                )
            )
    }
}