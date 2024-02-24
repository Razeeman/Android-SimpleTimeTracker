/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.example.util.simpletimetracker.presentation.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.OutlinedButton

@Composable
fun SubmitButton(onClick: () -> Unit, contentDescription: String = "Submit") {
    OutlinedButton(
        onClick = onClick,
        content = {
            Icon(Icons.AutoMirrored.Rounded.Send, contentDescription = contentDescription)
        },
        modifier = Modifier.padding(all = 8.dp),
    )
}

@Preview
@Composable
private fun Preview() {
    SubmitButton(
        onClick = { /* Log.i("Preview", "Refresh Button clicked!") */ },
    )
}