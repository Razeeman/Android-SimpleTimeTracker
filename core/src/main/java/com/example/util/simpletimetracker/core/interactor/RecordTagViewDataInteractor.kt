package com.example.util.simpletimetracker.core.interactor

import com.example.util.simpletimetracker.core.adapter.ViewHolderType
import com.example.util.simpletimetracker.core.mapper.CategoryViewDataMapper
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTagInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTypeInteractor
import javax.inject.Inject

class RecordTagViewDataInteractor @Inject constructor(
    private val prefsInteractor: PrefsInteractor,
    private val recordTypeInteractor: RecordTypeInteractor,
    private val recordTagInteractor: RecordTagInteractor,
    private val categoryViewDataMapper: CategoryViewDataMapper
) {

    suspend fun getViewData(newTypeId: Long): List<ViewHolderType> {
        if (newTypeId == 0L) {
            return categoryViewDataMapper.mapToTypeNotSelected()
        }

        val isDarkTheme = prefsInteractor.getDarkMode()
        val type = recordTypeInteractor.get(newTypeId)

        return recordTagInteractor.getByType(newTypeId)
            .filterNot { it.archived }
            .takeUnless { it.isEmpty() }
            ?.mapNotNull {
                categoryViewDataMapper.mapRecordTag(
                    tag = it,
                    type = type ?: return@mapNotNull null,
                    isDarkTheme = isDarkTheme,
                    showIcon = false
                )
            }
            ?.plus(
                categoryViewDataMapper.mapRecordTagUntagged(
                    isDarkTheme = isDarkTheme,
                    showIcon = false
                )
            )
            ?: categoryViewDataMapper.mapToRecordTagsEmpty()
    }
}