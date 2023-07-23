package com.example.util.simpletimetracker.feature_categories.interactor

import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.divider.DividerViewData
import com.example.util.simpletimetracker.core.mapper.CategoryViewDataMapper
import com.example.util.simpletimetracker.domain.interactor.CategoryInteractor
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTagInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.feature_categories.viewData.CategoriesViewData
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject

class CategoriesViewDataInteractor @Inject constructor(
    private val prefsInteractor: PrefsInteractor,
    private val categoryInteractor: CategoryInteractor,
    private val recordTagInteractor: RecordTagInteractor,
    private val recordTypeInteractor: RecordTypeInteractor,
    private val categoryViewDataMapper: CategoryViewDataMapper,
) {

    suspend fun getViewData(): CategoriesViewData = coroutineScope {
        val typeTags = async { getRecordTypeTagViewData() }
        val recordTags = async { getRecordTagViewData() }

        val items = typeTags.await().items +
            DividerViewData(1) +
            recordTags.await().items
        val showHint = typeTags.await().showHint ||
            recordTags.await().showHint

        return@coroutineScope CategoriesViewData(
            items = items,
            showHint = showHint,
        )
    }

    private suspend fun getRecordTypeTagViewData(): CategoriesViewData {
        val categories = categoryInteractor.getAll()
        val isDarkTheme = prefsInteractor.getDarkMode()
        val result: MutableList<ViewHolderType> = mutableListOf()

        categoryViewDataMapper.mapToCategoryHint().let(result::add)

        categories.map { category ->
            categoryViewDataMapper.mapCategory(
                category = category,
                isDarkTheme = isDarkTheme
            )
        }.let(result::addAll)

        categoryViewDataMapper.mapToTypeTagAddItem(isDarkTheme).let(result::add)

        return CategoriesViewData(
            items = result,
            showHint = categories.isNotEmpty(),
        )
    }

    private suspend fun getRecordTagViewData(): CategoriesViewData {
        val tags = recordTagInteractor.getAll().filterNot { it.archived }
        val types = recordTypeInteractor.getAll()
        val isDarkTheme = prefsInteractor.getDarkMode()
        val result: MutableList<ViewHolderType> = mutableListOf()

        categoryViewDataMapper.mapToRecordTagHint().let(result::add)

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

        categoryViewDataMapper.mapToRecordTagAddItem(isDarkTheme).let(result::add)

        return CategoriesViewData(
            items = result,
            showHint = tags.isNotEmpty(),
        )
    }
}