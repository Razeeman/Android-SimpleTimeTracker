/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.example.util.simpletimetracker.features.records.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
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
import com.example.util.simpletimetracker.domain.model.WearActivityIcon
import com.example.util.simpletimetracker.features.activities.ui.ActivityIcon
import com.example.util.simpletimetracker.presentation.ui.ACTIVITY_RUNNING_VIEW_HEIGHT
import com.example.util.simpletimetracker.utils.getCoercedFontScale

@Immutable
data class RecordChipState(
    val key: String,
    val name: String,
    val icon: WearActivityIcon,
    val color: Long,
    val tags: String,
    val time: String,
    val duration: String,
    val isRunning: Boolean,
    val isUntracked: Boolean,
)

@Composable
fun RecordChip(
    state: RecordChipState,
) {
    val height = ACTIVITY_RUNNING_VIEW_HEIGHT *
        getCoercedFontScale()
    Chip(
        modifier = Modifier
            .height(height.dp)
            .fillMaxWidth(),
        icon = {
            ActivityIcon(
                modifier = Modifier.heightIn(max = 20.dp),
                activityIcon = state.icon,
            )
        },
        label = {
            Text(
                text = if (state.tags.isEmpty()) {
                    state.name
                } else {
                    // TODO WEAR show tags with alpha same as in running record
                    "${state.name} - ${state.tags}"
                },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
        secondaryLabel = {
            Column {
                Text(
                    text = state.time,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontSize = 10.sp,
                )
                Text(
                    text = state.duration,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontSize = 10.sp,
                )
            }
        },
        colors = ChipDefaults.chipColors(
            backgroundColor = Color(state.color),
        ),
        onClick = {},
    )
}

@Preview(device = WearDevices.LARGE_ROUND)
@Composable
private fun Sample() {
    RecordChip(
        RecordChipState(
            "0", "Cooking", WearActivityIcon.Text("🎉"), 0xFF123456, "Home", "10:20 – 10:26", "6m 5s", false, false,
        ),
    )
}

@Preview(device = WearDevices.LARGE_ROUND, fontScale = 2f)
@Composable
private fun SampleFontScale() {
    RecordChip(
        RecordChipState(
            key = "0",
            name = "Cooking",
            icon = WearActivityIcon.Text("🎉"),
            color = 0xFF123456,
            tags = "Home",
            time = "10:20 – 10:26",
            duration = "6m 5s",
            isRunning = false,
            isUntracked = false,
        ),
    )
}

@Preview(device = WearDevices.LARGE_ROUND)
@Composable
private fun SampleSleep() {
    RecordChip(
        RecordChipState(
            key = "0",
            name = "Sleeping",
            icon = WearActivityIcon.Text("🛏️"),
            color = 0xFF123456,
            tags = "",
            time = "00:00 – 06:00",
            duration = "6h 0m 0s",
            isRunning = false,
            isUntracked = false,
        ),
    )
}

@Preview(device = WearDevices.LARGE_ROUND)
@Composable
private fun SampleText() {
    RecordChip(
        RecordChipState(
            key = "0",
            name = "Sleeping",
            icon = WearActivityIcon.Text("Zzzz"),
            color = 0xFF123456,
            tags = "",
            time = "00:00 – 06:00",
            duration = "6h 0m 0s",
            isRunning = false,
            isUntracked = false,
        ),
    )
}

@Preview(device = WearDevices.LARGE_ROUND)
@Composable
private fun SampleIcon() {
    RecordChip(
        RecordChipState(
            key = "0",
            name = "Sleeping",
            icon = WearActivityIcon.Image(R.drawable.ic_hotel_24px),
            color = 0xFF123456,
            tags = "",
            time = "00:00 – 06:00",
            duration = "6h 0m 0s",
            isRunning = false,
            isUntracked = false,
        ),
    )
}

@Preview(device = WearDevices.LARGE_ROUND)
@Composable
private fun SampleLongName() {
    RecordChip(
        RecordChipState(
            key = "0",
            name = "Some very long activity name",
            icon = WearActivityIcon.Image(R.drawable.ic_hotel_24px),
            color = 0xFF123456,
            tags = "Some very long tag name",
            time = "10:20 – 10:26",
            duration = "6m 5s",
            isRunning = false,
            isUntracked = false,
        ),
    )
}

@Preview(device = WearDevices.LARGE_ROUND, fontScale = 2f)
@Composable
private fun SampleLongNameFontScale() {
    RecordChip(
        RecordChipState(
            key = "0",
            name = "Some very long activity name",
            icon = WearActivityIcon.Image(R.drawable.ic_hotel_24px),
            color = 0xFF123456,
            tags = "Some very long tag name",
            time = "10:20 – 10:26",
            duration = "6m 5s",
            isRunning = false,
            isUntracked = false,
        ),
    )
}

@Preview(device = WearDevices.LARGE_ROUND)
@Composable
private fun White() {
    // TODO handle the look of light colored chips
    // Note: A white color is only possible when using the RGB color picker.
    // The default color options in the phone app are mostly darker shades.
    RecordChip(
        RecordChipState(
            key = "0",
            name = "Sleeping",
            icon = WearActivityIcon.Text("🛏️"),
            color = 0xFFFFFFFF,
            tags = "",
            time = "00:00 – 06:00",
            duration = "6h 0m 0s",
            isRunning = false,
            isUntracked = false,
        ),
    )
}

@Preview(device = WearDevices.LARGE_ROUND)
@Composable
private fun Duration() {
    RecordChip(
        RecordChipState(
            key = "0",
            name = "Sleeping",
            icon = WearActivityIcon.Text("🛏️"),
            color = 0xFF123456,
            tags = "",
            time = "00:00 – 03:59",
            duration = "99h 99m 99s",
            isRunning = false,
            isUntracked = false,
        ),
    )
}

@Preview(device = WearDevices.LARGE_ROUND, fontScale = 2f)
@Composable
private fun DurationFontScale() {
    RecordChip(
        RecordChipState(
            key = "0",
            name = "Sleeping",
            icon = WearActivityIcon.Text("🛏️"),
            color = 0xFF123456,
            tags = "",
            time = "00:00 – 03:59",
            duration = "99h 99m 99s",
            isRunning = false,
            isUntracked = false,
        ),
    )
}
