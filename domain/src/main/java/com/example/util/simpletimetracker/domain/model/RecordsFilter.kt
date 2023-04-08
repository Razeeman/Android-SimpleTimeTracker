package com.example.util.simpletimetracker.domain.model

sealed interface RecordsFilter {

    data class Activity(val typeIds: List<Long>) : RecordsFilter

    data class Comment(val comment: String) : RecordsFilter

    data class Date(val range: Range) : RecordsFilter

    data class SelectedTags(val tags: List<Tag>) : RecordsFilter

    data class FilteredTags(val tags: List<Tag>) : RecordsFilter

    data class ManuallyFiltered(val recordIds: List<Long>) : RecordsFilter

    sealed interface Tag {
        data class Tagged(val tagId: Long) : Tag

        data class Untagged(val typeId: Long) : Tag
    }
}