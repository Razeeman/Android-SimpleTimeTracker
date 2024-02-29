/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.example.util.simpletimetracker.presentation.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.wear.compose.material.ChipDefaults
import androidx.wear.compose.material.CompactChip
import androidx.wear.compose.material.Text
import com.example.util.simpletimetracker.R

@Composable
fun CreditsButton(onClick: () -> Unit) {
    CompactChip(
        onClick = onClick,
        label = { Text(LocalContext.current.getString(R.string.credits_button)) },
        colors = ChipDefaults.chipColors(
            backgroundColor = Color.Transparent
        )
    )
}