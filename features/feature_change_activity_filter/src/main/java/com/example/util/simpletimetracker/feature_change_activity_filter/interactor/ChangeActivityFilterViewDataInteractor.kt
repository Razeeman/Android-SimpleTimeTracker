package com.example.util.simpletimetracker.feature_change_activity_filter.interactor

import com.example.util.simpletimetracker.core.mapper.CategoryViewDataMapper
import com.example.util.simpletimetracker.core.mapper.RecordTypeViewDataMapper
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.interactor.CategoryInteractor
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.domain.model.ActivityFilter
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.divider.DividerViewData
import com.example.util.simpletimetracker.feature_base_adapter.info.InfoViewData
import javax.inject.Inject

class ChangeActivityFilterViewDataInteractor @Inject constructor(
    private val prefsInteractor: PrefsInteractor,
    private val recordTypeInteractor: RecordTypeInteractor,
    private val categoryInteractor: CategoryInteractor,
    private val recordTypeViewDataMapper: RecordTypeViewDataMapper,
    private val categoryViewDataMapper: CategoryViewDataMapper,
    private val resourceRepo: ResourceRepo,
) {

    suspend fun getTypesViewData(
        type: ActivityFilter.Type,
        selectedIds: List<Long>,
    ): List<ViewHolderType> {
        val numberOfCards = prefsInteractor.getNumberOfCards()
        val isDarkTheme = prefsInteractor.getDarkMode()

        return when (type) {
            is ActivityFilter.Type.Activity -> {
                recordTypeInteractor.getAll()
                    .filter { !it.hidden }
                    .map {
                        it.id to recordTypeViewDataMapper.map(
                            recordType = it,
                            numberOfCards = numberOfCards,
                            isDarkTheme = isDarkTheme
                        )
                    }
            }
            is ActivityFilter.Type.ActivityTag -> {
                categoryInteractor.getAll()
                    .map {
                        it.id to categoryViewDataMapper.mapActivityTag(
                            category = it,
                            isDarkTheme = isDarkTheme
                        )
                    }
            }
        }
            .takeUnless { it.isEmpty() }
            ?.let {
                val selected = it.filter { it.first in selectedIds }.map { it.second }
                val available = it.filter { it.first !in selectedIds }.map { it.second }
                selected to available
            }
            ?.let { (selected, available) ->
                val viewData = mutableListOf<ViewHolderType>()
                mapSelectedTypesHint(
                    isEmpty = selected.isEmpty()
                ).let(viewData::add)
                selected.let(viewData::addAll)
                DividerViewData(1)
                    .takeUnless { available.isEmpty() }
                    ?.let(viewData::add)
                available.let(viewData::addAll)
                viewData
            }
            ?: run {
                when (type) {
                    is ActivityFilter.Type.Activity -> {
                        recordTypeViewDataMapper.mapToEmpty()
                    }
                    is ActivityFilter.Type.ActivityTag -> {
                        listOf(categoryViewDataMapper.mapToActivityTagsEmpty())
                    }
                }
            }
    }
}

private fun mapSelectedTypesHint(isEmpty: Boolean): ViewHolderType {
    return InfoViewData(
        text = if (isEmpty) {
            "Nothing selected" // TODO add translations
        } else {
            "Selected:"
        }
//            .let(resourceRepo::getString)
    )
}