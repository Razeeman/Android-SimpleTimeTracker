/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.example.util.simpletimetracker.complication

import android.content.ComponentName
import android.content.Context
import androidx.wear.watchface.complications.datasource.ComplicationDataSourceUpdateRequester
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class WearComplicationManager @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    fun updateComplications() {
        ComplicationDataSourceUpdateRequester.create(
            context = context,
            complicationDataSourceComponent = ComponentName(
                context,
                WearComplicationService::class.java,
            ),
        ).requestUpdateAll()
    }
}