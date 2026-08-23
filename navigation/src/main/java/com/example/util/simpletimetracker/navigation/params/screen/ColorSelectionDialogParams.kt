package com.example.util.simpletimetracker.navigation.params.screen

import android.graphics.Color
import android.os.Parcelable
import androidx.annotation.ColorInt
import kotlinx.parcelize.Parcelize

@Parcelize
data class ColorSelectionDialogParams(
    val tag: String,
    @ColorInt val preselectedColor: Int,
) : ScreenParams, Parcelable {

    companion object {
        val Empty = ColorSelectionDialogParams(
            tag = "",
            preselectedColor = Color.RED,
        )
    }
}