package com.example.util.simpletimetracker.feature_categories.interactor

import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.divider.DividerViewData
import com.example.util.simpletimetracker.core.mapper.CategoryViewDataMapper
import com.example.util.simpletimetracker.domain.interactor.CategoryInteractor
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTagInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.core.mapper.CategoriesViewDataMapper
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject

class CategoriesViewDataInteractor @Inject constructor(
    private val prefsInteractor: PrefsInteractor,
    private val categoryInteractor: CategoryInteractor,
    private val recordTagInteractor: RecordTagInteractor,
    private val recordTypeInteractor: RecordTypeInteractor,
    private val categoryViewDataMapper: CategoryViewDataMapper,
    private val categoriesViewDataMapper: CategoriesViewDataMapper,
) {

    suspend fun getViewData(): List<ViewHolderType> = coroutineScope {
        val typeTags = async { getRecordTypeTagViewData() }
        val recordTags = async { getRecordTagViewData() }

        typeTags.await() +
            DividerViewData(1) +
            recordTags.await()
    }

    private suspend fun getRecordTypeTagViewData(): List<ViewHolderType> {
        val categories = categoryInteractor.getAll()
        val isDarkTheme = prefsInteractor.getDarkMode()
        val result: MutableList<ViewHolderType> = mutableListOf()

        categoriesViewDataMapper.mapToTypeTagHint().let(result::add)

        categories.map { category ->
            categoryViewDataMapper.mapCategory(
                category = category,
                isDarkTheme = isDarkTheme
            )
        }.let(result::addAll)

        categoriesViewDataMapper.mapToTypeTagAddItem(isDarkTheme).let(result::add)

        return result
    }

    private suspend fun getRecordTagViewData(): List<ViewHolderType> {
        val tags = recordTagInteractor.getAll().filterNot { it.archived }
        val types = recordTypeInteractor.getAll()
        val isDarkTheme = prefsInteractor.getDarkMode()
        val result: MutableList<ViewHolderType> = mutableListOf()

        categoriesViewDataMapper.mapToRecordTagHint().let(result::add)

        tags.sortedBy { tag ->
            val type = types.firstOrNull { it.id == tag.typeId } ?: 0
            types.indexOf(type)
        }.map { tag ->
            categoryViewDataMapper.mapRecordTag(
                tag = tag,
                type = types.firstOrNull { it.id == tag.typeId },
                isDarkTheme = isDarkTheme
            )
        }.let(result::addAll)

        categoriesViewDataMapper.mapToRecordTagAddItem(isDarkTheme).let(result::add)

        return result
    }
}