/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.example.util.simpletimetracker.navigation

import androidx.compose.runtime.Composable
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import com.example.util.simpletimetracker.presentation.screens.activities.ActivitiesScreen
import com.example.util.simpletimetracker.presentation.screens.settings.SettingsScreen
import com.example.util.simpletimetracker.presentation.screens.tagsSelection.TagsScreen

object Route {
    const val ACTIVITIES = "activities"
    const val TAGS = "activities/{id}/tags"
    const val SETTINGS = "settings"
}

@Composable
fun WearNavigator() {
    val navigation = rememberSwipeDismissableNavController()
    SwipeDismissableNavHost(
        navController = navigation,
        startDestination = Route.ACTIVITIES,
    ) {
        composable(Route.ACTIVITIES) {
            ActivitiesScreen(
                onRequestTagSelection = {
                    val route = Route.TAGS.replace("{id}", it.toString())
                    navigation.navigate(route)
                },
                onSettingsClick = {
                    navigation.navigate(Route.SETTINGS)
                },
            )
        }
        composable(Route.TAGS) {
            val activityId = it.arguments
                ?.getString("id")
                ?.toLong()
                ?: return@composable

            TagsScreen(
                activityId = activityId,
                onComplete = {
                    navigation.popBackStack()
                },
            )
        }
        composable(Route.SETTINGS) {
            SettingsScreen()
        }
    }
}
