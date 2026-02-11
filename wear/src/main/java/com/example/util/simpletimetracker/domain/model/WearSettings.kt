/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.example.util.simpletimetracker.domain.model

import com.example.util.simpletimetracker.domain.daysOfWeek.model.DayOfWeek

data class WearSettings(
    val apiVersion: String,
    val allowMultitasking: Boolean,
    val recordTagSelectionCloseAfterOne: Boolean,
    val closeAfterOneTagExcludeActivities: Set<Long>,
    val enableRepeatButton: Boolean,
    val retroactiveTrackingMode: Boolean,
    val startOfDayShift: Long,
    val firstDayOfWeek: DayOfWeek,
)