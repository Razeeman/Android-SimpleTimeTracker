package com.example.util.simpletimetracker.navigation.params.screen

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

sealed interface RecordsFilterParam : Parcelable {

    @Parcelize
    data class Activity(val typeIds: List<Long>) : RecordsFilterParam

    @Parcelize
    data class Category(val items: List<CategoryItem>) : RecordsFilterParam

    @Parcelize
    data class Comment(val comment: String) : RecordsFilterParam

    @Parcelize
    data class Date(val rangeStart: Long, val rangeEnd: Long) : RecordsFilterParam

    @Parcelize
    data class SelectedTags(val items: List<TagItem>) : RecordsFilterParam

    @Parcelize
    data class FilteredTags(val items: List<TagItem>) : RecordsFilterParam

    @Parcelize
    data class ManuallyFiltered(val recordIds: List<Long>) : RecordsFilterParam

    sealed interface CategoryItem : Parcelable {
        @Parcelize
        data class Categorized(val categoryId: Long) : CategoryItem

        @Parcelize
        object Uncategorized : CategoryItem
    }

    sealed interface TagItem : Parcelable {
        @Parcelize
        data class Tagged(val tagId: Long) : TagItem

        @Parcelize
        object Untagged : TagItem
    }
}