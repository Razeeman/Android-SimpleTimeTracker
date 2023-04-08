package com.example.util.simpletimetracker.navigation.params.screen

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

sealed interface RecordsFilterParam : Parcelable {

    @Parcelize
    data class Activity(val typeIds: List<Long>) : RecordsFilterParam

    @Parcelize
    data class Comment(val comment: String) : RecordsFilterParam

    @Parcelize
    data class Date(val rangeStart: Long, val rangeEnd: Long) : RecordsFilterParam

    @Parcelize
    data class SelectedTags(val tags: List<Tag>) : RecordsFilterParam

    @Parcelize
    data class FilteredTags(val tags: List<Tag>) : RecordsFilterParam

    @Parcelize
    data class ManuallyFiltered(val recordIds: List<Long>) : RecordsFilterParam

    sealed interface Tag : Parcelable {
        @Parcelize
        data class Tagged(val tagId: Long) : Tag

        @Parcelize
        data class Untagged(val typeId: Long) : Tag
    }
}