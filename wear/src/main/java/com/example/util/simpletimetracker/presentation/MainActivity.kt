/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.example.util.simpletimetracker.presentation

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.*
import com.google.android.horologist.compose.focus.rememberActiveFocusRequester
import com.google.android.horologist.compose.navscaffold.ExperimentalHorologistComposeLayoutApi
import com.google.android.horologist.compose.rotaryinput.*
import com.example.util.simpletimetracker.R
import com.example.util.simpletimetracker.data.getTimeTrackingActivities
import com.example.util.simpletimetracker.presentation.theme.SimpleTimeTrackerForWearOSTheme
import com.example.util.simpletimetracker.wearrpc.Messenger
import com.example.util.simpletimetracker.wearrpc.WearRPCClient
//import com.example.util.simpletimetracker.wearrpc.CurrentActivity
import kotlinx.coroutines.*
//import java.util.Date

const val LOG_TAG = "com.razeeman.util.simpletimetracker"

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
    val context = LocalContext.current
    val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
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
            ScalingLazyColumn (
                modifier = Modifier.fillMaxSize(),
                autoCentering = AutoCenteringParams(itemIndex = 0)
            ){
                item {
                    DebugButton(
                        label = "Ping",
                        onClick = {
                            Log.i(LOG_TAG, "Pinging mobile ...")
                            scope.async {
                                val response = WearRPCClient(Messenger(context)).ping("Ping button")
                                Log.i(LOG_TAG, response)
                            }
                        }
                    )
                }
                item {
                    DebugButton(label="Query Activities", onClick = {
                        Log.i(LOG_TAG, "Querying activities ...")
                        scope.async {
                            val response = WearRPCClient(Messenger(context)).queryActivities()
                            Log.i(LOG_TAG, "Received ${response.size} activities")
                            for (activity in response) {
                                Log.i(LOG_TAG, " - ${activity.id}: ${activity.name}")
                            }
                        }
                    })
                }
            }

//            ActivityList(scrollState)
        }
    }
}

@Composable
fun DebugButton(label: String, onClick: () -> Unit) {
    Chip(
        modifier = Modifier
            .fillMaxWidth(0.9f)
            .padding(top = 10.dp),
        label = { Text(label) },
        onClick = onClick,
    )
}

@OptIn(ExperimentalHorologistComposeLayoutApi::class)
@Composable
fun ScrollingColumn(scrollState: ScalingLazyListState = rememberScalingLazyListState(), content: @Composable () -> Unit) {
    val focusRequester = rememberActiveFocusRequester()
    val rotaryHapticFeedback = rememberRotaryHapticFeedback()
    ScalingLazyColumn(
        modifier =
        Modifier
            .rotaryWithScroll(focusRequester, scrollState, rotaryHapticFeedback)
            .fillMaxSize()
            .background(MaterialTheme.colors.background)
            .selectableGroup(),
        contentPadding = PaddingValues(10.dp),
        verticalArrangement = Arrangement.Center,
        state = scrollState,
    ) {
        content
    }
}

@OptIn(ExperimentalHorologistComposeLayoutApi::class)
@Composable
fun ActivityList(scrollState: ScalingLazyListState = rememberScalingLazyListState()) {
    val activities = getTimeTrackingActivities()
    val focusRequester = rememberActiveFocusRequester()
    val rotaryHapticFeedback = rememberRotaryHapticFeedback()
    ScalingLazyColumn(
        modifier =
        Modifier
            .rotaryWithScroll(focusRequester, scrollState, rotaryHapticFeedback)
            .fillMaxSize()
            .background(MaterialTheme.colors.background)
            .selectableGroup(),
        contentPadding = PaddingValues(10.dp),
        verticalArrangement = Arrangement.Center,
        state = scrollState,
    ) {
        for (activity in activities) {
            if (activity.tags.isEmpty()) {
                item {
                    Activity(
                        name = activity.name,
                        tag = "",
                        color = activity.color,
                        icon = activity.iconId,
                    )
                }
            } else {
                for (tag in activity.tags) {
                    item {
                        Activity(
                            name = activity.name,
                            tag = tag,
                            color = activity.color,
                            icon = activity.iconId,
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun Activity(
    name: String,
    tag: String,
    color: Color = Color(96, 125, 139, 255),
    icon: Int = R.drawable.baseline_question_mark_24,
) {
    if (tag.isNotEmpty()) {
        ActivityWithTag(name = name, tag = tag, color = color, icon = icon)
    } else {
        ActivityWithoutTag(name = name, color = color, icon = icon)
    }
}

@Composable
fun ActivityWithTag(
    name: String,
    tag: String,
    color: Color = Color(96, 125, 139, 255),
    icon: Int = R.drawable.baseline_question_mark_24,
) {
    val context = LocalContext.current
    val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    Chip(
        modifier =
        Modifier
            .fillMaxWidth(0.9f)
            .padding(top = 10.dp),
        icon = {
            ActivityIcon(iconId = icon)
        },
        label = {
            Text(
                text = tag,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
        secondaryLabel = {
            Text(
                text = name,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
        colors =
            ChipDefaults.chipColors(
                backgroundColor = color,
            ),
        onClick = { startTimeTracking(scope, context) },
    )
}

@Composable
fun ActivityWithoutTag(
    name: String,
    color: Color = Color(96, 125, 139, 255),
    icon: Int = R.drawable.baseline_question_mark_24,
) {
    val context = LocalContext.current
    val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    Chip(
        modifier =
        Modifier
            .fillMaxWidth(0.9f)
            .padding(top = 10.dp),
        icon = {
            ActivityIcon(iconId = icon)
        },
        label = {
            Text(
                text = name,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
        colors =
            ChipDefaults.chipColors(
                backgroundColor = color,
            ),
        onClick = { startTimeTracking(scope, context) },
    )
}

@Composable
fun ActivityIcon(iconId: Int) {
    Icon(
        painter = painterResource(id = iconId),
        contentDescription = "activity icon",
        modifier =
        Modifier
            .size(ChipDefaults.IconSize)
            .wrapContentSize(align = Alignment.Center),
    )
}

fun startTimeTracking(scope: CoroutineScope, context: Context) {
//    val activities = arrayOf(
//        CurrentActivity(
//            id,
//            name,
//            startedAt = Date(),
//            arrayOf(/* tags */),
//        )
//    )
//    scope.async {
//        WearRPCClient(context).setCurrentActivities(activities)
//    }
}