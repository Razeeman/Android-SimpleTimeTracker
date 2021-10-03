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
import javax.inject.Inject

class RecordTagViewDataInteractor @Inject constructor(
    private val resourceRepo: ResourceRepo,
    private val colorMapper: ColorMapper,
    private val prefsInteractor: PrefsInteractor,
    private val recordTypeInteractor: RecordTypeInteractor,
    private val recordTagInteractor: RecordTagInteractor,
    private val categoryViewDataMapper: CategoryViewDataMapper
) {

    suspend fun getViewData(typeId: Long): List<ViewHolderType> {
        val isDarkTheme = prefsInteractor.getDarkMode()
        val type = recordTypeInteractor.get(typeId)
        val tags = if (typeId != 0L) {
            recordTagInteractor.getByType(typeId)
        } else {
            emptyList()
        } + recordTagInteractor.getUntyped()

        return tags
            .filterNot { it.archived }
            .takeUnless { it.isEmpty() }
            ?.map { tag ->
                categoryViewDataMapper.mapRecordTag(
                    tag = tag,
                    type = type,
                    isDarkTheme = isDarkTheme,
                )
            }
            ?.plus(mapRecordTagUntagged(isDarkTheme))
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