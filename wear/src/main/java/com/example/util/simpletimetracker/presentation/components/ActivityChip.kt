/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.example.util.simpletimetracker.presentation.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import com.example.util.simpletimetracker.domain.WearActivityIcon
import com.example.util.simpletimetracker.presentation.remember.rememberDurationSince
import com.example.util.simpletimetracker.utils.getString
import java.time.Duration
import java.time.Instant

data class ActivityChipState(
    val id: Long,
    val name: String,
    val icon: WearActivityIcon,
    val color: Long,
    val startedAt: Long? = null,
    val tagString: String = "",
)

@Composable
fun ActivityChip(
    state: ActivityChipState,
    onClick: () -> Unit = {},
) {
    val isRunning = state.startedAt != null
    val height = if (isRunning) {
        ACTIVITY_RUNNING_VIEW_HEIGHT
    } else {
        ACTIVITY_VIEW_HEIGHT
    }
    Chip(
        modifier = Modifier
            .height(height.dp)
            .fillMaxWidth(),
        icon = {
            ActivityIcon(
                modifier = Modifier.height(20.dp),
                activityIcon = state.icon,
            )
        },
        label = {
            Row(modifier = Modifier.height(IntrinsicSize.Min)) {
                Text(
                    text = state.name,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        },
        secondaryLabel = {
            Column {
                if (state.tagString.isNotEmpty()) {
                    Text(
                        text = state.tagString,
                        color = Color(0x99FFFFFF),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontSize = 10.sp,
                    )
                }
                if (state.startedAt != null) {
                    val startedDiff = rememberDurationSince(state.startedAt)
                    val text = durationToLabel(startedDiff)
                    Text(
                        text = text,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontSize = 10.sp,
                    )
                }
            }
        },
        colors = ChipDefaults.chipColors(
            backgroundColor = Color(state.color),
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
    ActivityChip(
        ActivityChipState(0, "Cooking", WearActivityIcon.Text("🎉"), 0xFF123456),
    )
}

@Preview(device = WearDevices.LARGE_ROUND)
@Composable
fun Sample() {
    ActivityChip(
        ActivityChipState(0, "Cooking", WearActivityIcon.Text("🎉"), 0xFF123456),
    )
}

@Preview(device = WearDevices.LARGE_ROUND)
@Composable
fun SampleSleep() {
    ActivityChip(
        ActivityChipState(0, "Sleeping", WearActivityIcon.Text("🛏️"), 0xFFABCDEF),
    )
}

@Preview(device = WearDevices.LARGE_ROUND)
@Composable
fun SampleText() {
    ActivityChip(
        ActivityChipState(0, "Sleeping", WearActivityIcon.Text("Zzzz"), 0xFFABCDEF),
    )
}

@Preview(device = WearDevices.LARGE_ROUND)
@Composable
fun SampleIcon() {
    ActivityChip(
        ActivityChipState(0, "Sleeping", WearActivityIcon.Image(R.drawable.ic_hotel_24px), 0xFFABCDEF),
    )
}

@Preview(device = WearDevices.LARGE_ROUND)
@Composable
fun White() {
    // TODO handle the look of light colored chips
    // Note: A white color is only possible when using the RGB color picker.
    // The default color options in the phone app are mostly darker shades.
    ActivityChip(
        ActivityChipState(0, "Sleeping", WearActivityIcon.Text("🛏️"), 0xFFFFFFFF),
    )
}

@Preview(device = WearDevices.LARGE_ROUND)
@Composable
fun CurrentlyRunning() {
    ActivityChip(
        ActivityChipState(
            0, "Sleeping", WearActivityIcon.Text("🛏️"), 0xFFABCDEF,
            startedAt = Instant.now().toEpochMilli() - 365000,
        ),
    )
}

@Preview(device = WearDevices.LARGE_ROUND)
@Composable
fun CurrentlyRunningWithTags() {
    ActivityChip(
        ActivityChipState(
            0, "Sleeping", WearActivityIcon.Text("🛏️"), 0xFFABCDEF,
            startedAt = Instant.now().toEpochMilli() - 365000,
            tagString = "Work, Hotel",
        ),
    )
}