package com.example.util.simpletimetracker.feature_views.viewData

import android.os.Parcelable
import androidx.annotation.DrawableRes
import kotlinx.parcelize.Parcelize

sealed class RecordTypeIcon : Parcelable {

    @Parcelize
    data class Image(@DrawableRes val iconId: Int) : RecordTypeIcon()

    @Parcelize
    data class Text(val text: String) : RecordTypeIcon()
}
