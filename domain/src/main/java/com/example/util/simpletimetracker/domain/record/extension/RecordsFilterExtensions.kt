package com.example.util.simpletimetracker.domain.record.extension

import com.example.util.simpletimetracker.domain.base.UNTRACKED_ITEM_ID
import com.example.util.simpletimetracker.domain.daysOfWeek.model.DayOfWeek
import com.example.util.simpletimetracker.domain.recordType.model.RecordType
import com.example.util.simpletimetracker.domain.category.model.RecordTypeCategory
import com.example.util.simpletimetracker.domain.extension.orEmpty
import com.example.util.simpletimetracker.domain.extension.plus
import com.example.util.simpletimetracker.domain.record.model.MultitaskRecord
import com.example.util.simpletimetracker.domain.record.model.Range
import com.example.util.simpletimetracker.domain.record.model.Record
import com.example.util.simpletimetracker.domain.record.model.RecordBase
import com.example.util.simpletimetracker.domain.record.model.RecordsFilter
import com.example.util.simpletimetracker.domain.record.model.RunningRecord

fun List<RecordsFilter>.getTypeIds(): List<Long> {
    return filterIsInstance<RecordsFilter.Activity>()
        .map(RecordsFilter.Activity::selected)
        .flatten()
}

fun List<RecordsFilter>.getFilteredTypeIds(): List<Long> {
    return filterIsInstance<RecordsFilter.Activity>()
        .map(RecordsFilter.Activity::filtered)
        .flatten()
}

fun List<RecordsFilter>.getCategoryItems(): List<RecordsFilter.CategoryItem> {
    return filterIsInstance<RecordsFilter.Category>()
        .map(RecordsFilter.Category::selected)
        .flatten()
}

fun List<RecordsFilter>.getFilteredCategoryItems(): List<RecordsFilter.CategoryItem> {
    return filterIsInstance<RecordsFilter.Category>()
        .map(RecordsFilter.Category::filtered)
        .flatten()
}

fun List<RecordsFilter>.getCategoryIds(): List<Long> {
    return getCategoryItems()
        .filterIsInstance<RecordsFilter.CategoryItem.Categorized>()
        .map(RecordsFilter.CategoryItem.Categorized::categoryId)
}

fun List<RecordsFilter>.getFilteredCategoryIds(): List<Long> {
    return getFilteredCategoryItems()
        .filterIsInstance<RecordsFilter.CategoryItem.Categorized>()
        .map(RecordsFilter.CategoryItem.Categorized::categoryId)
}

fun List<RecordsFilter>.getTypeIdsFromCategories(
    recordTypes: List<RecordType>,
    recordTypeCategories: List<RecordTypeCategory>,
): List<Long> {
    return getTypeIdsFromCategories(
        categoryIds = getCategoryIds(),
        categoryItems = getCategoryItems(),
        recordTypes = recordTypes,
        recordTypeCategories = recordTypeCategories,
    )
}

fun List<RecordsFilter>.getTypeIdsFromFilteredCategories(
    recordTypes: List<RecordType>,
    recordTypeCategories: List<RecordTypeCategory>,
): List<Long> {
    return getTypeIdsFromCategories(
        categoryIds = getFilteredCategoryIds(),
        categoryItems = getFilteredCategoryItems(),
        recordTypes = recordTypes,
        recordTypeCategories = recordTypeCategories,
    )
}

fun List<RecordsFilter>.getAllTypeIds(
    recordTypes: List<RecordType>,
    recordTypeCategories: List<RecordTypeCategory>,
): List<Long> {
    return getTypeIds() + getTypeIdsFromCategories(recordTypes, recordTypeCategories)
}

fun List<RecordsFilter>.getAllFilteredTypeIds(
    recordTypes: List<RecordType>,
    recordTypeCategories: List<RecordTypeCategory>,
): List<Long> {
    return getFilteredTypeIds() + getTypeIdsFromFilteredCategories(recordTypes, recordTypeCategories)
}

fun List<RecordsFilter>.getCommentItems(): List<RecordsFilter.CommentItem> {
    return filterIsInstance<RecordsFilter.Comment>()
        .map(RecordsFilter.Comment::items)
        .flatten()
}

fun List<RecordsFilter>.getDate(): RecordsFilter.Date? {
    return filterIsInstance<RecordsFilter.Date>()
        .firstOrNull()
}

fun List<RecordsFilter>.getSelectedTags(): List<RecordsFilter.TagItem> {
    return filterIsInstance<RecordsFilter.Tags>()
        .map(RecordsFilter.Tags::selected)
        .flatten()
}

fun List<RecordsFilter>.getFilteredTags(): List<RecordsFilter.TagItem> {
    return filterIsInstance<RecordsFilter.Tags>()
        .map(RecordsFilter.Tags::filtered)
        .flatten()
}

fun List<RecordsFilter>.getTagIds(): List<Long> {
    return getSelectedTags()
        .filterIsInstance<RecordsFilter.TagItem.Tagged>()
        .map(RecordsFilter.TagItem.Tagged::tagId)
}

fun List<RecordsFilter>.getManuallyFilteredItems(): Map<RecordsFilter.ManuallyFilteredItem, Boolean> {
    return filterIsInstance<RecordsFilter.ManuallyFiltered>()
        .map(RecordsFilter.ManuallyFiltered::items)
        .flatten()
        .associateWith { true }
}

fun List<RecordsFilter>.getDaysOfWeek(): Set<DayOfWeek> {
    return filterIsInstance<RecordsFilter.DaysOfWeek>()
        .map(RecordsFilter.DaysOfWeek::items)
        .flatten()
        .toSet()
}

fun List<RecordsFilter>.getTimeOfDay(): Range? {
    return filterIsInstance<RecordsFilter.TimeOfDay>()
        .map(RecordsFilter.TimeOfDay::range)
        .firstOrNull()
}

fun List<RecordsFilter>.getDuration(): Range? {
    return filterIsInstance<RecordsFilter.Duration>()
        .map(RecordsFilter.Duration::range)
        .firstOrNull()
}

fun List<RecordsFilter.TagItem>.getTaggedIds(): List<Long> {
    return filterIsInstance<RecordsFilter.TagItem.Tagged>()
        .map(RecordsFilter.TagItem.Tagged::tagId)
}

fun List<RecordsFilter>.hasUntrackedFilter(): Boolean {
    return any { it is RecordsFilter.Untracked }
}

fun List<RecordsFilter>.hasMultitaskFilter(): Boolean {
    return any { it is RecordsFilter.Multitask }
}

fun List<RecordsFilter>.hasSelectedActivityFilter(): Boolean {
    return filterIsInstance<RecordsFilter.Activity>().any {
        it.selected.isNotEmpty()
    }
}

fun List<RecordsFilter>.hasFilteredActivityFilter(): Boolean {
    return filterIsInstance<RecordsFilter.Activity>().any {
        it.filtered.isNotEmpty()
    }
}

fun List<RecordsFilter>.hasSelectedCategoryFilter(): Boolean {
    return filterIsInstance<RecordsFilter.Category>().any {
        it.selected.isNotEmpty()
    }
}

fun List<RecordsFilter>.hasFilteredCategoryFilter(): Boolean {
    return filterIsInstance<RecordsFilter.Category>().any {
        it.filtered.isNotEmpty()
    }
}

fun List<RecordsFilter.CommentItem>.hasNoComment(): Boolean {
    return any { it is RecordsFilter.CommentItem.NoComment }
}

fun List<RecordsFilter.CommentItem>.hasAnyComment(): Boolean {
    return any { it is RecordsFilter.CommentItem.AnyComment }
}

fun List<RecordsFilter.CommentItem>.getComments(): List<String> {
    return filterIsInstance<RecordsFilter.CommentItem.Comment>()
        .map(RecordsFilter.CommentItem.Comment::text)
}

fun List<RecordsFilter>.hasSelectedTagsFilter(): Boolean {
    return filterIsInstance<RecordsFilter.Tags>().any {
        it.selected.isNotEmpty()
    }
}

fun List<RecordsFilter.TagItem>.hasUntaggedItem(): Boolean {
    return any { it is RecordsFilter.TagItem.Untagged }
}

fun List<RecordsFilter.CategoryItem>.hasUncategorizedItem(): Boolean {
    return any { it is RecordsFilter.CategoryItem.Uncategorized }
}

fun List<RecordsFilter>.hasManuallyFiltered(): Boolean {
    return any { it is RecordsFilter.ManuallyFiltered }
}

fun List<RecordsFilter>.hasDuplicationsFilter(): Boolean {
    return any { it is RecordsFilter.Duplications }
}

fun List<RecordsFilter>.getDuplicationItems(): List<RecordsFilter.DuplicationsItem> {
    return filterIsInstance<RecordsFilter.Duplications>()
        .map(RecordsFilter.Duplications::items)
        .flatten()
}

fun List<RecordsFilter.DuplicationsItem>.hasSameActivity(): Boolean {
    return any { it is RecordsFilter.DuplicationsItem.SameActivity }
}

fun List<RecordsFilter.DuplicationsItem>.hasSameTimes(): Boolean {
    return any { it is RecordsFilter.DuplicationsItem.SameTimes }
}

fun RecordBase.toManuallyFilteredItem(): RecordsFilter.ManuallyFilteredItem {
    return when (this) {
        is Record -> if (this.typeId == UNTRACKED_ITEM_ID) {
            RecordsFilter.ManuallyFilteredItem.Untracked(Range(this.timeStarted, this.timeEnded))
        } else {
            RecordsFilter.ManuallyFilteredItem.Tracked(this.id)
        }
        is RunningRecord -> {
            RecordsFilter.ManuallyFilteredItem.Running(this.id)
        }
        is MultitaskRecord -> {
            RecordsFilter.ManuallyFilteredItem.Multitask(this.records.map { it.id })
        }
    }
}

private fun getTypeIdsFromCategories(
    categoryIds: List<Long>,
    categoryItems: List<RecordsFilter.CategoryItem>,
    recordTypes: List<RecordType>,
    recordTypeCategories: List<RecordTypeCategory>,
): List<Long> {
    return categoryIds
        .takeUnless { it.isEmpty() }
        ?.let {
            recordTypeCategories
                .filter { it.categoryId in categoryIds }
                .map(RecordTypeCategory::recordTypeId)
        }
        .orEmpty()
        .let { selectedCategorizedTypes ->
            if (categoryItems.hasUncategorizedItem()) {
                val categorizedTypes = recordTypeCategories
                    .map(RecordTypeCategory::recordTypeId)
                    .distinct()

                selectedCategorizedTypes + recordTypes
                    .filter { it.id !in categorizedTypes }
                    .map(RecordType::id)
            } else {
                selectedCategorizedTypes
            }
        }
}
