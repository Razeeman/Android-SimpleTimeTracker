package com.example.util.simpletimetracker.feature_records_filter.interactor

import com.example.util.simpletimetracker.domain.UNCATEGORIZED_ITEM_ID
import com.example.util.simpletimetracker.domain.extension.addOrRemove
import com.example.util.simpletimetracker.domain.extension.getAllTypeIds
import com.example.util.simpletimetracker.domain.extension.getCategoryItems
import com.example.util.simpletimetracker.domain.extension.getCommentItems
import com.example.util.simpletimetracker.domain.extension.getDaysOfWeek
import com.example.util.simpletimetracker.domain.extension.getFilteredTags
import com.example.util.simpletimetracker.domain.extension.getManuallyFilteredRecordIds
import com.example.util.simpletimetracker.domain.extension.getSelectedTags
import com.example.util.simpletimetracker.domain.extension.getTypeIds
import com.example.util.simpletimetracker.domain.extension.getTypeIdsFromCategories
import com.example.util.simpletimetracker.domain.extension.hasMultitaskFilter
import com.example.util.simpletimetracker.domain.extension.hasUntrackedFilter
import com.example.util.simpletimetracker.domain.interactor.FilterSelectableTagsInteractor
import com.example.util.simpletimetracker.domain.model.Category
import com.example.util.simpletimetracker.domain.model.DayOfWeek
import com.example.util.simpletimetracker.domain.model.Range
import com.example.util.simpletimetracker.domain.model.RecordTag
import com.example.util.simpletimetracker.domain.model.RecordType
import com.example.util.simpletimetracker.domain.model.RecordTypeCategory
import com.example.util.simpletimetracker.domain.model.RecordTypeToTag
import com.example.util.simpletimetracker.domain.model.RecordsFilter
import com.example.util.simpletimetracker.feature_base_adapter.category.CategoryViewData
import com.example.util.simpletimetracker.feature_base_adapter.record.RecordViewData
import com.example.util.simpletimetracker.feature_base_adapter.recordFilter.RecordFilterViewData
import com.example.util.simpletimetracker.feature_records_filter.mapper.RecordsFilterViewDataMapper
import com.example.util.simpletimetracker.feature_records_filter.model.RecordsFilterSelectedRecordsViewData
import com.example.util.simpletimetracker.feature_records_filter.viewData.RecordsFilterSelectionButtonType
import javax.inject.Inject

class RecordsFilterUpdateInteractor @Inject constructor(
    private val filterSelectableTagsInteractor: FilterSelectableTagsInteractor,
    private val recordsFilterViewDataMapper: RecordsFilterViewDataMapper,
) {

    fun handleTypeClick(
        id: Long,
        currentFilters: List<RecordsFilter>,
        recordTypes: List<RecordType>,
        recordTypeCategories: List<RecordTypeCategory>,
    ): List<RecordsFilter> {
        val filters = currentFilters.toMutableList()
        val currentIds = filters.getTypeIds().toMutableList()
        val currentIdsFromCategories = filters.getTypeIdsFromCategories(
            recordTypes = recordTypes,
            recordTypeCategories = recordTypeCategories,
        )

        // Switch from categories to types in these categories.
        if (currentIdsFromCategories.isNotEmpty()) {
            currentIds.addAll(currentIdsFromCategories)
        }

        val newIds = currentIds.toMutableList().apply { addOrRemove(id) }

        return handleSelectTypes(filters, newIds)
    }

    fun handleCategoryClick(
        id: Long,
        currentFilters: List<RecordsFilter>,
    ): List<RecordsFilter> {
        val filters = currentFilters.toMutableList()
        val currentItems = filters.getCategoryItems()

        val newItems = if (id == UNCATEGORIZED_ITEM_ID) {
            RecordsFilter.CategoryItem.Uncategorized
        } else {
            RecordsFilter.CategoryItem.Categorized(id)
        }.let { currentItems.toMutableList().apply { addOrRemove(it) } }

        return handleSelectCategories(filters, newItems)
    }

    fun handleTagClick(
        currentState: RecordFilterViewData.Type,
        currentFilters: List<RecordsFilter>,
        item: CategoryViewData.Record,
    ): List<RecordsFilter> {
        val filters = currentFilters.toMutableList()
        val currentTags = when (currentState) {
            RecordFilterViewData.Type.SELECTED_TAGS -> filters.getSelectedTags()
            RecordFilterViewData.Type.FILTERED_TAGS -> filters.getFilteredTags()
            else -> return currentFilters
        }

        val newTags = when (item) {
            is CategoryViewData.Record.Tagged -> RecordsFilter.TagItem.Tagged(item.id)
            is CategoryViewData.Record.Untagged -> RecordsFilter.TagItem.Untagged
        }.let { currentTags.toMutableList().apply { addOrRemove(it) } }

        return handleSelectTags(currentState, filters, newTags)
    }

    fun handleUntrackedClick(
        currentFilters: List<RecordsFilter>,
    ): List<RecordsFilter> {
        val filters = currentFilters.toMutableList()
        val hasUntrackedFilter = filters.hasUntrackedFilter()

        if (!hasUntrackedFilter) {
            val filtersAvailableWithUntrackedFilter = listOf(
                RecordsFilter.Date::class.java,
                RecordsFilter.DaysOfWeek::class.java,
                RecordsFilter.TimeOfDay::class.java,
                RecordsFilter.Duration::class.java,
            )
            filters.removeAll {
                it::class.java !in filtersAvailableWithUntrackedFilter
            }

            filters.add(RecordsFilter.Untracked)
        } else {
            filters.removeAll { it is RecordsFilter.Untracked }
        }

        return filters
    }

    fun handleMultitaskClick(
        currentFilters: List<RecordsFilter>,
    ): List<RecordsFilter> {
        val filters = currentFilters.toMutableList()
        val hasMultitaskFilter = filters.hasMultitaskFilter()

        if (!hasMultitaskFilter) {
            val filtersAvailableWithMultitaskFilter = listOf(
                RecordsFilter.Date::class.java,
                RecordsFilter.DaysOfWeek::class.java,
                RecordsFilter.TimeOfDay::class.java,
                RecordsFilter.Duration::class.java,
            )
            filters.removeAll {
                it::class.java !in filtersAvailableWithMultitaskFilter
            }

            filters.add(RecordsFilter.Multitask)
        } else {
            filters.removeAll { it is RecordsFilter.Multitask }
        }

        return filters
    }

    fun handleCommentFilterClick(
        currentFilters: List<RecordsFilter>,
        item: RecordFilterViewData,
    ): List<RecordsFilter> {
        val filters = currentFilters.toMutableList()
        val currentItems = filters.getCommentItems()

        val clickedItem = when (item.id) {
            RecordFilterViewData.CommentType.NO_COMMENT.ordinal.toLong() -> {
                RecordsFilter.CommentItem.NoComment
            }
            RecordFilterViewData.CommentType.ANY_COMMENT.ordinal.toLong() -> {
                RecordsFilter.CommentItem.AnyComment
            }
            else -> return currentFilters
        }
        val newItems = currentItems.toMutableList().apply {
            if (clickedItem !in this) clear()
            addOrRemove(clickedItem)
        }

        filters.removeAll { it is RecordsFilter.Comment }
        if (newItems.isNotEmpty()) filters.add(RecordsFilter.Comment(newItems))

        return filters
    }

    fun handleCommentChange(
        currentFilters: List<RecordsFilter>,
        text: String,
    ): List<RecordsFilter> {
        val filters = currentFilters.toMutableList()
        filters.removeAll { it is RecordsFilter.Comment }
        if (text.isNotEmpty()) {
            val newItems = RecordsFilter.CommentItem.Comment(text).let(::listOf)
            filters.add(RecordsFilter.Comment(newItems))
        }
        return filters
    }

    fun handleRecordClick(
        currentFilters: List<RecordsFilter>,
        id: Long,
    ): List<RecordsFilter> {
        val filters = currentFilters.toMutableList()
        val newIds = filters.getManuallyFilteredRecordIds()
            .toMutableList()
            .apply { addOrRemove(id) }
        filters.removeAll { it is RecordsFilter.ManuallyFiltered }
        if (newIds.isNotEmpty()) filters.add(RecordsFilter.ManuallyFiltered(newIds))
        return filters
    }

    fun handleInvertSelection(
        currentFilters: List<RecordsFilter>,
        recordsViewData: RecordsFilterSelectedRecordsViewData?,
    ): List<RecordsFilter> {
        val filters = currentFilters.toMutableList()
        val filteredIds = filters.getManuallyFilteredRecordIds()
            .toMutableList()
        val selectedIds = recordsViewData
            ?.recordsViewData
            .orEmpty()
            .filterIsInstance<RecordViewData.Tracked>()
            .filter { it.id !in filteredIds }
            .map { it.id }

        filters.removeAll { it is RecordsFilter.ManuallyFiltered }
        if (selectedIds.isNotEmpty()) filters.add(RecordsFilter.ManuallyFiltered(selectedIds))
        return filters
    }

    fun onDurationSet(
        currentFilters: List<RecordsFilter>,
        rangeStart: Long,
        rangeEnd: Long,
    ): List<RecordsFilter> {
        val filters = currentFilters.toMutableList()
        filters.removeAll { it is RecordsFilter.Duration }
        filters.add(RecordsFilter.Duration(Range(rangeStart, rangeEnd)))
        return filters
    }

    fun handleDateSet(
        currentFilters: List<RecordsFilter>,
        rangeStart: Long,
        rangeEnd: Long,
    ): List<RecordsFilter> {
        val filters = currentFilters.toMutableList()
        filters.removeAll { it is RecordsFilter.Date }
        filters.add(RecordsFilter.Date(Range(rangeStart, rangeEnd)))
        return filters
    }

    fun handleTimeOfDaySet(
        currentFilters: List<RecordsFilter>,
        rangeStart: Long,
        rangeEnd: Long,
    ): List<RecordsFilter> {
        val filters = currentFilters.toMutableList()
        filters.removeAll { it is RecordsFilter.TimeOfDay }
        filters.add(RecordsFilter.TimeOfDay(Range(rangeStart, rangeEnd)))
        return filters
    }

    fun removeFilter(
        currentFilters: List<RecordsFilter>,
        type: RecordFilterViewData.Type,
    ): List<RecordsFilter> {
        val filters = currentFilters.toMutableList()
        val filterClass = recordsFilterViewDataMapper.mapToClass(type)
        filters.removeAll { filterClass.isInstance(it) }
        return filters
    }

    fun handleDayOfWeekClick(
        currentFilters: List<RecordsFilter>,
        dayOfWeek: DayOfWeek,
    ): List<RecordsFilter> {
        val filters = currentFilters.toMutableList()
        val newDays = filters.getDaysOfWeek()
            .toMutableList()
            .apply { addOrRemove(dayOfWeek) }

        filters.removeAll { it is RecordsFilter.DaysOfWeek }
        if (newDays.isNotEmpty()) filters.add(RecordsFilter.DaysOfWeek(newDays))
        return filters
    }

    fun onTypesSelectionButtonClick(
        currentFilters: List<RecordsFilter>,
        subtype: RecordsFilterSelectionButtonType.Subtype,
        recordTypes: List<RecordType>,
    ): List<RecordsFilter> {
        val newIds = when (subtype) {
            is RecordsFilterSelectionButtonType.Subtype.SelectAll -> recordTypes.map { it.id }
            is RecordsFilterSelectionButtonType.Subtype.SelectNone -> emptyList()
        }
        return handleSelectTypes(
            currentFilters = currentFilters,
            newIds = newIds,
        )
    }

    fun onCategoriesSelectionButtonClick(
        currentFilters: List<RecordsFilter>,
        subtype: RecordsFilterSelectionButtonType.Subtype,
        categories: List<Category>,
    ): List<RecordsFilter> {
        val newItems = when (subtype) {
            is RecordsFilterSelectionButtonType.Subtype.SelectAll -> {
                categories
                    .map { RecordsFilter.CategoryItem.Categorized(it.id) }
                    .plus(RecordsFilter.CategoryItem.Uncategorized)
            }
            is RecordsFilterSelectionButtonType.Subtype.SelectNone -> {
                emptyList()
            }
        }
        return handleSelectCategories(
            currentFilters = currentFilters,
            newItems = newItems,
        )
    }

    fun onTagsSelectionButtonClick(
        currentFilters: List<RecordsFilter>,
        subtype: RecordsFilterSelectionButtonType.Subtype,
        currentState: RecordFilterViewData.Type,
        tags: List<RecordTag>,
    ): List<RecordsFilter> {
        val newItems = when (subtype) {
            is RecordsFilterSelectionButtonType.Subtype.SelectAll -> {
                tags
                    .map { RecordsFilter.TagItem.Tagged(it.id) }
                    .plus(RecordsFilter.TagItem.Untagged)
            }
            is RecordsFilterSelectionButtonType.Subtype.SelectNone -> {
                emptyList()
            }
        }
        return handleSelectTags(
            currentState = currentState,
            currentFilters = currentFilters,
            newItems = newItems,
        )
    }

    fun checkTagFilterConsistency(
        currentFilters: List<RecordsFilter>,
        recordTypes: List<RecordType>,
        recordTypeCategories: List<RecordTypeCategory>,
        recordTags: List<RecordTag>,
        typesToTags: List<RecordTypeToTag>,
    ): List<RecordsFilter> {
        val filters = currentFilters.toMutableList()
        // Update tags according to selected activities
        val newTypeIds: List<Long> = filters.getAllTypeIds(
            recordTypes = recordTypes,
            recordTypeCategories = recordTypeCategories,
        )

        fun update(tags: List<RecordsFilter.TagItem>): List<RecordsFilter.TagItem> {
            return tags.filter {
                when (it) {
                    is RecordsFilter.TagItem.Tagged -> {
                        it.tagId in recordTags
                            .map { tag -> tag.id }
                            .let { tags ->
                                filterSelectableTagsInteractor.execute(
                                    tagIds = tags,
                                    typesToTags = typesToTags,
                                    typeIds = newTypeIds,
                                )
                            }
                    }
                    is RecordsFilter.TagItem.Untagged -> {
                        true
                    }
                }
            }
        }

        val newSelectedTags = update(filters.getSelectedTags())

        filters.removeAll { filter -> filter is RecordsFilter.SelectedTags }
        if (newSelectedTags.isNotEmpty()) filters.add(RecordsFilter.SelectedTags(newSelectedTags))

        val newFilteredTags = update(filters.getFilteredTags())

        filters.removeAll { filter -> filter is RecordsFilter.FilteredTags }
        if (newFilteredTags.isNotEmpty()) filters.add(RecordsFilter.FilteredTags(newFilteredTags))

        return filters
    }

    private fun handleSelectTypes(
        currentFilters: List<RecordsFilter>,
        newIds: List<Long>,
    ): List<RecordsFilter> {
        val filters = currentFilters.toMutableList()
        filters.removeAll { it is RecordsFilter.Activity }
        filters.removeAll { it is RecordsFilter.Category }
        filters.removeAll { it is RecordsFilter.Untracked }
        filters.removeAll { it is RecordsFilter.Multitask }
        if (newIds.isNotEmpty()) filters.add(RecordsFilter.Activity(newIds))
        return filters
    }

    private fun handleSelectCategories(
        currentFilters: List<RecordsFilter>,
        newItems: List<RecordsFilter.CategoryItem>,
    ): List<RecordsFilter> {
        val filters = currentFilters.toMutableList()
        filters.removeAll { it is RecordsFilter.Activity }
        filters.removeAll { it is RecordsFilter.Category }
        filters.removeAll { it is RecordsFilter.Untracked }
        filters.removeAll { it is RecordsFilter.Multitask }
        if (newItems.isNotEmpty()) filters.add(RecordsFilter.Category(newItems))
        return filters
    }

    private fun handleSelectTags(
        currentState: RecordFilterViewData.Type,
        currentFilters: List<RecordsFilter>,
        newItems: List<RecordsFilter.TagItem>,
    ): List<RecordsFilter> {
        val filters = currentFilters.toMutableList()
        filters.removeAll { it is RecordsFilter.Untracked }
        filters.removeAll { it is RecordsFilter.Multitask }
        when (currentState) {
            RecordFilterViewData.Type.SELECTED_TAGS -> {
                filters.removeAll { it is RecordsFilter.SelectedTags }
                if (newItems.isNotEmpty()) filters.add(RecordsFilter.SelectedTags(newItems))
            }
            RecordFilterViewData.Type.FILTERED_TAGS -> {
                filters.removeAll { it is RecordsFilter.FilteredTags }
                if (newItems.isNotEmpty()) filters.add(RecordsFilter.FilteredTags(newItems))
            }
            else -> return currentFilters
        }
        return filters
    }
}