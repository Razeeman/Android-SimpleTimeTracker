package com.example.util.simpletimetracker.core.interactor

import com.example.util.simpletimetracker.core.R
import com.example.util.simpletimetracker.core.mapper.CategoryViewDataMapper
import com.example.util.simpletimetracker.core.mapper.ColorMapper
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTagInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.category.CategoryViewData
import com.example.util.simpletimetracker.feature_base_adapter.divider.DividerViewData
import javax.inject.Inject

class RecordTagViewDataInteractor @Inject constructor(
    private val resourceRepo: ResourceRepo,
    private val colorMapper: ColorMapper,
    private val prefsInteractor: PrefsInteractor,
    private val recordTypeInteractor: RecordTypeInteractor,
    private val recordTagInteractor: RecordTagInteractor,
    private val categoryViewDataMapper: CategoryViewDataMapper
) {

    suspend fun getViewData(selectedTags: List<Long>, typeId: Long): List<ViewHolderType> {
        val isDarkTheme = prefsInteractor.getDarkMode()
        val type = recordTypeInteractor.get(typeId)
        val recordTags = if (typeId != 0L) {
            recordTagInteractor.getByType(typeId)
        } else {
            emptyList()
        } + recordTagInteractor.getUntyped()

        return recordTags
            .filterNot { it.archived }
            .takeUnless { it.isEmpty() }
            ?.let { tags ->
                val selected = tags.filter { it.id in selectedTags }
                val available = tags.filter { it.id !in selectedTags }
                selected to available
            }
            ?.let { (selected, available) ->
                val viewData = mutableListOf<ViewHolderType>()

                categoryViewDataMapper.mapSelectedCategoriesHint(
                    isEmpty = selected.isEmpty()
                ).let(viewData::add)

                selected.map {
                    categoryViewDataMapper.mapRecordTag(
                        tag = it,
                        type = type,
                        isDarkTheme = isDarkTheme
                    )
                }.let(viewData::addAll)

                DividerViewData(1)
                    .takeUnless { available.isEmpty() }
                    ?.let(viewData::add)

                available.map {
                    categoryViewDataMapper.mapRecordTag(
                        tag = it,
                        type = type,
                        isDarkTheme = isDarkTheme
                    )
                }.let(viewData::addAll)

                if (selected.isNotEmpty() || available.isNotEmpty()) {
                    DividerViewData(2).let(viewData::add)
                    mapRecordTagUntagged(isDarkTheme).let(viewData::add)
                }

                viewData
            }
            ?: categoryViewDataMapper.mapToRecordTagsEmpty()
    }

    private fun mapRecordTagUntagged(
        isDarkTheme: Boolean
    ): CategoryViewData.Record {
        return CategoryViewData.Record.Untagged(
            typeId = 0L,
            name = R.string.change_record_untagged.let(resourceRepo::getString),
            iconColor = categoryViewDataMapper.getTextColor(isDarkTheme, false),
            color = colorMapper.toUntrackedColor(isDarkTheme),
            icon = null
        )
    }
}