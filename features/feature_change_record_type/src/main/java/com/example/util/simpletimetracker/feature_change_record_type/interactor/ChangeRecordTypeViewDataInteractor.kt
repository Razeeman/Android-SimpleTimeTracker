package com.example.util.simpletimetracker.feature_change_record_type.interactor

import com.example.util.simpletimetracker.core.mapper.CategoryViewDataMapper
import com.example.util.simpletimetracker.core.mapper.CommonViewDataMapper
import com.example.util.simpletimetracker.domain.interactor.CategoryInteractor
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.divider.DividerViewData
import com.example.util.simpletimetracker.feature_change_record_type.viewData.ChangeRecordTypeCategoriesViewData
import javax.inject.Inject

class ChangeRecordTypeViewDataInteractor @Inject constructor(
    private val prefsInteractor: PrefsInteractor,
    private val categoryInteractor: CategoryInteractor,
    private val categoryViewDataMapper: CategoryViewDataMapper,
    private val commonViewDataMapper: CommonViewDataMapper,
) {

    suspend fun getCategoriesViewData(
        selectedCategories: List<Long>,
    ): ChangeRecordTypeCategoriesViewData {
        val isDarkTheme = prefsInteractor.getDarkMode()
        val categories = categoryInteractor.getAll()

        return if (categories.isNotEmpty()) {
            val selected = categories.filter { it.id in selectedCategories }
            val available = categories.filter { it.id !in selectedCategories }
            val viewData = mutableListOf<ViewHolderType>()

            categoryViewDataMapper.mapToCategoryHint().let(viewData::add)

            DividerViewData(1).let(viewData::add)

            commonViewDataMapper.mapSelectedHint(
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

            ChangeRecordTypeCategoriesViewData(
                selectedCount = selected.size,
                viewData = viewData,
            )
        } else {
            ChangeRecordTypeCategoriesViewData(
                selectedCount = 0,
                viewData = listOf(
                    categoryViewDataMapper.mapToCategoriesFirstHint(),
                    categoryViewDataMapper.mapToTypeTagAddItem(isDarkTheme),
                ),
            )
        }
    }
}