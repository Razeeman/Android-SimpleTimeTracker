/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.example.util.simpletimetracker.presentation.screens.dialog

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.datasource.LoremIpsum
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.dialog.Confirmation
import androidx.wear.compose.material.dialog.Dialog
import androidx.wear.tooling.preview.devices.WearDevices
import com.example.util.simpletimetracker.R

@Composable
fun MessageDialog(
    message: String,
    onDismiss: () -> Unit,
) {
    Dialog(
        showDialog = true,
        onDismissRequest = onDismiss,
    ) {
        Content(
            message = message,
            onDismiss = onDismiss,
        )
    }
}

@Composable
private fun Content(
    message: String,
    onDismiss: () -> Unit = {},
) {
    Confirmation(
        icon = {
            Icon(
                painter = painterResource(R.drawable.wear_error),
                contentDescription = null,
            )
        },
        onTimeout = onDismiss,
    ) {
        Text(
            text = message,
            textAlign = TextAlign.Center,
        )
    }
}

@Preview(device = WearDevices.LARGE_ROUND)
@Composable
private fun Preview() {
    Content(
        message = "Some message",
    )
}

@Preview(device = WearDevices.LARGE_ROUND)
@Composable
private fun PreviewLong() {
    Content(
        message = LoremIpsum().values.first(),
    )
}