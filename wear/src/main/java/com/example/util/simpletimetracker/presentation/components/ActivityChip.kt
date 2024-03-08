/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.example.util.simpletimetracker.presentation.components

import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.ChipDefaults
import androidx.wear.compose.material.Text
import androidx.wear.tooling.preview.devices.WearDevices
import com.example.util.simpletimetracker.R
import com.example.util.simpletimetracker.presentation.remember.rememberDurationSince
import com.example.util.simpletimetracker.utils.getString
import java.time.Duration
import java.time.Instant

@Composable
fun ActivityChip(
    name: String,
    icon: String,
    color: Long,
    startedAt: Long? = null,
    tags: List<String> = emptyList(),
    onClick: () -> Unit = {},
) {
    val tagsList = tags.takeUnless { it.isEmpty() }
        ?.joinToString(separator = ", ")
        .orEmpty()
    val tagString = if (tagsList.isNotEmpty()) {
        " - $tagsList"
    } else {
        ""
    }
    Chip(
        modifier = Modifier.fillMaxWidth(),
        label = {
            Row(modifier = Modifier.height(IntrinsicSize.Min)) {
                ActivityIcon(activityIcon = icon)
                Text(
                    text = name,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(start = 4.dp),
                )
            }
        },
        secondaryLabel = {
            if (startedAt != null) {
                val startedDiff = rememberDurationSince(epochMillis = startedAt)

                Text(
                    text = durationToLabel(startedDiff) + tagString,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(
                        start = if (tagString.isNotEmpty()) {
                            2.dp
                        } else {
                            22.dp
                        },
                    ),
                )
            }
        },
        colors = ChipDefaults.chipColors(
            backgroundColor = Color(color),
        ),
        onClick = onClick,
    )
}

// Copy from TimeMapper.formatInterval
@Composable
fun durationToLabel(duration: Duration): String {
    val hourString = getString(R.string.time_hour)
    val minuteString = getString(R.string.time_minute)
    val secondString = getString(R.string.time_second)

    val hr = duration.toHours()
    val min = duration.toMinutes() % 60
    val sec = duration.seconds % 60

    val willShowHours = hr != 0L
    val willShowMinutes = willShowHours || min != 0L
    val willShowSeconds = true

    var res = ""
    if (willShowHours) res += "$hr$hourString "
    if (willShowMinutes) res += "$min$minuteString"
    if (willShowMinutes && willShowSeconds) res += " "
    if (willShowSeconds) res += "$sec$secondString"

    return res
}

@Preview(device = WearDevices.LARGE_ROUND)
@Composable
fun SampleCooking() {
    ActivityChip("Cooking", "🎉", 0xFF123456)
}

@Preview(device = WearDevices.LARGE_ROUND)
@Composable
fun Sample() {
    ActivityChip("Cooking", "🎉", 0xFF123456)
}

@Preview(device = WearDevices.LARGE_ROUND)
@Composable
fun SampleSleep() {
    ActivityChip("Sleeping", "🛏️", 0xFFABCDEF)
}

@Preview(device = WearDevices.LARGE_ROUND)
@Composable
fun SampleText() {
    ActivityChip("Sleeping", "Zzzz", 0xFFABCDEF)
}

@Preview(device = WearDevices.LARGE_ROUND)
@Composable
fun SampleIcon() {
    ActivityChip("Sleeping", "ic_hotel_24px", 0xFFABCDEF)
}

@Preview(device = WearDevices.LARGE_ROUND)
@Composable
fun InvalidIcon() {
    ActivityChip("Sleeping", "ic_gobbldeegoock_24px", 0xFFABCDEF)
}

@Preview(device = WearDevices.LARGE_ROUND)
@Composable
fun White() {
    // TODO handle the look of light colored chips
    // Note: A white color is only possible when using the RGB color picker.
    // The default color options in the phone app are mostly darker shades.
    ActivityChip("Sleeping", "🛏️", 0xFFFFFFFF)
}

@Preview(device = WearDevices.LARGE_ROUND)
@Composable
fun CurrentlyRunning() {
    ActivityChip(
        "Sleeping", "🛏️", 0xFFABCDEF,
        startedAt = Instant.now().toEpochMilli() - 365000,
    )
}

@Preview(device = WearDevices.LARGE_ROUND)
@Composable
fun CurrentlyRunningWithTags() {
    ActivityChip(
        "Sleeping", "🛏️", 0xFFABCDEF,
        startedAt = Instant.now().toEpochMilli() - 365000,
        tags = listOf("Work", "Hotel"),
    )
}