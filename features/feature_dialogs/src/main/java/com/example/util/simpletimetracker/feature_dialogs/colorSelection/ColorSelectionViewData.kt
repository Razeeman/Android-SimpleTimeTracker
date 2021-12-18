package com.example.util.simpletimetracker.feature_dialogs.colorSelection

import androidx.annotation.ColorInt

data class ColorSelectionViewData(
    @ColorInt val selectedColor: Int,
    val colorHue: Float,
    val colorSaturation: Float,
    val colorValue: Float,
    val colorHex: String,
    val colorRedString: String,
    val colorGreenString: String,
    val colorBlueString: String,
    val colorHueString: String,
    val colorSaturationString: String,
    val colorValueString: String,
)