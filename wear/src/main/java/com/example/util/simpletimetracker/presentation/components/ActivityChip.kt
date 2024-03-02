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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.SplitToggleChip
import androidx.wear.compose.material.Switch
import androidx.wear.compose.material.SwitchDefaults
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.ToggleChipDefaults
import androidx.wear.tooling.preview.devices.WearDevices
import com.example.util.simpletimetracker.presentation.remember.rememberDurationSince
import com.example.util.simpletimetracker.wear_api.Activity
import com.example.util.simpletimetracker.wear_api.Tag
import java.time.Duration
import java.time.Instant

private const val ISO_HOURS_MINUTES_PARTS_REGEX = "(\\d[HM])(?!$)"
private const val DECIMAL_SEPARATOR_AND_FRACTIONAL_PART_REGEX = "\\.\\d+"
private const val ISO_MISSING_MINUTES_REGEX = "(\\d+H) (\\d+S)"

@Composable
fun ActivityChip(
    activity: Activity,
    startedAt: Long? = null,
    tags: List<Tag> = emptyList(),
    onClick: () -> Unit = {},
    onToggleOn: () -> Unit = {},
    onToggleOff: () -> Unit = {},
) {
    val tagsList = if (tags.isNotEmpty()) {
        tags.joinToString(", ") { it.name }
    } else {
        ""
    }
    val tagString = if (tagsList.isNotEmpty()) {
        " - $tagsList"
    } else {
        ""
    }
    val switchChecked = startedAt != null
    SplitToggleChip(
        modifier = Modifier
            .fillMaxWidth(0.9f)
            .padding(top = 10.dp),
        label = {
            Row(modifier = Modifier.height(IntrinsicSize.Min)) {
                ActivityIcon(activityIcon = activity.icon)
                Text(
                    text = activity.name,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(start = 4.dp),
                )
            }
        },
        secondaryLabel = {
            if (startedAt != null) {
                var startedDiff = rememberDurationSince(epochMillis = startedAt)

                Text(
                    text = durationToLabel(startedDiff) + tagString,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = Color.White,
                    fontSize = 10.sp,
                    modifier = Modifier.padding(
                        start = if (tagString.isNotEmpty()) {
                            2.dp
                        } else {
                            22.dp
                        },
                    ),
                )
            } else {
                null
            }
        },
        colors = ToggleChipDefaults.splitToggleChipColors(
            backgroundColor = Color(activity.color),
            splitBackgroundOverlayColor = if (switchChecked) {
                Color.White.copy(alpha = .1F)
            } else {
                Color.Black.copy(alpha = .3F)
            },
        ),
        onCheckedChange = {
            if (it) {
                onToggleOn()
            } else {
                onToggleOff()
            }
        },
        checked = switchChecked,
        onClick = onClick,
        toggleControl = {
            Switch(
                checked = switchChecked,
                enabled = true,
                modifier = Modifier.semantics {
                    this.contentDescription = if (switchChecked) "On" else "Off"
                },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = Color.White.copy(alpha = 0.5F),
                    uncheckedThumbColor = Color.White,
                    uncheckedTrackColor = Color.White.copy(alpha = 0.5F),
                ),
            )
        },
    )
}

fun durationToLabel(duration: Duration): String {
    return duration.toString()
        .substring(2) // remove "PT" at the beginning of the string representation
        .replace(ISO_HOURS_MINUTES_PARTS_REGEX.toRegex(), "$1 ")
        .replace(DECIMAL_SEPARATOR_AND_FRACTIONAL_PART_REGEX.toRegex(), "")
        .replace(ISO_MISSING_MINUTES_REGEX.toRegex(), "$1 0M $2").lowercase()
}

@Preview(device = WearDevices.LARGE_ROUND)
@Composable
fun SampleCooking() {
    ActivityChip(Activity(123, "Cooking", "🎉", 0xFF123456))
}

@Preview(device = WearDevices.LARGE_ROUND)
@Composable
fun SampleSleep() {
    ActivityChip(Activity(456, "Sleeping", "🛏️", 0xFFABCDEF))
}

@Preview(device = WearDevices.LARGE_ROUND)
@Composable
fun SampleText() {
    ActivityChip(Activity(456, "Sleeping", "Zzzz", 0xFFABCDEF))
}

@Preview(device = WearDevices.LARGE_ROUND)
@Composable
fun SampleIcon() {
    ActivityChip(Activity(456, "Sleeping", "ic_hotel_24px", 0xFFABCDEF))
}

@Preview(device = WearDevices.LARGE_ROUND)
@Composable
fun InvalidIcon() {
    ActivityChip(Activity(456, "Sleeping", "ic_gobbldeegoock_24px", 0xFFABCDEF))
}

@Preview(device = WearDevices.LARGE_ROUND)
@Composable
fun White() {
    // TODO handle the look of light colored chips
    // Note: A white color is only possible when using the RGB color picker.
    // The default color options in the phone app are mostly darker shades.
    ActivityChip(Activity(456, "Sleeping", "🛏️", 0xFFFFFFFF))
}

@Preview(device = WearDevices.LARGE_ROUND)
@Composable
fun CurrentlyRunning() {
    ActivityChip(
        Activity(456, "Sleeping", "🛏️", 0xFFABCDEF),
        startedAt = Instant.now().toEpochMilli() - 360000,
    )
}

@Preview(device = WearDevices.LARGE_ROUND)
@Composable
fun CurrentlyRunningWithTags() {
    ActivityChip(
        Activity(456, "Sleeping", "🛏️", 0xFFABCDEF),
        startedAt = Instant.now().toEpochMilli() - 360000,
        tags = listOf(
            Tag(id = 2, name = "Work", isGeneral = true, color = 0xFFFFAA22),
            Tag(id = 4, name = "Hotel", isGeneral = false, color = 0xFFABCDEF),
        ),
    )
}