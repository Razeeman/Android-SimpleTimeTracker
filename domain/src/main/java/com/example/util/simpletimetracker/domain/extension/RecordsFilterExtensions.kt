package com.example.util.simpletimetracker.domain.extension

import com.example.util.simpletimetracker.domain.model.Range
import com.example.util.simpletimetracker.domain.model.RecordTypeCategory
import com.example.util.simpletimetracker.domain.model.RecordsFilter

fun List<RecordsFilter>.getTypeIds(): List<Long> {
    return filterIsInstance<RecordsFilter.Activity>()
        .map(RecordsFilter.Activity::typeIds)
        .flatten()
}

fun List<RecordsFilter>.getCategoryIds(): List<Long> {
    return filterIsInstance<RecordsFilter.Category>()
        .map(RecordsFilter.Category::categoryIds)
        .flatten()
}

fun List<RecordsFilter>.getTypeIdsFromCategories(
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
}

fun List<RecordsFilter>.getAllTypeIds(
    recordTypeCategories: List<RecordTypeCategory>
): List<Long> {
    return getTypeIds() + getTypeIdsFromCategories(recordTypeCategories)
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

fun List<RecordsFilter>.getSelectedTags(): List<RecordsFilter.Tag> {
    return filterIsInstance<RecordsFilter.SelectedTags>()
        .map(RecordsFilter.SelectedTags::tags)
        .flatten()
}

fun List<RecordsFilter>.getFilteredTags(): List<RecordsFilter.Tag> {
    return filterIsInstance<RecordsFilter.FilteredTags>()
        .map(RecordsFilter.FilteredTags::tags)
        .flatten()
}

fun List<RecordsFilter>.getManuallyFilteredRecordIds(): List<Long> {
    return filterIsInstance<RecordsFilter.ManuallyFiltered>()
        .map(RecordsFilter.ManuallyFiltered::recordIds)
        .flatten()
}

fun List<RecordsFilter.Tag>.getTaggedIds(): List<Long> {
    return filterIsInstance<RecordsFilter.Tag.Tagged>()
        .map(RecordsFilter.Tag.Tagged::tagId)
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

fun List<RecordsFilter.Tag>.hasUntaggedItem(): Boolean {
    return any { it is RecordsFilter.Tag.Untagged }
}

fun List<RecordsFilter>.hasManuallyFiltered(): Boolean {
    return any { it is RecordsFilter.ManuallyFiltered }
}

