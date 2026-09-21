/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.example.util.simpletimetracker.navigation

import androidx.compose.runtime.Composable
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import com.example.util.simpletimetracker.features.activities.screen.ActivitiesScreen
import com.example.util.simpletimetracker.features.settings.screen.SettingsScreen
import com.example.util.simpletimetracker.features.statistics.screen.StatisticsScreen
import com.example.util.simpletimetracker.features.tagValueSelection.screen.TagValueSelectionScreen
import com.example.util.simpletimetracker.features.tagsSelection.screen.TagsScreen
import com.example.util.simpletimetracker.presentation.datePicker.WearDatePicker
import com.example.util.simpletimetracker.presentation.datePicker.toLocalDate
import com.example.util.simpletimetracker.presentation.dialog.MessageDialog
import com.example.util.simpletimetracker.utils.getString

@Composable
fun WearNavigator() {
    val navigation = rememberSwipeDismissableNavController()
    SwipeDismissableNavHost(
        navController = navigation,
        startDestination = WearNavigationRoute.Activities.baseRoute,
    ) {
        composable(WearNavigationRoute.Activities) { _, _ ->
            ActivitiesScreen(
                onRequestTagSelection = {
                    navigation.navigate(WearNavigationRoute.Tags, it)
                },
                onStatisticsClick = {
                    navigation.navigate(WearNavigationRoute.Statistics)
                },
                onSettingsClick = {
                    navigation.navigate(WearNavigationRoute.Settings)
                },
                onShowMessage = {
                    navigation.navigate(WearNavigationRoute.Alert, it)
                },
            )
        }
        composable(WearNavigationRoute.Tags) { route, arguments ->
            val activityId = route.get(arguments) ?: return@composable

            TagsScreen(
                activityId = activityId,
                onRequestTagValueSelection = {
                    navigation.navigate(WearNavigationRoute.TagValue, it)
                },
                onComplete = {
                    navigation.popBackStack()
                },
            )
        }
        composable(WearNavigationRoute.TagValue) { route, arguments ->
            val tagId = route.get(arguments) ?: return@composable

            TagValueSelectionScreen(
                tagId = tagId,
                onComplete = {
                    navigation.popBackStack()
                },
            )
        }
        composable(WearNavigationRoute.Statistics) { _, _ ->
            StatisticsScreen(
                onOpenDatePicker = {
                    navigation.navigate(WearNavigationRoute.DatePicker, it)
                },
            )
        }
        composable(WearNavigationRoute.Settings) { _, _ ->
            SettingsScreen()
        }
        composable(WearNavigationRoute.Alert) { route, arguments ->
            val textResId = route.get(arguments) ?: return@composable

            MessageDialog(
                message = getString(textResId),
                onDismiss = {
                    navigation.popBackStack()
                },
            )
        }
        composable(WearNavigationRoute.DatePicker) { route, arguments ->
            val timestamp = route.get(arguments) ?: return@composable
            val date = timestamp.toLocalDate()

            WearDatePicker(
                date = date,
                onComplete = {
                    navigation.popBackStack()
                },
            )
        }
    }
}
