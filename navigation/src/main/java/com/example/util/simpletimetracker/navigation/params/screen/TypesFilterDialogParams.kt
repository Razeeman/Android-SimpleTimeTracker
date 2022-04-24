package com.example.util.simpletimetracker.navigation.params.screen

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class TypesFilterDialogParams(
    val tag: String = "",
    val filter: TypesFilterParams = TypesFilterParams()
) : ScreenParams, Parcelable