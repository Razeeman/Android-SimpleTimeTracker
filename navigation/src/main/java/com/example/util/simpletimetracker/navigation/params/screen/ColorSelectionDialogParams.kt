package com.example.util.simpletimetracker.navigation.params.screen

import android.graphics.Color
import android.os.Parcelable
import androidx.annotation.ColorInt
import kotlinx.parcelize.Parcelize

@Parcelize
data class ColorSelectionDialogParams(
    @ColorInt val preselectedColor: Int = Color.RED,
) : ScreenParams, Parcelable