package com.example.util.simpletimetracker.feature_base_adapter.category

import androidx.annotation.ColorInt
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_views.viewData.RecordTypeIcon

sealed class CategoryViewData : ViewHolderType {
    abstract val id: Long
    abstract val name: String
    abstract val iconColor: Int
    abstract val color: Int

    override fun getUniqueId(): Long = id

    data class Activity(
        override val id: Long,
        override val name: String,
        @ColorInt override val iconColor: Int,
        @ColorInt override val color: Int
    ) : CategoryViewData() {

        override fun isValidType(other: ViewHolderType): Boolean = other is Activity
    }

    sealed class Record : CategoryViewData() {
        abstract val icon: RecordTypeIcon?
        abstract val iconAlpha: Float

        data class Tagged(
            override val id: Long,
            override val name: String,
            @ColorInt override val iconColor: Int,
            @ColorInt override val color: Int,
            override val icon: RecordTypeIcon?,
            override val iconAlpha: Float = 1.0f
        ) : Record() {

            override fun isValidType(other: ViewHolderType): Boolean = other is Tagged
        }

        data class General(
            override val id: Long,
            override val name: String,
            @ColorInt override val iconColor: Int,
            @ColorInt override val color: Int,
            override val icon: RecordTypeIcon? = null,
            override val iconAlpha: Float = 1.0f
        ) : Record() {

            override fun isValidType(other: ViewHolderType): Boolean = other is General
        }

        data class Untagged(
            val typeId: Long,
            override val name: String,
            @ColorInt override val iconColor: Int,
            @ColorInt override val color: Int,
            override val icon: RecordTypeIcon?,
            override val iconAlpha: Float = 1.0f
        ) : Record() {

            override val id: Long get() = typeId

            override fun isValidType(other: ViewHolderType): Boolean = other is Untagged
        }
    }
}