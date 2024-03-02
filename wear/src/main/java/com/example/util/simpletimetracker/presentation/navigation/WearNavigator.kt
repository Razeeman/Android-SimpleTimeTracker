/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.example.util.simpletimetracker.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import com.example.util.simpletimetracker.presentation.screens.ActivitiesScreen
import com.example.util.simpletimetracker.presentation.screens.TagsScreen

object Route {
    const val Activities = "activities"
    const val Tags = "activities/{id}/tags"
}

@Composable
fun WearNavigator() {
    val navigation = rememberSwipeDismissableNavController()
    SwipeDismissableNavHost(
        navController = navigation,
        startDestination = Route.Activities,
    ) {
        composable(Route.Activities) {
            ActivitiesScreen(
                onRequestTagSelection = {
                    val route = Route.Tags.replace("{id}", it.toString())
                    navigation.navigate(route)
                },
            )
        }
        composable(Route.Tags) {
            val activityId = it.arguments
                ?.getString("id")
                ?.toLong()
                ?: return@composable

            TagsScreen(
                activityId = activityId,
                onComplete = {
                    navigation.navigateToRoot(Route.Activities)
                },
            )
        }
    }
}
