/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.example.util.simpletimetracker.presentation

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.ChipDefaults
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.OutlinedButton
import androidx.wear.compose.material.PositionIndicator
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.ScalingLazyColumn
import androidx.wear.compose.material.ScalingLazyListState
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TimeText
import androidx.wear.compose.material.Vignette
import androidx.wear.compose.material.VignettePosition
import androidx.wear.compose.material.rememberScalingLazyListState
import androidx.wear.compose.material.scrollAway
import com.example.util.simpletimetracker.presentation.theme.SimpleTimeTrackerForWearOSTheme
import com.example.util.simpletimetracker.wearrpc.Activity
import com.example.util.simpletimetracker.wearrpc.ContextMessenger
import com.example.util.simpletimetracker.wearrpc.WearRPCClient
import com.google.android.horologist.compose.focus.rememberActiveFocusRequester
import com.google.android.horologist.compose.navscaffold.ExperimentalHorologistComposeLayoutApi
import com.google.android.horologist.compose.rotaryinput.rememberRotaryHapticFeedback
import com.google.android.horologist.compose.rotaryinput.rotaryWithScroll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WearApp()
        }
    }
}

@Composable
fun WearApp() {
    SimpleTimeTrackerForWearOSTheme {
        val scrollState = rememberScalingLazyListState()
        Scaffold(
            timeText = {
                TimeText(modifier = Modifier.scrollAway(scrollState))
            },
            vignette = {
                Vignette(vignettePosition = VignettePosition.TopAndBottom)
            },
            positionIndicator = {
                PositionIndicator(scalingLazyListState = scrollState)
            },
        ) {
            ActivityList(scrollState)
        }
    }
}


@OptIn(ExperimentalHorologistComposeLayoutApi::class)
@Composable
fun ActivityList(scrollState: ScalingLazyListState = rememberScalingLazyListState()) {
    val context = LocalContext.current
    var activities: Array<Activity> by remember { mutableStateOf(arrayOf()) }
    var queryCount by remember { mutableIntStateOf(0) }
    LaunchedEffect(
        key1 = queryCount,
        block = {
            async(Dispatchers.Default) {
                activities = WearRPCClient(ContextMessenger(context)).queryActivities()
            }
        },
    )

    val focusRequester = rememberActiveFocusRequester()
    val rotaryHapticFeedback = rememberRotaryHapticFeedback()
    ScalingLazyColumn(
        modifier = Modifier
            .rotaryWithScroll(focusRequester, scrollState, rotaryHapticFeedback)
            .fillMaxSize()
            .background(MaterialTheme.colors.background)
            .selectableGroup(),
        contentPadding = PaddingValues(10.dp),
        verticalArrangement = Arrangement.Center,
        state = scrollState,
    ) {
        for (activity in activities) {
            item {
                Activity(
                    id = activity.id,
                    name = activity.name,
                    color = Color(android.graphics.Color.parseColor(activity.color)),
                    icon = activity.icon,
                )
            }
        }
        item {
            OutlinedButton(
                onClick = {
                    queryCount++
                    Toast.makeText(context, "Refreshing activities", Toast.LENGTH_SHORT).show()
                },
                content = {
                    Icon(Icons.Rounded.Refresh, contentDescription = "Refresh Activities List")
                },
                modifier = Modifier.padding(all = 8.dp),
            )
        }
    }
}

@Composable
fun Activity(
    id: Long,
    name: String,
    color: Color = Color(96, 125, 139, 255),
    icon: String = "?",
) {
    val context = LocalContext.current
    val briefIcon = if (icon.startsWith("ic_")) {
        "?"
    } else {
        icon.substring(0, icon.length.coerceAtMost(2))
    }
    Chip(
        modifier = Modifier
            .fillMaxWidth(0.9f)
            .padding(top = 10.dp),
        label = {
            Text(
                text = "$briefIcon : $name",
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
        colors = ChipDefaults.chipColors(
            backgroundColor = color,
        ),
        onClick = {
            Toast.makeText(
                context,
                "Starting `$name` (id: $id)\n[Not Yet Implemented]", Toast.LENGTH_SHORT,
            ).show()
        },
    )
}

