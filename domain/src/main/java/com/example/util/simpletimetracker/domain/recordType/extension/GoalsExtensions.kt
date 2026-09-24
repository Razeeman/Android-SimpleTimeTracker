package com.example.util.simpletimetracker.domain.recordType.extension

import com.example.util.simpletimetracker.domain.extension.orZero
import com.example.util.simpletimetracker.domain.daysOfWeek.model.DayOfWeek
import com.example.util.simpletimetracker.domain.recordType.model.RecordTypeGoal
import com.example.util.simpletimetracker.domain.recordType.model.RecordTypeGoal.Range
import com.example.util.simpletimetracker.domain.recordType.model.RecordTypeGoal.Type
import com.example.util.simpletimetracker.domain.statistics.model.RangeLength

fun Range.toRangeLength(): RangeLength? {
    return when (this) {
        is Range.Session -> null
        is Range.Daily -> RangeLength.Day
        is Range.Weekly -> RangeLength.Week
        is Range.Monthly -> RangeLength.Month
        is Range.Yearly -> RangeLength.Year
    }
}

fun List<RecordTypeGoal>.getDurations(): List<RecordTypeGoal> {
    return filter { it.type is Type.Duration }
}

fun List<RecordTypeGoal>.getCounts(): List<RecordTypeGoal> {
    return filter { it.type is Type.Count }
}

fun List<RecordTypeGoal>.getLongest(): RecordTypeGoal? {
    return getDurations().ifEmpty { getCounts() }.maxByOrNull { it.value }
}

fun List<RecordTypeGoal>.getSession(): List<RecordTypeGoal> {
    return filter { it.range is Range.Session }
}

fun List<RecordTypeGoal>.getDaily(): List<RecordTypeGoal> {
    return filter { it.range is Range.Daily }
}

fun List<RecordTypeGoal>.getWeekly(): List<RecordTypeGoal> {
    return filter { it.range is Range.Weekly }
}

fun List<RecordTypeGoal>.getMonthly(): List<RecordTypeGoal> {
    return filter { it.range is Range.Monthly }
}

fun List<RecordTypeGoal>.getYearly(): List<RecordTypeGoal> {
    return filter { it.range is Range.Yearly }
}

fun List<RecordTypeGoal>.filterDaysOfWeek(dayOfWeek: DayOfWeek): List<RecordTypeGoal> {
    return filter {
        if (it.range is Range.Daily) dayOfWeek in it.daysOfWeek else true
    }
}

fun RecordTypeGoal.Subtype.isReached(
    current: Long,
    goalValue: Long,
): Boolean {
    return when (this) {
        is RecordTypeGoal.Subtype.Goal -> current >= goalValue
        is RecordTypeGoal.Subtype.Limit -> current > goalValue
    }
}

fun RecordTypeGoal.Subtype.isSuccessful(
    current: Long,
    goalValue: Long,
): Boolean {
    val isReached = isReached(current = current, goalValue = goalValue)
    return when (this) {
        is RecordTypeGoal.Subtype.Goal -> isReached
        is RecordTypeGoal.Subtype.Limit -> !isReached
    }
}

val RecordTypeGoal?.value: Long get() = this?.type?.value.orZero()
