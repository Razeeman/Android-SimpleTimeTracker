/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.example.util.simpletimetracker.domain.model

data class WearStatistics(
    val id: Long,
    val type: Type,
    val name: String?,
    val icon: String?,
    val color: Long?,
    val duration: Long,
) {
    enum class Type { Activity, Category, Tag, Untracked, Uncategorized, Untagged }
}
