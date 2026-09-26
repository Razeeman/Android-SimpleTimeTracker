/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.example.util.simpletimetracker.features.statistics.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
data class StatisticsChipState(
    val id: Long,
    val name: String,
    val icon: WearActivityIcon?,
    val color: Long,
    val duration: String,
    val percent: String?,
)

@Composable
fun StatisticsChip(
    state: StatisticsChipState,
) {
    val height = ACTIVITY_RUNNING_VIEW_HEIGHT *
        getCoercedFontScale()
    Chip(
        modifier = Modifier
            .height(height.dp)
            .fillMaxWidth(),
        icon = {
            if (state.icon != null) {
                ActivityIcon(
                    modifier = Modifier.height(20.dp),
                    activityIcon = state.icon,
                )
            }
        },
        label = {
            Text(
                text = state.name,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
        secondaryLabel = {
            Column {
                Text(
                    text = state.duration,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontSize = 10.sp,
                )
                if (state.percent != null) {
                    Text(
                        text = state.percent,
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
        onClick = {},
    )
}

@Preview(device = WearDevices.LARGE_ROUND)
@Composable
private fun Sample() {
    StatisticsChip(
        StatisticsChipState(0, "Cooking", WearActivityIcon.Text("🎉"), 0xFF123456, "6m 5s", "1%"),
    )
}

@Preview(device = WearDevices.LARGE_ROUND, fontScale = 2f)
@Composable
private fun SampleFontScale() {
    StatisticsChip(
        StatisticsChipState(0, "Cooking", WearActivityIcon.Text("🎉"), 0xFF123456, "6m 5s", "1%"),
    )
}

@Preview(device = WearDevices.LARGE_ROUND)
@Composable
private fun SampleSleep() {
    StatisticsChip(
        StatisticsChipState(0, "Sleeping", WearActivityIcon.Text("🛏️"), 0xFF123456, "6m 5s", "1%"),
    )
}

@Preview(device = WearDevices.LARGE_ROUND)
@Composable
private fun SampleText() {
    StatisticsChip(
        StatisticsChipState(0, "Sleeping", WearActivityIcon.Text("Zzzz"), 0xFF123456, "6m 5s", "1%"),
    )
}

@Preview(device = WearDevices.LARGE_ROUND)
@Composable
private fun SampleIcon() {
    StatisticsChip(
        StatisticsChipState(0, "Sleeping", WearActivityIcon.Image(R.drawable.ic_hotel_24px), 0xFF123456, "6m 5s", "1%"),
    )
}

@Preview(device = WearDevices.LARGE_ROUND)
@Composable
private fun SampleLongName() {
    StatisticsChip(
        StatisticsChipState(
            0,
            "Some very long activity name",
            WearActivityIcon.Image(R.drawable.ic_hotel_24px),
            0xFF123456,
            "6m 5s",
            "1%",
        ),
    )
}

@Preview(device = WearDevices.LARGE_ROUND, fontScale = 2f)
@Composable
private fun SampleLongNameFontScale() {
    StatisticsChip(
        StatisticsChipState(
            0,
            "Some very long activity name",
            WearActivityIcon.Image(R.drawable.ic_hotel_24px),
            0xFF123456,
            "6m 5s",
            "1%",
        ),
    )
}

@Preview(device = WearDevices.LARGE_ROUND)
@Composable
private fun White() {
    // TODO handle the look of light colored chips
    // Note: A white color is only possible when using the RGB color picker.
    // The default color options in the phone app are mostly darker shades.
    StatisticsChip(
        StatisticsChipState(
            0, "Sleeping", WearActivityIcon.Text("🛏️"), 0xFFFFFFFF, "6m 5s", "1%",
        ),
    )
}

@Preview(device = WearDevices.LARGE_ROUND)
@Composable
private fun Duration() {
    StatisticsChip(
        StatisticsChipState(
            0, "Sleeping", WearActivityIcon.Text("🛏️"), 0xFF123456, "99h 99m 99s", "100%",
        ),
    )
}

@Preview(device = WearDevices.LARGE_ROUND, fontScale = 2f)
@Composable
private fun DurationFontScale() {
    StatisticsChip(
        StatisticsChipState(
            0, "Sleeping", WearActivityIcon.Text("🛏️"), 0xFF123456, "99h 99m 99s", "100%",
        ),
    )
}

@Preview(device = WearDevices.LARGE_ROUND)
@Composable
private fun Total() {
    StatisticsChip(
        StatisticsChipState(
            0, "Total", null, 0xFF455A64, "99h 99m 99s", null,
        ),
    )
}

@Preview(device = WearDevices.LARGE_ROUND, fontScale = 2f)
@Composable
private fun TotalFontScale() {
    StatisticsChip(
        StatisticsChipState(
            0, "Total", null, 0xFF455A64, "99h 99m 99s", null,
        ),
    )
}
