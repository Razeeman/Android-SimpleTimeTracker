package com.example.util.simpletimetracker.core.interactor

import com.example.util.simpletimetracker.core.adapter.ViewHolderType
import com.example.util.simpletimetracker.core.mapper.CategoryViewDataMapper
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTagInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTypeInteractor
import javax.inject.Inject

class CategoryViewDataInteractor @Inject constructor(
    private val prefsInteractor: PrefsInteractor,
    private val recordTypeInteractor: RecordTypeInteractor,
    private val recordTagInteractor: RecordTagInteractor,
    private val categoryViewDataMapper: CategoryViewDataMapper
) {

    suspend fun getCategoriesViewData(newTypeId: Long): List<ViewHolderType> {
        if (newTypeId == 0L) {
            return categoryViewDataMapper.mapToTypeNotSelected()
        }

        val isDarkTheme = prefsInteractor.getDarkMode()
        val type = recordTypeInteractor.get(newTypeId)

        return recordTagInteractor.getByType(newTypeId)
            .filterNot { it.archived }
            .takeUnless { it.isEmpty() }
            ?.map { categoryViewDataMapper.map(it, type, isDarkTheme) }
            ?.plus(categoryViewDataMapper.mapUntagged(isDarkTheme))
            ?: categoryViewDataMapper.mapToCategoriesEmpty()
    }
}