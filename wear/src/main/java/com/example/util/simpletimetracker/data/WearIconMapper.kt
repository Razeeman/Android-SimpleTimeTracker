/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.example.util.simpletimetracker.data

import android.content.Context
import com.example.util.simpletimetracker.domain.model.WearActivityIcon
import com.example.util.simpletimetracker.resources.CommonActivityIcon
import com.example.util.simpletimetracker.resources.IconMapperUtils
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WearIconMapper @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    fun mapIcon(icon: String): WearActivityIcon {
        return IconMapperUtils.mapIcon(context, icon).let(::map)
    }

    private fun map(icon: CommonActivityIcon): WearActivityIcon {
        return when (icon) {
            is CommonActivityIcon.Image -> WearActivityIcon.Image(icon.iconId)
            is CommonActivityIcon.Text -> WearActivityIcon.Text(icon.text)
        }
    }
}