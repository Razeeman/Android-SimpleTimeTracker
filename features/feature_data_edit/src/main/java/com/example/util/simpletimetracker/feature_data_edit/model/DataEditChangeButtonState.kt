package com.example.util.simpletimetracker.feature_data_edit.model

import androidx.annotation.ColorInt

data class DataEditChangeButtonState(
    val enabled: Boolean,
    @ColorInt val backgroundTint: Int,
)