/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.example.util.simpletimetracker.domain.model

data class WearRecord(
    val id: Long,
    val type: Type,
    val activityId: Long?,
    val activityName: String?,
    val activityIcon: String?,
    val activityColor: Long?,
    val startedAt: Long,
    val endedAt: Long,
    val tags: List<WearCurrentActivity.Tag>,
) {
    enum class Type { Tracked, Running, Untracked }
}
