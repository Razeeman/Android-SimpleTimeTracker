/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.example.util.simpletimetracker.domain.model

data class WearRecordRepeatResult(
    val result: ActionResult,
) {

    sealed interface ActionResult {
        object Started : ActionResult
        object NoPreviousFound : ActionResult
        object AlreadyTracking : ActionResult
    }
}