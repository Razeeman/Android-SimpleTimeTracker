package com.example.util.simpletimetracker.navigation.params.screen

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class TypesSelectionDialogParams(
    val tag: String,
    val title: String,
    val subtitle: String,
    val type: Type,
    val selectedTypeIds: List<Long>,
    val selectedTagValues: List<TagData>,
    val selectedTagValueOnStart: List<Long>,
    val isMultiSelectAvailable: Boolean,
    // Allows showing items that are archived but was selected earlier.
    val idsShouldBeVisible: List<Long>,
    // Allows excluding certain ids from showing.
    val excludedTypeIds: List<Long> = emptyList(),
    val showHints: Boolean,
    val allowTagValueSelection: Boolean,
    val allowSelectTagValueOnStart: Boolean = false,
) : Parcelable, ScreenParams {

    sealed interface Type : Parcelable {
        @Parcelize
        data object Activity : Type

        sealed interface Tag : Type {
            @Parcelize
            data object All : Tag

            @Parcelize
            data class ByType(val typeIds: List<Long>) : Tag
        }
    }

    @Parcelize
    data class TagData(
        val tagId: Long,
        val numericValue: Double?,
    ) : Parcelable

    companion object {
        val Empty = TypesSelectionDialogParams(
            tag = "",
            title = "",
            subtitle = "",
            selectedTypeIds = emptyList(),
            selectedTagValues = emptyList(),
            selectedTagValueOnStart = emptyList(),
            type = Type.Activity,
            isMultiSelectAvailable = false,
            idsShouldBeVisible = emptyList(),
            showHints = false,
            allowTagValueSelection = false,
            allowSelectTagValueOnStart = false,
        )
    }
}