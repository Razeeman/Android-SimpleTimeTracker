/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.example.util.simpletimetracker.features.activities.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
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
import androidx.wear.compose.material.CircularProgressIndicator
import androidx.wear.compose.material.Text
import androidx.wear.tooling.preview.devices.WearDevices
import com.example.util.simpletimetracker.R
import com.example.util.simpletimetracker.domain.model.WearActivityIcon
import com.example.util.simpletimetracker.presentation.theme.ColorInactive
import com.example.util.simpletimetracker.presentation.remember.rememberDurationSince
import com.example.util.simpletimetracker.presentation.ui.ACTIVITY_RUNNING_VIEW_HEIGHT
import com.example.util.simpletimetracker.presentation.ui.ACTIVITY_VIEW_HEIGHT
import com.example.util.simpletimetracker.utils.durationToLabel
import com.example.util.simpletimetracker.utils.getCoercedFontScale
import java.time.Instant

@Immutable
data class ActivityChipState(
    val id: Long,
    val name: String,
    val icon: WearActivityIcon,
    val color: Long,
    val type: ActivityChipType = ActivityChipType.Base,
    val isRunning: Boolean = false,
    val timeHint: TimeHint = TimeHint.None,
    val timeHint2: TimeHint = TimeHint.None,
    val tagString: String = "",
    val isLoading: Boolean = false,
    val hint: String = "",
) {

    val uniqueId: UniqueId = UniqueId(id, type)

    data class UniqueId(
        private val id: Long,
        private val type: ActivityChipType = ActivityChipType.Base,
    )

    sealed interface TimeHint {
        object None : TimeHint
        data class Timer(val startedAt: Long) : TimeHint
        data class Duration(val millis: Long) : TimeHint
    }
}

@Composable
fun ActivityChip(
    state: ActivityChipState,
    onClick: () -> Unit = {},
) {
    val isRunning = state.timeHint !is ActivityChipState.TimeHint.None
    val height = if (isRunning) {
        ACTIVITY_RUNNING_VIEW_HEIGHT
    } else {
        ACTIVITY_VIEW_HEIGHT
    } * getCoercedFontScale()
    Chip(
        modifier = Modifier
            .height(height.dp)
            .fillMaxWidth(),
        icon = {
            if (!state.isLoading) {
                ActivityIcon(
                    modifier = Modifier.height(20.dp),
                    activityIcon = state.icon,
                )
            } else {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
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
                if (state.tagString.isNotEmpty()) {
                    Text(
                        text = state.tagString,
                        color = Color(0x99FFFFFF),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontSize = 10.sp,
                    )
                }
                when (val timeHint = state.timeHint) {
                    is ActivityChipState.TimeHint.Timer -> {
                        val startedDiff = rememberDurationSince(timeHint.startedAt)
                        durationToLabel(startedDiff)
                    }
                    is ActivityChipState.TimeHint.Duration -> {
                        val timestamp = java.time.Duration.ofMillis(timeHint.millis)
                        durationToLabel(timestamp)
                    }
                    is ActivityChipState.TimeHint.None -> {
                        null
                    }
                }?.let {
                    Text(
                        text = it,
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
    if (state.hint.isNotEmpty()) {
        Text(
            modifier = Modifier
                .background(
                    color = ColorInactive,
                    shape = CircleShape,
                )
                .padding(horizontal = 8.dp),
            text = state.hint,
            fontSize = 11.sp,
            lineHeight = 11.sp,
        )
    }
}

@Preview(device = WearDevices.LARGE_ROUND)
@Composable
private fun Sample() {
    ActivityChip(
        ActivityChipState(0, "Cooking", WearActivityIcon.Text("🎉"), 0xFF123456),
    )
}

@Preview(device = WearDevices.LARGE_ROUND, fontScale = 2f)
@Composable
private fun SampleFontScale() {
    ActivityChip(
        ActivityChipState(0, "Cooking", WearActivityIcon.Text("🎉"), 0xFF123456),
    )
}

@Preview(device = WearDevices.LARGE_ROUND)
@Composable
private fun SampleSleep() {
    ActivityChip(
        ActivityChipState(0, "Sleeping", WearActivityIcon.Text("🛏️"), 0xFF123456),
    )
}

@Preview(device = WearDevices.LARGE_ROUND)
@Composable
private fun SampleText() {
    ActivityChip(
        ActivityChipState(0, "Sleeping", WearActivityIcon.Text("Zzzz"), 0xFF123456),
    )
}

@Preview(device = WearDevices.LARGE_ROUND)
@Composable
private fun SampleIcon() {
    ActivityChip(
        ActivityChipState(0, "Sleeping", WearActivityIcon.Image(R.drawable.ic_hotel_24px), 0xFF123456),
    )
}

@Preview(device = WearDevices.LARGE_ROUND)
@Composable
private fun SampleLongName() {
    ActivityChip(
        ActivityChipState(
            0,
            "Some very long activity name",
            WearActivityIcon.Image(R.drawable.ic_hotel_24px),
            0xFF123456,
        ),
    )
}

@Preview(device = WearDevices.LARGE_ROUND, fontScale = 2f)
@Composable
private fun SampleLongNameFontScale() {
    ActivityChip(
        ActivityChipState(
            0,
            "Some very long activity name",
            WearActivityIcon.Image(R.drawable.ic_hotel_24px),
            0xFF123456,
        ),
    )
}

@Preview(device = WearDevices.LARGE_ROUND)
@Composable
private fun SampleLoading() {
    ActivityChip(
        ActivityChipState(
            0, "Sleeping", WearActivityIcon.Image(R.drawable.ic_hotel_24px), 0xFF123456,
            isLoading = true,
        ),
    )
}

@Preview(device = WearDevices.LARGE_ROUND)
@Composable
private fun White() {
    // TODO handle the look of light colored chips
    // Note: A white color is only possible when using the RGB color picker.
    // The default color options in the phone app are mostly darker shades.
    ActivityChip(
        ActivityChipState(0, "Sleeping", WearActivityIcon.Text("🛏️"), 0xFFFFFFFF),
    )
}

@Preview(device = WearDevices.LARGE_ROUND)
@Composable
private fun CurrentlyRunning() {
    ActivityChip(
        ActivityChipState(
            0, "Sleeping", WearActivityIcon.Text("🛏️"), 0xFF123456,
            timeHint = ActivityChipState.TimeHint.Timer(
                Instant.now().toEpochMilli() - 365000,
            ),
        ),
    )
}

@Preview(device = WearDevices.LARGE_ROUND, fontScale = 2f)
@Composable
private fun CurrentlyRunningFontScale() {
    ActivityChip(
        ActivityChipState(
            0, "Sleeping", WearActivityIcon.Text("🛏️"), 0xFF123456,
            timeHint = ActivityChipState.TimeHint.Timer(
                Instant.now().toEpochMilli() - 365000,
            ),
        ),
    )
}

@Preview(device = WearDevices.LARGE_ROUND)
@Composable
private fun CurrentlyRunningLoading() {
    ActivityChip(
        ActivityChipState(
            0, "Sleeping", WearActivityIcon.Text("🛏️"), 0xFF123456,
            timeHint = ActivityChipState.TimeHint.Timer(
                Instant.now().toEpochMilli() - 365000,
            ),
            isLoading = true,
        ),
    )
}

@Preview(device = WearDevices.LARGE_ROUND)
@Composable
private fun CurrentlyRunningWithTags() {
    ActivityChip(
        ActivityChipState(
            0, "Sleeping", WearActivityIcon.Text("🛏️"), 0xFF123456,
            timeHint = ActivityChipState.TimeHint.Timer(
                Instant.now().toEpochMilli() - 365000,
            ),
            tagString = "Work, Hotel",
        ),
    )
}

@Preview(device = WearDevices.LARGE_ROUND, fontScale = 2f)
@Composable
private fun CurrentlyRunningWithTagsFontScale() {
    ActivityChip(
        ActivityChipState(
            0, "Sleeping", WearActivityIcon.Text("🛏️"), 0xFF123456,
            timeHint = ActivityChipState.TimeHint.Timer(
                Instant.now().toEpochMilli() - 365000,
            ),
            tagString = "Work, Hotel",
        ),
    )
}

@Preview(device = WearDevices.LARGE_ROUND)
@Composable
private fun CurrentlyRunningWithTagsLoading() {
    ActivityChip(
        ActivityChipState(
            0, "Sleeping", WearActivityIcon.Text("🛏️"), 0xFF123456,
            timeHint = ActivityChipState.TimeHint.Timer(
                Instant.now().toEpochMilli() - 365000,
            ),
            tagString = "Work, Hotel",
            isLoading = true,
        ),
    )
}

@Preview(device = WearDevices.LARGE_ROUND)
@Composable
private fun Duration() {
    ActivityChip(
        ActivityChipState(
            0, "Sleeping", WearActivityIcon.Text("🛏️"), 0xFF123456,
            timeHint = ActivityChipState.TimeHint.Duration(
                36500,
            ),
        ),
    )
}

@Preview(device = WearDevices.LARGE_ROUND)
@Composable
private fun WithHint() {
    ActivityChip(
        ActivityChipState(
            0, "Sleeping", WearActivityIcon.Text("🛏️"), 0xFF123456,
            timeHint = ActivityChipState.TimeHint.Duration(
                36500,
            ),
            hint = "Last",
        ),
    )
}

@Preview(device = WearDevices.LARGE_ROUND, fontScale = 2f)
@Composable
private fun WithHintFontScale() {
    ActivityChip(
        ActivityChipState(
            0, "Sleeping", WearActivityIcon.Text("🛏️"), 0xFF123456,
            timeHint = ActivityChipState.TimeHint.Duration(
                36500,
            ),
            hint = "Last",
        ),
    )
}

@Preview(device = WearDevices.LARGE_ROUND)
@Composable
private fun WithHintTags() {
    ActivityChip(
        ActivityChipState(
            0, "Sleeping", WearActivityIcon.Text("🛏️"), 0xFF123456,
            timeHint = ActivityChipState.TimeHint.Duration(
                36500,
            ),
            tagString = "Work, Hotel",
            hint = "Last",
        ),
    )
}

@Preview(device = WearDevices.LARGE_ROUND, fontScale = 2f)
@Composable
private fun WithHintTagsFontScale() {
    ActivityChip(
        ActivityChipState(
            0, "Sleeping", WearActivityIcon.Text("🛏️"), 0xFF123456,
            timeHint = ActivityChipState.TimeHint.Duration(
                36500,
            ),
            tagString = "Work, Hotel",
            hint = "Last",
        ),
    )
}