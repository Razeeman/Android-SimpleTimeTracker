package com.example.util.simpletimetracker.feature_dialogs.typesFilter.interactor

import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.divider.DividerViewData
import com.example.util.simpletimetracker.feature_base_adapter.hint.HintViewData
import com.example.util.simpletimetracker.core.mapper.CategoryViewDataMapper
import com.example.util.simpletimetracker.core.mapper.RecordTypeViewDataMapper
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.model.Category
import com.example.util.simpletimetracker.domain.model.ChartFilterType
import com.example.util.simpletimetracker.domain.model.RecordTag
import com.example.util.simpletimetracker.domain.model.RecordType
import com.example.util.simpletimetracker.domain.model.RecordTypeCategory
import com.example.util.simpletimetracker.feature_dialogs.R
import com.example.util.simpletimetracker.feature_dialogs.typesFilter.mapper.TypesFilterMapper
import com.example.util.simpletimetracker.navigation.params.screen.TypesFilterParams
import javax.inject.Inject

class TypesFilterViewDataInteractor @Inject constructor(
    private val prefsInteractor: PrefsInteractor,
    private val recordTypeViewDataMapper: RecordTypeViewDataMapper,
    private val categoryViewDataMapper: CategoryViewDataMapper,
    private val typesFilterMapper: TypesFilterMapper,
    private val resourceRepo: ResourceRepo
) {

    suspend fun getViewData(
        filter: TypesFilterParams,
        types: List<RecordType>,
        recordTypeCategories: List<RecordTypeCategory>,
        activityTags: List<Category>,
        recordTags: List<RecordTag>
    ): List<ViewHolderType> {
        val result: MutableList<ViewHolderType> = mutableListOf()

        val numberOfCards = prefsInteractor.getNumberOfCards()
        val isDarkTheme = prefsInteractor.getDarkMode()
        val typesMap = types.map { it.id to it }.toMap()

        val selectedTypes = types.map(RecordType::id).filter { typeId ->
            when (filter.filterType) {
                ChartFilterType.ACTIVITY ->
                    typeId in filter.selectedIds
                ChartFilterType.CATEGORY ->
                    typeId in recordTypeCategories
                        .filter { it.categoryId in filter.selectedIds }
                        .map { it.recordTypeId }
            }
        }

        val typesViewData = types.map { type ->
            recordTypeViewDataMapper.mapFiltered(
                recordType = type,
                numberOfCards = numberOfCards,
                isDarkTheme = isDarkTheme,
                isFiltered = type.id !in selectedTypes
            )
        }

        val activityTagsViewData = activityTags.map { tag ->
            val isFiltered = filter.filterType != ChartFilterType.CATEGORY ||
                tag.id !in filter.selectedIds

            categoryViewDataMapper.mapActivityTag(
                category = tag,
                isDarkTheme = isDarkTheme,
                isFiltered = isFiltered
            )
        }

        val recordTagsViewData = recordTags
            .asSequence()
            .filter { it.typeId in selectedTypes }
            .groupBy { it.typeId }
            .mapNotNull { (typeId, tags) ->
                typeId to typesFilterMapper.mapRecordTagUntagged(
                    type = typesMap[typeId] ?: return@mapNotNull null,
                    isDarkTheme = isDarkTheme,
                    isFiltered = typeId in filter.filteredRecordTags
                        .filterIsInstance<TypesFilterParams.FilteredRecordTag.Untagged>()
                        .map { it.typeId }
                ).let(::listOf) + mapTags(
                    filter = filter,
                    tags = tags,
                    typesMap = typesMap,
                    isDarkTheme = isDarkTheme
                )
            }
            .sortedBy { (typeId, _) ->
                val type = types.firstOrNull { it.id == typeId } ?: 0
                types.indexOf(type)
            }
            .map { (_, tags) -> tags }
            .flatten()
            .toList()

        if (activityTagsViewData.isNotEmpty()) {
            HintViewData(resourceRepo.getString(R.string.activity_tag_hint)).let(result::add)
            activityTagsViewData.let(result::addAll)
        }

        if (typesViewData.isNotEmpty()) {
            if (activityTagsViewData.isNotEmpty()) {
                DividerViewData(1).let(result::add)
            }
            HintViewData(resourceRepo.getString(R.string.activity_hint)).let(result::add)
            typesViewData.let(result::addAll)
        }

        if (recordTagsViewData.isNotEmpty()) {
            DividerViewData(2).let(result::add)
            HintViewData(resourceRepo.getString(R.string.record_tag_hint)).let(result::add)
            recordTagsViewData.let(result::addAll)
        }

        return result
    }

    private fun mapTags(
        filter: TypesFilterParams,
        tags: List<RecordTag>,
        typesMap: Map<Long, RecordType>,
        isDarkTheme: Boolean
    ): List<ViewHolderType> {
        return tags.mapNotNull { tag ->
            categoryViewDataMapper.mapRecordTag(
                tag = tag,
                type = typesMap[tag.typeId] ?: return@mapNotNull null,
                isDarkTheme = isDarkTheme,
                isFiltered = tag.id in filter.filteredRecordTags
                    .filterIsInstance<TypesFilterParams.FilteredRecordTag.Tagged>()
                    .map { it.id }
            )
        }
    }
}