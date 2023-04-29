package com.example.util.simpletimetracker.domain.model

sealed interface RecordsFilter {

    data class Activity(val typeIds: List<Long>) : RecordsFilter

    data class Category(val items: List<CategoryItem>) : RecordsFilter

    data class Comment(val comment: String) : RecordsFilter

    data class Date(val range: Range) : RecordsFilter

    data class SelectedTags(val items: List<TagItem>) : RecordsFilter

    data class FilteredTags(val items: List<TagItem>) : RecordsFilter

    data class ManuallyFiltered(val recordIds: List<Long>) : RecordsFilter

    sealed interface CategoryItem {
        data class Categorized(val categoryId: Long) : CategoryItem
        object Uncategorized : CategoryItem
    }

    sealed interface TagItem {
        data class Tagged(val tagId: Long) : TagItem
        object Untagged : TagItem
    }
}