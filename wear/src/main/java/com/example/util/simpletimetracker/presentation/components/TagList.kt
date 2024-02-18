/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.example.util.simpletimetracker.presentation.components

import androidx.compose.runtime.Composable
import com.example.util.simpletimetracker.presentation.layout.ScaffoldedScrollingColumn
import com.example.util.simpletimetracker.wearrpc.Tag

@Composable
fun TagList(tags: Array<Tag>, onSelectTag: (tag: Tag) -> Unit) {
    ScaffoldedScrollingColumn {
        for (tag in tags) {
            item {
                TagChip(tag) { onSelectTag(tag) }
            }
        }
    }
}