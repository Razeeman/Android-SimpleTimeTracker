package com.example.util.simpletimetracker.feature_change_record_type.viewData

import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes

data class ChangeRecordTypeViewData(
    val name: String,
    @DrawableRes val icon: Int,
    @ColorInt val color: Int
)