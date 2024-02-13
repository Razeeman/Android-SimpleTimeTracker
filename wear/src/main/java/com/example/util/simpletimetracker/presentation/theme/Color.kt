/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.example.util.simpletimetracker.presentation.theme

import androidx.compose.ui.graphics.Color
import androidx.wear.compose.material.Colors

val Purple200 = Color(0xFFBB86FC)
val Purple700 = Color(0xFF3700B3)
val Teal200 = Color(0xFF03DAC5)
val Red400 = Color(0xFFCF6679)

internal val wearColorPalette: Colors =
    Colors(
        primary = Purple200,
        primaryVariant = Purple700,
        secondary = Teal200,
        secondaryVariant = Teal200,
        error = Red400,
        onPrimary = Color.Black,
        onSecondary = Color.Black,
        onError = Color.Black,
    )

fun hexCodeToColor(hex: String): Color {
    return Color(android.graphics.Color.parseColor(hex))
}