package com.example.util.simpletimetracker.navigation.params

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class StandardDialogParams(
    val tag: String? = null,
    val data: Parcelable? = null,
    val title: String = "",
    val message: String = "",
    val btnPositive: String = "",
    val btnNegative: String = ""
) : Parcelable