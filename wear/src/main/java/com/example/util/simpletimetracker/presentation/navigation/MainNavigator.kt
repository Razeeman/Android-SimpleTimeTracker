/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.example.util.simpletimetracker.presentation.navigation

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import com.example.util.simpletimetracker.presentation.screens.ActivitiesScreen
import com.example.util.simpletimetracker.presentation.screens.TagsScreen

@Composable
fun MainNavigator() {
    val navController = rememberSwipeDismissableNavController()
    SwipeDismissableNavHost(
        navController = navController,
        startDestination = "activities"
    ) {
        composable("activities") {
            ActivitiesScreen(onSelectActivity = { id ->
                navController.navigate("activities/$id/tags")
            })
        }
        composable("activities/{id}/tags") {
            TagsScreen(activityId = it.arguments?.getString("id")?.toLong()!!, onSelectTag = {
                navController.navigate("activities")
            })
        }
    }
}