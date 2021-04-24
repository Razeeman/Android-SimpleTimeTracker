package com.example.util.simpletimetracker.core.viewData

import android.os.Parcelable
import androidx.annotation.DrawableRes
import kotlinx.android.parcel.Parcelize

sealed class RecordTypeIcon : Parcelable {

    @Parcelize
    data class Image(@DrawableRes val iconId: Int) : RecordTypeIcon()

    @Parcelize
    data class Emoji(val emojiText: String) : RecordTypeIcon()
}
