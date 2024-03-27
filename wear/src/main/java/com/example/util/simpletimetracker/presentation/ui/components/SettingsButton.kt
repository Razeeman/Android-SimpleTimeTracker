/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.example.util.simpletimetracker.presentation.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.ButtonDefaults
import androidx.wear.compose.material.Icon
import com.example.util.simpletimetracker.R
import com.example.util.simpletimetracker.presentation.theme.ColorInactive

@Composable
fun SettingsButton(
    onClick: () -> Unit = {},
) {
    Button(
        modifier = Modifier.size(ACTIVITY_VIEW_HEIGHT.dp),
        onClick = onClick,
        content = {
            Icon(
                painter = painterResource(id = R.drawable.wear_settings),
                contentDescription = null,
            )
        },
        colors = ButtonDefaults.buttonColors(
            backgroundColor = ColorInactive,
        ),
    )
}

@Preview
@Composable
private fun Preview() {
    SettingsButton()
}