/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.example.util.simpletimetracker.presentation.remember

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue

@Composable
fun rememberAnimationRotation(key: Int): Float {
    val targetRotation by animateFloatAsState(
        targetValue = key * 360F,
        animationSpec = tween(1500),
        label = "rotation",
    )
    return targetRotation
}