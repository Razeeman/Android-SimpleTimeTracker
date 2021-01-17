package com.example.util.simpletimetracker.feature_change_record_type.interactor

import com.example.util.simpletimetracker.core.adapter.ViewHolderType
import com.example.util.simpletimetracker.core.adapter.divider.DividerViewData
import com.example.util.simpletimetracker.core.mapper.CategoryViewDataMapper
import com.example.util.simpletimetracker.domain.interactor.CategoryInteractor
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.model.Category
import com.example.util.simpletimetracker.feature_change_record_type.mapper.ChangeRecordTypeMapper
import javax.inject.Inject

class ChangeRecordTypeViewDataInteractor @Inject constructor(
    private val prefsInteractor: PrefsInteractor,
    private val categoryInteractor: CategoryInteractor,
    private val categoryViewDataMapper: CategoryViewDataMapper,
    private val changeRecordTypeMapper: ChangeRecordTypeMapper
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

                changeRecordTypeMapper.mapSelectedCategoriesHint(
                    isEmpty = selected.isEmpty()
                ).let(viewData::add)

                selected.map {
                    categoryViewDataMapper.map(
                        category = it,
                        isDarkTheme = isDarkTheme
                    )
                }.let(viewData::addAll)

                DividerViewData
                    .takeUnless { available.isEmpty() }
                    ?.let(viewData::add)

                available.map {
                    categoryViewDataMapper.map(
                        category = it,
                        isDarkTheme = isDarkTheme
                    )
                }.let(viewData::addAll)

                viewData
            }
            ?: changeRecordTypeMapper.mapToEmpty()
    }
}