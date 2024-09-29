package com.example.util.simpletimetracker.domain.model

sealed interface RecordsFilter {

    // Incompatible with Activity, Category, Comment, SelectedTags, FilteredTags, ManuallyFiltered.
    object Untracked : RecordsFilter

    object Multitask : RecordsFilter

    data class Activity(val typeIds: List<Long>) : RecordsFilter

    data class Category(val items: List<CategoryItem>) : RecordsFilter

    data class Comment(val items: List<CommentItem>) : RecordsFilter

    data class Date(val range: RangeLength, val position: Int) : RecordsFilter

    data class SelectedTags(val items: List<TagItem>) : RecordsFilter

    data class FilteredTags(val items: List<TagItem>) : RecordsFilter

    data class ManuallyFiltered(val recordIds: List<Long>) : RecordsFilter

    data class DaysOfWeek(val items: List<DayOfWeek>) : RecordsFilter

    data class TimeOfDay(val range: Range) : RecordsFilter // duration-from, duration-to in range.

    data class Duration(val range: Range) : RecordsFilter // duration-from, duration-to in range.

    sealed interface CommentItem {
        object NoComment : CommentItem
        object AnyComment : CommentItem
        data class Comment(val text: String) : CommentItem
    }

    sealed interface CategoryItem {
        data class Categorized(val categoryId: Long) : CategoryItem
        object Uncategorized : CategoryItem
    }

    sealed interface TagItem {
        data class Tagged(val tagId: Long) : TagItem
        object Untagged : TagItem
    }
}