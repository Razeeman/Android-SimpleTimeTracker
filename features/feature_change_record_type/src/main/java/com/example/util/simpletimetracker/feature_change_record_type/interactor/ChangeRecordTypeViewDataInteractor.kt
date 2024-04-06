package com.example.util.simpletimetracker.feature_change_record_type.interactor

import com.example.util.simpletimetracker.core.mapper.CategoryViewDataMapper
import com.example.util.simpletimetracker.domain.interactor.CategoryInteractor
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.model.Category
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.divider.DividerViewData
import javax.inject.Inject

class ChangeRecordTypeViewDataInteractor @Inject constructor(
    private val prefsInteractor: PrefsInteractor,
    private val categoryInteractor: CategoryInteractor,
    private val categoryViewDataMapper: CategoryViewDataMapper,
) {

    suspend fun getCategoriesViewData(selectedCategories: List<Long>): List<ViewHolderType> {
        val isDarkTheme = prefsInteractor.getDarkMode()

        return categoryInteractor.getAll()
            .takeUnless(List<Category>::isEmpty)
            ?.let { categories ->
                val selected = categories.filter { it.id in selectedCategories }
                val available = categories.filter { it.id !in selectedCategories }
                selected to available
            }
            ?.let { (selected, available) ->
                val viewData = mutableListOf<ViewHolderType>()

                categoryViewDataMapper.mapToCategoryHint().let(viewData::add)

                DividerViewData(1).let(viewData::add)

                categoryViewDataMapper.mapSelectedCategoriesHint(
                    isEmpty = selected.isEmpty(),
                ).let(viewData::add)

                selected.map {
                    categoryViewDataMapper.mapCategory(
                        category = it,
                        isDarkTheme = isDarkTheme,
                    )
                }.let(viewData::addAll)

                DividerViewData(2).let(viewData::add)

                available.map {
                    categoryViewDataMapper.mapCategory(
                        category = it,
                        isDarkTheme = isDarkTheme,
                    )
                }.let(viewData::addAll)

                categoryViewDataMapper.mapToTypeTagAddItem(isDarkTheme).let(viewData::add)

                viewData
            }
            ?: listOf(
                categoryViewDataMapper.mapToCategoriesFirstHint(),
                categoryViewDataMapper.mapToTypeTagAddItem(isDarkTheme),
            )
    }
}