/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.example.util.simpletimetracker.wear_api

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Data Transfer Objects
 *
 * Object definitions for records sent between Wear/Mobile
 */

@Parcelize
data class WearActivity(
    val id: Long,
    val name: String,
    val icon: String,
    val color: Long,
) : Parcelable

@Parcelize
data class WearCurrentActivity(
    val id: Long,
    val startedAt: Long,
    val tags: List<WearTag>,
) : Parcelable

@Parcelize
data class WearTag(
    val id: Long,
    val name: String,
    val isGeneral: Boolean,
    val color: Long,
) : Parcelable

@Parcelize
data class WearSettings(
    val allowMultitasking: Boolean,
    val showRecordTagSelection: Boolean,
    val recordTagSelectionCloseAfterOne: Boolean,
    val recordTagSelectionExcludedActivities: List<Long>,
) : Parcelable