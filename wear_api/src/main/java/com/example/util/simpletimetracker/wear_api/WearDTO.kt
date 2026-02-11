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
data class WearCurrentStateDTO(
    @SerializedName("currentActivities")
    val currentActivities: List<WearCurrentActivityDTO>,
    @SerializedName("lastRecords")
    val lastRecords: List<WearLastRecordDTO>,
    @SerializedName("suggestionIds")
    val suggestionIds: List<Long>,
) : Parcelable

@Parcelize
data class WearCurrentActivityDTO(
    @SerializedName("id")
    val id: Long,
    @SerializedName("startedAt")
    val startedAt: Long,
    @SerializedName("tags")
    val tags: List<TagDTO>,
) : Parcelable {

    @Parcelize
    data class TagDTO(
        @SerializedName("name")
        val name: String,
        @SerializedName("numericValue")
        val numericValue: Double?,
        @SerializedName("valueSuffix")
        val valueSuffix: String?,
    ) : Parcelable
}

@Parcelize
data class WearStatisticsRequest(
    @SerializedName("shift")
    val shift: Int?,
    @SerializedName("filterType")
    val filterType: WearChartFilterTypeDTO?,
) : Parcelable

@Parcelize
data class WearStatisticsDTO(
    @SerializedName("id")
    val id: Long,
    @SerializedName("name")
    val name: String?,
    @SerializedName("icon")
    val icon: String?,
    @SerializedName("color")
    val color: Long?,
    @SerializedName("duration")
    val duration: Long,
) : Parcelable

@Parcelize
data class WearLastRecordDTO(
    @SerializedName("activityId")
    val activityId: Long,
    @SerializedName("startedAt")
    val startedAt: Long,
    @SerializedName("finishedAt")
    val finishedAt: Long,
    @SerializedName("tags")
    val tags: List<WearCurrentActivityDTO.TagDTO>,
) : Parcelable

@Parcelize
data class WearTagDTO(
    @SerializedName("id")
    val id: Long,
    @SerializedName("name")
    val name: String,
    @SerializedName("color")
    val color: Long,
    @SerializedName("preselected")
    val preselected: Boolean,
) : Parcelable

@Parcelize
data class WearSettingsDTO(
    @SerializedName("apiVersion")
    val apiVersion: String,
    @SerializedName("allowMultitasking")
    val allowMultitasking: Boolean?,
    @SerializedName("recordTagSelectionCloseAfterOne")
    val recordTagSelectionCloseAfterOne: Boolean?,
    @SerializedName("closeAfterOneTagExcludeActivities")
    val closeAfterOneTagExcludeActivities: List<Long>?,
    @SerializedName("enableRepeatButton")
    val enableRepeatButton: Boolean?,
    @SerializedName("retroactiveTrackingMode")
    val retroactiveTrackingMode: Boolean?,
    @SerializedName("startOfDayShift")
    val startOfDayShift: Long?,
    @SerializedName("firstDayOfWeek")
    val firstDayOfWeek: WearDayOfWeekDTO?,
) : Parcelable

@Parcelize
data class WearSetSettingsRequest(
    @SerializedName("allowMultitasking")
    val allowMultitasking: Boolean?,
) : Parcelable

@Parcelize
data class WearStartActivityRequest(
    @SerializedName("id")
    val id: Long?,
    @SerializedName("tags")
    val tags: List<Tag?>?,
) : Parcelable {

    @Parcelize
    data class Tag(
        @SerializedName("tagId")
        val tagId: Long?,
        @SerializedName("numericValue")
        val numericValue: Double?,
    ) : Parcelable
}

@Parcelize
data class WearStopActivityRequest(
    @SerializedName("id")
    val id: Long?,
) : Parcelable

@Parcelize
data class WearShouldShowTagSelectionRequest(
    @SerializedName("id")
    val id: Long?,
) : Parcelable

@Parcelize
data class WearShouldShowTagSelectionResponse(
    @SerializedName("shouldShow")
    val shouldShow: Boolean,
) : Parcelable

@Parcelize
data class WearShouldShowTagValueSelectionRequest(
    @SerializedName("selectedTagIds")
    val selectedTagIds: List<Long>?,
    @SerializedName("clickedTagId")
    val clickedTagId: Long?,
) : Parcelable

@Parcelize
data class WearShouldShowTagValueSelectionResponse(
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

enum class WearChartFilterTypeDTO {
    @SerializedName("ACTIVITY")
    ACTIVITY,

    @SerializedName("CATEGORY")
    CATEGORY,

    @SerializedName("RECORD_TAG")
    RECORD_TAG,
}

enum class WearDayOfWeekDTO {
    @SerializedName("SUNDAY")
    SUNDAY,

    @SerializedName("MONDAY")
    MONDAY,

    @SerializedName("TUESDAY")
    TUESDAY,

    @SerializedName("WEDNESDAY")
    WEDNESDAY,

    @SerializedName("THURSDAY")
    THURSDAY,

    @SerializedName("FRIDAY")
    FRIDAY,

    @SerializedName("SATURDAY")
    SATURDAY,
}
