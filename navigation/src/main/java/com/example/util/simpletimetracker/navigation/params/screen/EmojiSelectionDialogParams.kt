package com.example.util.simpletimetracker.navigation.params.screen

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class EmojiSelectionDialogParams(
    val color: Color = Color(colorId = 0, colorInt = ""),
    val emojiCodes: List<String> = emptyList(),
) : Parcelable, ScreenParams {

    @Parcelize
    data class Color(
        val colorId: Int = 0,
        val colorInt: String = "",
    ) : Parcelable
}