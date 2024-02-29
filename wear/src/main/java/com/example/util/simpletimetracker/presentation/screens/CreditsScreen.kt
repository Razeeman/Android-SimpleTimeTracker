/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.example.util.simpletimetracker.presentation.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.wear.compose.material.Text
import androidx.wear.tooling.preview.devices.WearDevices
import com.example.util.simpletimetracker.presentation.layout.ScaffoldedScrollingColumn
import com.example.util.simpletimetracker.R

@Composable
fun CreditsScreen() {
    ScaffoldedScrollingColumn{
        item { Text(text = "Simple Time Tracker", fontWeight = FontWeight.Bold) }
        item { Text(text = "for WearOS") }
        item { Text(text = "") }
        item { Text(text = LocalContext.current.getString(R.string.credits_by)) }
        item { Text(text = "") }
        item { Text(text = "Joseph Hale") }
        item { Text(text = "https://jhale.dev") }
        item { Text(text = "") }
        item { Text(text = "@kantahrek") }
        item { Text(text = "") }
        item { Text(text = "Anton Razinkov") }
        item { Text(text = "@Razeeman") }

    }
}


@Preview(device = WearDevices.LARGE_ROUND)
@Composable
fun CreditsScreenPreview() {
    CreditsScreen()
}