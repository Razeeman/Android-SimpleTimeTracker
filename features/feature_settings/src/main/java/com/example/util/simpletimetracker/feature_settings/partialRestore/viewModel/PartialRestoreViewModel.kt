package com.example.util.simpletimetracker.feature_settings.partialRestore.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.util.simpletimetracker.core.base.BaseViewModel
import com.example.util.simpletimetracker.core.base.SingleLiveEvent
import com.example.util.simpletimetracker.core.extension.lazySuspend
import com.example.util.simpletimetracker.core.extension.set
import com.example.util.simpletimetracker.domain.model.ActivityFilter
import com.example.util.simpletimetracker.domain.model.PartialBackupRestoreData
import com.example.util.simpletimetracker.domain.model.RecordTypeGoal
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.recordFilter.FilterViewData
import com.example.util.simpletimetracker.feature_settings.partialRestore.interactor.PartialRestoreViewDataInteractor
import com.example.util.simpletimetracker.feature_settings.partialRestore.mapper.PartialRestoreViewDataMapper
import com.example.util.simpletimetracker.feature_settings.partialRestore.model.PartialRestoreFilterType
import com.example.util.simpletimetracker.feature_settings.partialRestore.utils.getIds
import com.example.util.simpletimetracker.feature_settings.partialRestoreSelection.model.PartialRestoreSelectionDialogParams
import com.example.util.simpletimetracker.feature_settings.viewModel.delegate.SettingsFileWorkDelegate
import com.example.util.simpletimetracker.navigation.Router
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class PartialRestoreViewModel @Inject constructor(
    private val router: Router,
    private val partialRestoreViewDataInteractor: PartialRestoreViewDataInteractor,
    private val settingsFileWorkDelegate: SettingsFileWorkDelegate,
    private val partialRestoreViewDataMapper: PartialRestoreViewDataMapper,
) : BaseViewModel() {

    val filtersViewData: LiveData<List<ViewHolderType>>
        by lazySuspend { loadFiltersViewData() }
    val dismiss: LiveData<Unit>
        by lazy { SingleLiveEvent<Unit>() }

    // Map of filter to list of filtered ids.
    private var filters: Map<PartialRestoreFilterType, Set<Long>> = emptyMap()

    override fun onCleared() {
        // Clear field to free memory. Can hold all the records and data.
        settingsFileWorkDelegate.partialBackupRestoreData = null
        settingsFileWorkDelegate.partialBackupRestoreDataSelectable = null
        super.onCleared()
    }

    fun onFilterClick(data: FilterViewData) {
        val itemType = data.type as? PartialRestoreFilterType ?: return

        PartialRestoreSelectionDialogParams(
            tag = PARTIAL_RESTORE_SELECTION_TAG,
            type = itemType,
            filteredIds = filters[itemType].orEmpty(),
        ).let(router::navigate)
    }

    fun onFilterRemoveClick(data: FilterViewData) {
        val itemType = data.type as? PartialRestoreFilterType ?: return
        val currentData = settingsFileWorkDelegate.partialBackupRestoreData ?: return
        filters = filters.toMutableMap().apply { put(itemType, currentData.getIds(itemType)) }
        updateFilters()
    }

    fun onDataSelected(
        type: PartialRestoreFilterType,
        dataIds: Set<Long>,
        tag: String?,
    ) {
        if (tag != PARTIAL_RESTORE_SELECTION_TAG) return
        filters = filters.toMutableMap().apply { put(type, dataIds) }
        updateFilters()
    }

    fun onRestoreClick() {
        val data = settingsFileWorkDelegate.partialBackupRestoreDataSelectable ?: return
        val filteredData = partialRestoreViewDataMapper.mapFilteredData(filters, data)
        settingsFileWorkDelegate.onPartialRestoreConfirmed(filteredData)
        dismiss.set(Unit)
    }

    private suspend fun checkDataConsistency(
        data: PartialBackupRestoreData,
    ): PartialBackupRestoreData = withContext(Dispatchers.Default) {
        val types = data.types
        val typesIds = types.keys
        val categories = data.categories
        val categoriesIds = categories.keys

        // Check tags
        val tags = data.tags.mapValues {
            if (it.value.iconColorSource != 0L && it.value.iconColorSource !in typesIds) {
                it.value.copy(
                    icon = types[it.value.iconColorSource]?.icon ?: it.value.icon,
                    color = types[it.value.iconColorSource]?.color ?: it.value.color,
                )
            } else {
                it.value
            }
        }
        val typeToTag = data.typeToTag.filter {
            it.recordTypeId in typesIds && it.tagId in tags
        }
        val typeToDefaultTag = data.typeToDefaultTag.filter {
            it.recordTypeId in typesIds && it.tagId in tags
        }

        // Check records
        val records = data.records.filter {
            it.value.typeId in typesIds
        }.mapValues { record ->
            record.value.copy(tagIds = record.value.tagIds.filter { it in tags })
        }
        val recordsIds = records.keys

        // Check record to tag relation
        val recordToTag = data.recordToTag.filter {
            it.recordId in recordsIds && it.recordTagId in tags
        }

        // Check type to category relation
        val typeToCategory = data.typeToCategory.filter {
            it.recordTypeId in typesIds && it.categoryId in categoriesIds
        }

        // Check filters
        val activityFilters = data.activityFilters.mapValues { filter ->
            val newIds = filter.value.selectedIds.filter {
                when (filter.value.type) {
                    is ActivityFilter.Type.Activity -> it in typesIds
                    is ActivityFilter.Type.Category -> it in categoriesIds
                }
            }
            filter.value.copy(selectedIds = newIds)
        }

        // Check goals
        val goals = data.goals.filter {
            when (val id = it.value.idData) {
                is RecordTypeGoal.IdData.Type -> id.value in typesIds
                is RecordTypeGoal.IdData.Category -> id.value in categoriesIds
            }
        }

        // Check rules
        val rules = data.rules.mapNotNull { rule ->
            val new = rule.value.copy(
                actionAssignTagIds = rule.value.actionAssignTagIds
                    .filter { it in tags }.toSet(),
                conditionStartingTypeIds = rule.value.conditionStartingTypeIds
                    .filter { it in typesIds }.toSet(),
                conditionCurrentTypeIds = rule.value.conditionCurrentTypeIds
                    .filter { it in typesIds }.toSet(),
            ).takeIf {
                it.hasActions && it.hasConditions
            } ?: return@mapNotNull null
            rule.key to new
        }.toMap()

        return@withContext PartialBackupRestoreData(
            types = types,
            records = records,
            categories = categories,
            typeToCategory = typeToCategory,
            tags = tags,
            recordToTag = recordToTag,
            typeToTag = typeToTag,
            typeToDefaultTag = typeToDefaultTag,
            activityFilters = activityFilters,
            favouriteComments = data.favouriteComments,
            favouriteIcon = data.favouriteIcon,
            goals = goals,
            rules = rules,
        )
    }

    private suspend fun onFilterChange() {
        val originalData = settingsFileWorkDelegate.partialBackupRestoreData ?: return
        val newSelectableData = originalData
            .copy(
                types = originalData.types.filter {
                    it.key !in filters[PartialRestoreFilterType.Activities].orEmpty()
                },
                categories = originalData.categories.filter {
                    it.key !in filters[PartialRestoreFilterType.Categories].orEmpty()
                },
                tags = originalData.tags.filter {
                    it.key !in filters[PartialRestoreFilterType.Tags].orEmpty()
                },
            )
            .let { checkDataConsistency(it) }
            .copy(
                types = originalData.types,
                categories = originalData.categories,
                tags = originalData.tags,
            )
        settingsFileWorkDelegate.partialBackupRestoreDataSelectable = newSelectableData
        filters = filters.mapValues { (filter, ids) ->
            ids.filter { it in newSelectableData.getIds(filter) }.toSet()
        }
    }

    private suspend fun loadInitialFilters() {
        val data = settingsFileWorkDelegate.partialBackupRestoreData
            ?.let { checkDataConsistency(it) }
            ?: return
        settingsFileWorkDelegate.partialBackupRestoreDataSelectable = data
        filters = partialRestoreViewDataInteractor.getInitialFilters(data)
    }

    private fun updateFilters() {
        viewModelScope.launch {
            onFilterChange()
            val data = loadFiltersViewData()
            filtersViewData.set(data)
        }
    }

    private suspend fun loadFiltersViewData(): List<ViewHolderType> {
        if (filters.isEmpty()) loadInitialFilters()
        return partialRestoreViewDataInteractor.getFiltersViewData(
            data = settingsFileWorkDelegate.partialBackupRestoreDataSelectable ?: return emptyList(),
            filters = filters,
        )
    }

    companion object {
        private const val PARTIAL_RESTORE_SELECTION_TAG = "PARTIAL_RESTORE_SELECTION_TAG"
    }
}
