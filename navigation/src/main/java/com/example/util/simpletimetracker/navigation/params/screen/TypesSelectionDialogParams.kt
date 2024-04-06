package com.example.util.simpletimetracker.navigation.params.screen

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class TypesSelectionDialogParams(
    val tag: String = "",
    val title: String = "",
    val subtitle: String = "",
    val selectedTypeIds: List<Long> = emptyList(),
    val isMultiSelectAvailable: Boolean = false,
) : Parcelable, ScreenParams