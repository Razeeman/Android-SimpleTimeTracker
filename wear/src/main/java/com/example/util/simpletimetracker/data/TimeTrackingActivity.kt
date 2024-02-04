/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.example.util.simpletimetracker.data

import androidx.compose.ui.graphics.Color
import com.example.util.simpletimetracker.R

class TimeTrackingActivity(
    val name: String,
    val tags: List<String>,
    val color: Color,
    val iconId: Int,
)

fun getTimeTrackingActivities(): List<TimeTrackingActivity> {
    return listOf<TimeTrackingActivity>(
        activity("TODOs", listOf<String>("Chores", "Shopping")),
        activity("Travel", listOf<String>("Bike", "Car", "Plane", "Scooter", "Public Transit")),
        activity("Planning", listOf<String>("Goals", "Processing", "Reflection")),
        activity("Personal Learning", listOf<String>("Industry", "Hobbies", "Music")),
        activity("Professional Development", listOf<String>("Engineering", "Networking", "Trainings")),
        activity("Basic Needs", listOf<String>("Exercise", "Hygiene", "Meals", "Sleep")),
        activity("Church", listOf<String>("Service", "Study", "Worship", "Volunteer")),
        activity("Activities", listOf<String>("Family", "Friends", "Personal")),
        activity("Dates"),
        activity("Other"),
    )
}

fun activity(
    name: String,
    tags: List<String> = listOf(),
): TimeTrackingActivity {
    return TimeTrackingActivity(
        name = name,
        tags = tags,
        color = colorForActivity(name),
        iconId = iconForActivity(name),
    )
}

fun iconForActivity(activityName: String): Int {
    val icons =
        mapOf<String, Int>(
            "Activities" to R.drawable.baseline_people_24,
            "Basic Needs" to R.drawable.baseline_bed_24,
            "Church" to R.drawable.baseline_church_24,
            "Dates" to R.drawable.baseline_heart_broken_24,
            "Other" to R.drawable.baseline_horizontal_rule_24,
            "Personal Learning" to R.drawable.baseline_computer_24,
            "Planning" to R.drawable.baseline_timer_24,
            "Professional Development" to R.drawable.baseline_home_work_24,
            "TODOs" to R.drawable.baseline_check_box_24,
            "Travel" to R.drawable.baseline_directions_car_24,
        )
    return icons.getOrDefault(activityName, R.drawable.baseline_question_mark_24)
}

fun colorForActivity(activityName: String): Color {
    val colors =
        mapOf<String, Color>(
            "Activities" to Color(3, 169, 244, 255),
            "Basic Needs" to Color(205, 220, 57, 255),
            "Church" to Color(76, 175, 80, 255),
            "Dates" to Color(156, 39, 176, 255),
            "Other" to Color(96, 125, 139, 255),
            "Personal Learning" to Color(255, 193, 7, 255),
            "Planning" to Color(255, 87, 34, 255),
            "Professional Development" to Color(233, 30, 99, 255),
            "TODOs" to Color(245, 54, 57, 255),
            "Travel" to Color(120, 82, 72, 255),
        )
    return colors.getOrDefault(activityName, Color(96, 125, 139, 255))
}
