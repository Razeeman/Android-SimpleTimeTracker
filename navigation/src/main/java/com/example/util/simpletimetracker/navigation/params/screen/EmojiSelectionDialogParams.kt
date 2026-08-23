package com.example.util.simpletimetracker.navigation.params.screen

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class EmojiSelectionDialogParams(
    val tag: String,
    val color: Color,
    val emojiCodes: List<String>,
) : Parcelable, ScreenParams {

    @Parcelize
    data class Color(
        val colorId: Int,
        val colorInt: String,
    ) : Parcelable

    companion object {
        val Empty = EmojiSelectionDialogParams(
            tag = "",
            color = Color(colorId = 0, colorInt = ""),
            emojiCodes = emptyList(),
        )
    }
}