package com.example.util.simpletimetracker.resources

import androidx.annotation.DrawableRes

sealed interface CommonActivityIcon {

    data class Image(@DrawableRes val iconId: Int) : CommonActivityIcon

    data class Text(val text: String) : CommonActivityIcon
}