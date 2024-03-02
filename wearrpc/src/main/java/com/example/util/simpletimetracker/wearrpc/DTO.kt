/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.example.util.simpletimetracker.wearrpc

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Data Transfer Objects
 *
 * Object definitions for records sent between Wear/Mobile
 */

@Parcelize
data class Activity(
    val id: Long,
    val name: String,
    val icon: String,
    val color: Long,
): Parcelable

@Parcelize
data class CurrentActivity(
    val id: Long,
    val startedAt: Long,
    val tags: List<Tag>,
): Parcelable

@Parcelize
data class Tag(
    val id: Long,
    val name: String,
    val isGeneral: Boolean,
    val color: Long,
): Parcelable

@Parcelize
data class Settings(
    val allowMultitasking: Boolean,
    val showRecordTagSelection: Boolean,
    val recordTagSelectionCloseAfterOne: Boolean,
    val recordTagSelectionEvenForGeneralTags: Boolean,
): Parcelable