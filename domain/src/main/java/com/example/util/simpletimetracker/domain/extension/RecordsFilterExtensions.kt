package com.example.util.simpletimetracker.domain.extension

import com.example.util.simpletimetracker.domain.model.Range
import com.example.util.simpletimetracker.domain.model.RecordType
import com.example.util.simpletimetracker.domain.model.RecordTypeCategory
import com.example.util.simpletimetracker.domain.model.RecordsFilter

fun List<RecordsFilter>.getTypeIds(): List<Long> {
    return filterIsInstance<RecordsFilter.Activity>()
        .map(RecordsFilter.Activity::typeIds)
        .flatten()
}

fun List<RecordsFilter>.getCategoryItems(): List<RecordsFilter.CategoryItem> {
    return filterIsInstance<RecordsFilter.Category>()
        .map(RecordsFilter.Category::items)
        .flatten()
}

fun List<RecordsFilter>.getCategoryIds(): List<Long> {
    return getCategoryItems()
        .filterIsInstance<RecordsFilter.CategoryItem.Categorized>()
        .map(RecordsFilter.CategoryItem.Categorized::categoryId)
}

fun List<RecordsFilter>.getTypeIdsFromCategories(
    recordTypes: List<RecordType>,
    recordTypeCategories: List<RecordTypeCategory>
): List<Long> {
    return getCategoryIds()
        .takeUnless { it.isEmpty() }
        ?.let { categoryIds ->
            recordTypeCategories
                .filter { it.categoryId in categoryIds }
                .map(RecordTypeCategory::recordTypeId)
        }
        .orEmpty()
        .let { selectedCategorizedTypes ->
            if (getCategoryItems().hasUncategorizedItem()) {
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

fun List<RecordsFilter>.getAllTypeIds(
    recordTypes: List<RecordType>,
    recordTypeCategories: List<RecordTypeCategory>
): List<Long> {
    return getTypeIds() + getTypeIdsFromCategories(recordTypes, recordTypeCategories)
}

fun List<RecordsFilter>.getComment(): String? {
    return filterIsInstance<RecordsFilter.Comment>()
        .map(RecordsFilter.Comment::comment)
        .firstOrNull()
}

fun List<RecordsFilter>.getDate(): Range? {
    return filterIsInstance<RecordsFilter.Date>()
        .map(RecordsFilter.Date::range)
        .firstOrNull()
}

fun List<RecordsFilter>.getSelectedTags(): List<RecordsFilter.TagItem> {
    return filterIsInstance<RecordsFilter.SelectedTags>()
        .map(RecordsFilter.SelectedTags::items)
        .flatten()
}

fun List<RecordsFilter>.getFilteredTags(): List<RecordsFilter.TagItem> {
    return filterIsInstance<RecordsFilter.FilteredTags>()
        .map(RecordsFilter.FilteredTags::items)
        .flatten()
}

fun List<RecordsFilter>.getManuallyFilteredRecordIds(): List<Long> {
    return filterIsInstance<RecordsFilter.ManuallyFiltered>()
        .map(RecordsFilter.ManuallyFiltered::recordIds)
        .flatten()
}

fun List<RecordsFilter.TagItem>.getTaggedIds(): List<Long> {
    return filterIsInstance<RecordsFilter.TagItem.Tagged>()
        .map(RecordsFilter.TagItem.Tagged::tagId)
}

fun List<RecordsFilter>.hasActivityFilter(): Boolean {
    return any { it is RecordsFilter.Activity }
}

fun List<RecordsFilter>.hasCategoryFilter(): Boolean {
    return any { it is RecordsFilter.Category }
}

fun List<RecordsFilter>.hasSelectedTagsFilter(): Boolean {
    return any { it is RecordsFilter.SelectedTags }
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
