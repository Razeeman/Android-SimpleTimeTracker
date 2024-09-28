package com.example.util.simpletimetracker.feature_settings.partialRestore.interactor

import com.example.util.simpletimetracker.core.mapper.ColorMapper
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.model.PartialBackupRestoreData
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.recordFilter.FilterViewData
import com.example.util.simpletimetracker.feature_settings.partialRestore.mapper.PartialRestoreViewDataMapper
import com.example.util.simpletimetracker.feature_settings.partialRestore.model.PartialRestoreFilterType
import com.example.util.simpletimetracker.feature_settings.partialRestore.utils.getIds
import javax.inject.Inject

class PartialRestoreViewDataInteractor @Inject constructor(
    private val prefsInteractor: PrefsInteractor,
    private val mapper: PartialRestoreViewDataMapper,
    private val colorMapper: ColorMapper,
) {

    fun getInitialFilters(
        data: PartialBackupRestoreData,
    ): Map<PartialRestoreFilterType, Set<Long>> {
        return availableFilters.filter {
            data.getIds(it).isNotEmpty()
        }.associateWith {
            emptySet()
        }
    }

    suspend fun getFiltersViewData(
        data: PartialBackupRestoreData,
        filters: Map<PartialRestoreFilterType, Set<Long>>,
    ): List<ViewHolderType> {
        val isDarkTheme = prefsInteractor.getDarkMode()

        return filters.toList().mapIndexed { index, (type, ids) ->
            val allIds = data.getIds(type)
            val selectedIds = allIds.filter { it !in ids }
            val selected = selectedIds.isNotEmpty()
            val name = mapper.mapFilterName(
                filter = type,
                selectedIds = selectedIds,
            )
            val color = if (selected) {
                colorMapper.toActiveColor(isDarkTheme)
            } else {
                colorMapper.toInactiveColor(isDarkTheme)
            }
            FilterViewData(
                id = index.toLong(),
                type = type,
                name = name,
                color = color,
                removeBtnVisible = selected,
                selected = selected,
            )
        }
    }

    companion object {
        private val availableFilters = listOfNotNull(
            PartialRestoreFilterType.Activities,
            PartialRestoreFilterType.Categories,
            PartialRestoreFilterType.Tags,
            PartialRestoreFilterType.Records,
            PartialRestoreFilterType.ActivityFilters,
            PartialRestoreFilterType.FavouriteComments,
            PartialRestoreFilterType.FavouriteIcons,
            PartialRestoreFilterType.ComplexRules,
        )
    }
}