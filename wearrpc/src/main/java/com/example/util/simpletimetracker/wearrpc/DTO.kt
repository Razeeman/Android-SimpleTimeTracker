/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.example.util.simpletimetracker.wearrpc

/**
 * Data Transfer Objects
 *
 * Object definitions for records sent between Wear/Mobile
 */

data class Activity(val id: Long, val name: String, val icon: String, val color: Long)

data class CurrentActivity(val id: Long, val startedAt: Long, val tags: Array<Tag>) {
    override fun equals(other: Any?): Boolean {
        // autogenerated
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CurrentActivity

        if (id != other.id) return false
        if (startedAt != other.startedAt) return false
        return tags.contentEquals(other.tags)
    }

    override fun hashCode(): Int {
        // autogenerated
        var result = id.toInt()
        result = 31 * result + startedAt.hashCode()
        result = 31 * result + tags.contentHashCode()
        return result
    }
}

data class Tag(val id: Long, val name: String, val isGeneral: Boolean, val color: Long)

data class Settings(
    val allowMultitasking: Boolean,
    val showRecordTagSelection: Boolean,
    val recordTagSelectionCloseAfterOne: Boolean,
    val recordTagSelectionEvenForGeneralTags: Boolean,
)