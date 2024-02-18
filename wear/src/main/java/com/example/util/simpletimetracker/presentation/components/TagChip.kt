/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.example.util.simpletimetracker.presentation.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.style.TextOverflow
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.Text
import com.example.util.simpletimetracker.wearrpc.Tag

@Composable
fun TagChip(tag: Tag, onClick: () -> Unit) {
    Chip(
        onClick = onClick,
        label = { Text(tag.name, maxLines = 1, overflow = TextOverflow.Ellipsis) },
    )
}