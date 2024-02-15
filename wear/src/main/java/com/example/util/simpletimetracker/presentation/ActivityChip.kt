/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.example.util.simpletimetracker.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.ChipDefaults
import androidx.wear.compose.material.Text
import com.example.util.simpletimetracker.presentation.theme.hexCodeToColor
import com.example.util.simpletimetracker.wearrpc.Activity
import com.example.util.simpletimetracker.wearrpc.Tag
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun ActivityChip(
    activity: Activity,
    startedAt: Long? = null,
    tags: Array<Tag> = arrayOf(),
    onClick: () -> Unit = {},
) {
    val briefIcon = if (activity.icon.startsWith("ic_")) {
        "?"
    } else {
        activity.icon.substring(0, activity.icon.length.coerceAtMost(2))
    }
    val tagsList = if (tags.isNotEmpty()) {
        tags.joinToString(", ") { it.name }
    } else {
        ""
    }
    val tagString = if (tagsList.length > 0) {
        " ($tagsList)"
    } else {
        ""
    }
    val color = hexCodeToColor(activity.color)
    var modifier = Modifier
        .fillMaxWidth(0.9f)
        .padding(top = 10.dp)
    Chip(
        modifier = modifier,
        label = {
            Text(
                text = "$briefIcon : ${activity.name}" + tagString,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
        border = ChipDefaults.chipBorder(
            borderStroke = if (startedAt != null) {
                BorderStroke(2.dp, Color.White)
            } else {
                null
            },
        ),
        secondaryLabel = {
            if (startedAt != null) {
                Text("Since ${recentTimestampToString(startedAt)}")
            } else {
                null
            }
        },
        colors = ChipDefaults.chipColors(
            backgroundColor = color,
        ),
        onClick = onClick,
    )
}

fun recentTimestampToString(epochMillis: Long): String {
    // Someday, it would be nice for this to show nicer time strings
    // e.g. "a few minutes ago", "yesterday", etc.
    val time = LocalDateTime.ofInstant(
        Instant.ofEpochMilli(epochMillis),
        ZoneId.systemDefault(),
    )
    if (time > LocalDateTime.ofInstant(Instant.now(), ZoneId.systemDefault()).minusDays(1)) {
        return time.format(DateTimeFormatter.ISO_LOCAL_TIME)
    } else {
        return time.format(DateTimeFormatter.ISO_DATE_TIME).replace("T", " ")
    }

}

@Preview()
@Composable
fun SampleCooking() {
    ActivityChip(Activity(123, "Cooking", "🎉", "#123456"))
}

@Preview()
@Composable
fun SampleSleep() {
    ActivityChip(Activity(456, "Sleeping", "🛏️", "#ABCDEF"))
}

@Preview()
@Composable
fun CurrentlyRunning() {
    ActivityChip(Activity(456, "Sleeping", "🛏️", "#ABCDEF"), startedAt = 1706751601000L)
}

@Preview()
@Composable
fun CurrentlyRunningWithTags() {
    ActivityChip(
        Activity(456, "Sleeping", "🛏️", "#ABCDEF"), startedAt = 1706751601000L,
        tags = arrayOf(
            Tag(id = 2, name = "Work"),
            Tag(id = 4, name = "Hotel"),
        ),
    )
}