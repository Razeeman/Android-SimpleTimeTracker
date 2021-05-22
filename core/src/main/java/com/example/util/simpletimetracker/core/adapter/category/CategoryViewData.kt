package com.example.util.simpletimetracker.core.adapter.category

import androidx.annotation.ColorInt
import com.example.util.simpletimetracker.core.adapter.ViewHolderType
import com.example.util.simpletimetracker.core.viewData.RecordTypeIcon

sealed class CategoryViewData : ViewHolderType {
    abstract val id: Long
    abstract val name: String
    abstract val textColor: Int
    abstract val color: Int

    override fun getUniqueId(): Long = id

    data class Activity(
        override val id: Long,
        override val name: String,
        @ColorInt override val textColor: Int,
        @ColorInt override val color: Int
    ) : CategoryViewData() {

        override fun isValidType(other: ViewHolderType): Boolean = other is Activity
    }

    data class Record(
        override val id: Long,
        override val name: String,
        @ColorInt override val textColor: Int,
        @ColorInt override val color: Int,
        val icon: RecordTypeIcon?
    ) : CategoryViewData() {

        override fun isValidType(other: ViewHolderType): Boolean = other is Record
    }
}