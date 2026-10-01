/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.example.util.simpletimetracker.presentation.datePicker

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

internal fun LocalDate.toStartOfDayTimestamp(
    zoneId: ZoneId = ZoneId.systemDefault(),
): Long {
    return atStartOfDay(zoneId).toInstant().toEpochMilli()
}

internal fun Long.toLocalDate(
    zoneId: ZoneId = ZoneId.systemDefault(),
): LocalDate {
    return Instant.ofEpochMilli(this).atZone(zoneId).toLocalDate()
}
