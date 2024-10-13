/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.example.util.simpletimetracker.wear_api

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

/**
 * Data Transfer Objects
 *
 * Object definitions for records sent between Wear/Mobile
 */

@Parcelize
data class WearActivityDTO(
    @SerializedName("id")
    val id: Long,
    @SerializedName("name")
    val name: String,
    @SerializedName("icon")
    val icon: String,
    @SerializedName("color")
    val color: Long,
) : Parcelable

@Parcelize
data class WearCurrentActivityDTO(
    @SerializedName("id")
    val id: Long,
    @SerializedName("startedAt")
    val startedAt: Long,
    @SerializedName("tags")
    val tags: List<WearTagDTO>,
) : Parcelable

@Parcelize
data class WearTagDTO(
    @SerializedName("id")
    val id: Long,
    @SerializedName("name")
    val name: String,
    @SerializedName("color")
    val color: Long,
) : Parcelable

@Parcelize
data class WearSettingsDTO(
    @SerializedName("allowMultitasking")
    val allowMultitasking: Boolean,
    @SerializedName("recordTagSelectionCloseAfterOne")
    val recordTagSelectionCloseAfterOne: Boolean,
    @SerializedName("enableRepeatButton")
    val enableRepeatButton: Boolean,
) : Parcelable

@Parcelize
data class WearStartActivityRequest(
    @SerializedName("id")
    val id: Long,
    @SerializedName("tagIds")
    val tagIds: List<Long>,
) : Parcelable

@Parcelize
data class WearStopActivityRequest(
    @SerializedName("id")
    val id: Long,
) : Parcelable

@Parcelize
data class WearShouldShowTagSelectionRequest(
    @SerializedName("id")
    val id: Long,
) : Parcelable

@Parcelize
data class WearShouldShowTagSelectionResponse(
    @SerializedName("shouldShow")
    val shouldShow: Boolean,
) : Parcelable

@Parcelize
data class WearRecordRepeatResponse(
    @SerializedName("result")
    val result: ActionResult,
) : Parcelable {

    enum class ActionResult {
        STARTED,
        NO_PREVIOUS_FOUND,
        ALREADY_TRACKING,
    }
}
