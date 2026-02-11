package com.example.util.simpletimetracker.feature_notification.activitySwitch.manager

import com.example.util.simpletimetracker.domain.record.model.RecordBase
import com.example.util.simpletimetracker.feature_views.GoalCheckmarkView
import com.example.util.simpletimetracker.feature_views.viewData.RecordTypeIcon

sealed interface NotificationControlsParams {
    data object Disabled : NotificationControlsParams

    data class Enabled(
        val typesShift: Int,
        val tagsShift: Int,
        val controlIconColor: Int,
        val isMultipleTagAvailable: Boolean,
        val selectedTypeId: Long?,
        val selectedTags: List<RecordBase.Tag> = emptyList(),
        val editingTagId: Long? = null,
        val editingTagValueInput: String? = null,
        val viewState: ViewState,
    ) : NotificationControlsParams

    sealed interface ViewState {
        val hint: String

        data class TypeSelection(
            override val hint: String,
            val types: List<Type>,
            val tags: List<Tag>,
            val controlIconPrev: RecordTypeIcon,
            val controlIconNext: RecordTypeIcon,
            val filteredTypeColor: Int,
        ) : ViewState

        data class TagValueSelection(
            override val hint: String,
            val numbers: List<TagValueControls>,
            val controlIconBack: RecordTypeIcon,
            val controlBackColor: Int,
            val controlIconSave: RecordTypeIcon,
            val controlSaveColor: Int,
            val controlIconRemove: RecordTypeIcon,
        ) : ViewState
    }

    sealed interface Type {
        data class Present(
            val id: Long,
            val icon: RecordTypeIcon,
            val color: Int,
            val checkState: GoalCheckmarkView.CheckState,
            val isComplete: Boolean,
        ) : Type

        data object Empty : Type
    }

    sealed interface Tag {
        data class Present(
            val id: Long,
            val text: String,
            val color: Int,
            val isSelected: Boolean,
        ) : Tag

        data object Empty : Tag
    }

    sealed interface TagValueControls {
        data class Present(
            val type: Type,
            val text: String,
            val color: Int,
        ) : TagValueControls {

            sealed interface Type {
                data class Number(val number: Int) : Type
                data object Dot : Type
                data object PlusMinus : Type
            }
        }

        data object Empty : TagValueControls
    }
}