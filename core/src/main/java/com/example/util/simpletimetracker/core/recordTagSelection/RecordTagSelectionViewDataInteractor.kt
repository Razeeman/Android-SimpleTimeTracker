package com.example.util.simpletimetracker.core.recordTagSelection

import com.example.util.simpletimetracker.core.adapter.ViewHolderType
import com.example.util.simpletimetracker.core.adapter.hint.HintViewData
import com.example.util.simpletimetracker.core.mapper.CategoryViewDataMapper
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTagInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTypeInteractor
import javax.inject.Inject

class RecordTagSelectionViewDataInteractor @Inject constructor(
    private val resourceRepo: ResourceRepo,
    private val prefsInteractor: PrefsInteractor,
    private val recordTypeInteractor: RecordTypeInteractor,
    private val recordTagInteractor: RecordTagInteractor,
    private val categoryViewDataMapper: CategoryViewDataMapper
) {

    suspend fun getViewData(typeId: Long): List<ViewHolderType> {
        val result: MutableList<ViewHolderType> = mutableListOf()
        val isDarkTheme = prefsInteractor.getDarkMode()
        val types = recordTypeInteractor.getAll().map { it.id to it }.toMap()

        val recordTagsViewData = recordTagInteractor.getByType(typeId)
            .mapNotNull { tag ->
                categoryViewDataMapper.mapRecordTag(
                    tag = tag,
                    type = types[tag.typeId] ?: return@mapNotNull null,
                    isDarkTheme = isDarkTheme,
                    isFiltered = false
                )
            }

        HintViewData("Select record tag").let(result::add)
        recordTagsViewData.let(result::addAll)

        return result
    }
}