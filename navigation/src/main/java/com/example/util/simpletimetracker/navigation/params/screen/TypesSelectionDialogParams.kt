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
    val isMultiSelectAvailable: Boolean,
    val idsShouldBeVisible: List<Long>,
) : Parcelable, ScreenParams {

    sealed interface Type : Parcelable {
        @Parcelize
        object Activity : Type

        @Parcelize
        object Tag : Type
    }

    companion object {
        val Empty = TypesSelectionDialogParams(
            tag = "",
            title = "",
            subtitle = "",
            selectedTypeIds = emptyList(),
            type = Type.Activity,
            isMultiSelectAvailable = false,
            idsShouldBeVisible = emptyList(),
        )
    }
}