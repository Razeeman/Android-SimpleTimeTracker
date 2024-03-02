/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.example.util.simpletimetracker.presentation.navigation

import androidx.navigation.NavHostController

fun NavHostController.navigateToRoot(route: String) {
    // Inspired by: https://stackoverflow.com/a/72856761/14765128
    navigate(route) {
        popUpTo(graph.startDestinationId) { inclusive = true }
        graph.setStartDestination(route)
    }
}