package com.example.util.simpletimetracker.feature_settings.partialRestore.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.util.simpletimetracker.core.base.BaseViewModel
import com.example.util.simpletimetracker.core.base.SingleLiveEvent
import com.example.util.simpletimetracker.core.extension.lazySuspend
import com.example.util.simpletimetracker.core.extension.set
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.activityFilter.model.ActivityFilter
import com.example.util.simpletimetracker.domain.backup.model.PartialBackupRestoreData
import com.example.util.simpletimetracker.domain.recordType.model.RecordTypeGoal
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.recordFilter.FilterViewData
import com.example.util.simpletimetracker.feature_settings.R
import com.example.util.simpletimetracker.feature_settings.partialRestore.interactor.PartialRestoreViewDataInteractor
import com.example.util.simpletimetracker.feature_settings.partialRestore.mapper.PartialRestoreViewDataMapper
import com.example.util.simpletimetracker.feature_settings.partialRestore.model.PartialRestoreFilterType
import com.example.util.simpletimetracker.feature_settings.partialRestore.utils.getIds
import com.example.util.simpletimetracker.feature_settings.partialRestoreSelection.model.PartialRestoreSelectionDialogParams
import com.example.util.simpletimetracker.feature_settings.viewModel.delegate.SettingsFileWorkDelegate
import com.example.util.simpletimetracker.navigation.Router
import com.example.util.simpletimetracker.navigation.params.screen.StandardDialogParams
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class PartialRestoreViewModel @Inject constructor(
    private val router: Router,
    private val resourceRepo: ResourceRepo,
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
        filters = filters.toMutableMap().apply {
            put(itemType, currentData.getIds(itemType, existing = false))
        }
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
        router.navigate(
            StandardDialogParams(
                tag = PARTIAL_RESTORE_ALERT_DIALOG_TAG,
                message = resourceRepo.getString(R.string.archive_deletion_alert),
                btnPositive = resourceRepo.getString(R.string.ok),
                btnNegative = resourceRepo.getString(R.string.cancel),
            ),
        )
    }

    fun onPositiveDialogClick(tag: String?) {
        when (tag) {
            PARTIAL_RESTORE_ALERT_DIALOG_TAG -> onRestore()
        }
    }

    private fun onRestore() {
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
        val tags = data.tags.mapValues { (_, item) ->
            if (item.data.iconColorSource != 0L &&
                item.data.iconColorSource !in typesIds
            ) {
                val newData = item.data.copy(
                    icon = types[item.data.iconColorSource]?.data?.icon
                        ?: item.data.icon,
                    color = types[item.data.iconColorSource]?.data?.color
                        ?: item.data.color,
                )
                item.copy(data = newData)
            } else {
                item
            }
        }
        val typeToTag = data.typeToTag.filter {
            it.data.recordTypeId in typesIds && it.data.tagId in tags
        }
        val typeToDefaultTag = data.typeToDefaultTag.filter {
            it.data.recordTypeId in typesIds && it.data.tagId in tags
        }

        // Check records
        val records = data.records.filter {
            it.value.data.typeId in typesIds
        }.mapValues { (_, item) ->
            val newData = item.data.copy(
                tags = item.data.tags.filter { it.tagId in tags },
            )
            item.copy(data = newData)
        }
        val recordsIds = records.keys

        // Check record shortcuts
        val recordShortcuts = data.recordShortcuts.filter {
            it.value.data.typeId in typesIds
        }.mapValues { (_, item) ->
            val newData = item.data.copy(
                tags = item.data.tags.filter { it.tagId in tags },
            )
            item.copy(data = newData)
        }
        val recordShortcutsIds = records.keys

        // Check record to tag relation
        val recordToTag = data.recordToTag.filter {
            it.data.recordId in recordsIds && it.data.recordTagId in tags
        }

        // Check shortcut to tag relation
        val recordShortcutToTag = data.recordShortcutToTag.filter {
            it.data.shortcutId in recordShortcutsIds && it.data.recordTagId in tags
        }

        // Check type to category relation
        val typeToCategory = data.typeToCategory.filter {
            it.data.recordTypeId in typesIds && it.data.categoryId in categoriesIds
        }

        // Check filters
        val activityFilters = data.activityFilters.mapValues { (_, item) ->
            val newIds = item.data.selectedIds.filter {
                when (item.data.type) {
                    is ActivityFilter.Type.Activity -> it in typesIds
                    is ActivityFilter.Type.Category -> it in categoriesIds
                }
            }.toSet()
            val newData = item.data.copy(selectedIds = newIds)
            item.copy(data = newData)
        }

        // Check goals
        val goals = data.goals.filter {
            when (val id = it.value.data.idData) {
                is RecordTypeGoal.IdData.Type -> id.value in typesIds
                is RecordTypeGoal.IdData.Category -> id.value in categoriesIds
                is RecordTypeGoal.IdData.Tag -> id.value in tags.keys
            }
        }

        // Check rules
        val rules = data.rules.mapNotNull { (id, item) ->
            val newData = item.data.copy(
                actionAssignTagIds = item.data.actionAssignTagIds
                    .filter { it in tags }.toSet(),
                conditionStartingTypeIds = item.data.conditionStartingTypeIds
                    .filter { it in typesIds }.toSet(),
                conditionCurrentTypeIds = item.data.conditionCurrentTypeIds
                    .filter { it in typesIds }.toSet(),
            ).takeIf {
                it.hasActions && it.hasConditions
            } ?: return@mapNotNull null
            id to item.copy(data = newData)
        }.toMap()

        // Check suggestions
        val activitySuggestions = data.activitySuggestions.filter {
            it.value.data.forTypeId in typesIds
        }.mapNotNull { (id, item) ->
            val newIds = item.data.suggestionIds.filter {
                it in typesIds
            }.takeIf {
                it.isNotEmpty()
            }?.toSet() ?: return@mapNotNull null
            val newData = item.data.copy(suggestionIds = newIds)
            id to item.copy(data = newData)
        }.toMap()

        return@withContext PartialBackupRestoreData(
            types = types,
            records = records,
            recordShortcuts = recordShortcuts,
            categories = categories,
            typeToCategory = typeToCategory,
            tags = tags,
            recordToTag = recordToTag,
            recordShortcutToTag = recordShortcutToTag,
            typeToTag = typeToTag,
            typeToDefaultTag = typeToDefaultTag,
            activityFilters = activityFilters,
            favouriteComments = data.favouriteComments,
            favouriteColors = data.favouriteColors,
            favouriteIcon = data.favouriteIcon,
            goals = goals,
            rules = rules,
            activitySuggestions = activitySuggestions,
        )
    }

    private suspend fun onFilterChange() = withContext(Dispatchers.Default) {
        val originalData = settingsFileWorkDelegate.partialBackupRestoreData ?: return@withContext
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
            val newIds = newSelectableData.getIds(filter, existing = false)
            ids.filter { it in newIds }.toSet()
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
        private const val PARTIAL_RESTORE_ALERT_DIALOG_TAG = "PARTIAL_RESTORE_ALERT_DIALOG_TAG"
    }
}
