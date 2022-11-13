package com.example.util.simpletimetracker.navigation.params.screen

import android.os.Parcelable
import androidx.annotation.DrawableRes
import kotlinx.parcelize.Parcelize

sealed class RecordTypeIconParams : Parcelable {
    @Parcelize
    data class Image(@DrawableRes val iconId: Int) : RecordTypeIconParams()

    @Parcelize
    data class Text(val text: String) : RecordTypeIconParams()
}