/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.example.util.simpletimetracker.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.util.simpletimetracker.navigation.WearActionResolver
import com.example.util.simpletimetracker.navigation.WearNavigator
import com.example.util.simpletimetracker.presentation.theme.WearTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var wearActionResolver: WearActionResolver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        wearActionResolver.registerResultListeners(this)

        setContent {
            WearTheme {
                WearNavigator()
            }
        }
    }
}
