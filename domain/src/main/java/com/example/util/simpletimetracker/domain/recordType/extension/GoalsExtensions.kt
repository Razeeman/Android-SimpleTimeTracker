package com.example.util.simpletimetracker.domain.recordType.extension

import com.example.util.simpletimetracker.domain.extension.orZero
import com.example.util.simpletimetracker.domain.daysOfWeek.model.DayOfWeek
import com.example.util.simpletimetracker.domain.recordType.model.RecordTypeGoal
import com.example.util.simpletimetracker.domain.recordType.model.RecordTypeGoal.Range
import com.example.util.simpletimetracker.domain.recordType.model.RecordTypeGoal.Type
import com.example.util.simpletimetracker.domain.statistics.model.RangeLength

fun Range.toRangeLength(): RangeLength? {
    return when (this) {
        is Range.Session -> return null
        is Range.Daily -> RangeLength.Day
        is Range.Weekly -> RangeLength.Week
        is Range.Monthly -> RangeLength.Month
    }
}

fun List<RecordTypeGoal>.getSession(): RecordTypeGoal? {
    return firstOrNull { it.range is Range.Session }
}

fun List<RecordTypeGoal>.getDaily(): RecordTypeGoal? {
    return firstOrNull { it.range is Range.Daily }
}

fun List<RecordTypeGoal>.getWeekly(): RecordTypeGoal? {
    return firstOrNull { it.range is Range.Weekly }
}

fun List<RecordTypeGoal>.getMonthly(): RecordTypeGoal? {
    return firstOrNull { it.range is Range.Monthly }
}

fun List<RecordTypeGoal>.getSessionDuration(): RecordTypeGoal? {
    return firstOrNull {
        it.range is Range.Session && it.type is Type.Duration
    }
}

fun List<RecordTypeGoal>.getDailyDuration(): RecordTypeGoal? {
    return firstOrNull {
        it.range is Range.Daily && it.type is Type.Duration
    }
}

fun List<RecordTypeGoal>.getWeeklyDuration(): RecordTypeGoal? {
    return firstOrNull {
        it.range is Range.Weekly && it.type is Type.Duration
    }
}

fun List<RecordTypeGoal>.getMonthlyDuration(): RecordTypeGoal? {
    return firstOrNull {
        it.range is Range.Monthly && it.type is Type.Duration
    }
}

fun List<RecordTypeGoal>.getSessionCount(): RecordTypeGoal? {
    return firstOrNull {
        it.range is Range.Session && it.type is Type.Count
    }
}

fun List<RecordTypeGoal>.getDailyCount(): RecordTypeGoal? {
    return firstOrNull {
        it.range is Range.Daily && it.type is Type.Count
    }
}

fun List<RecordTypeGoal>.getWeeklyCount(): RecordTypeGoal? {
    return firstOrNull {
        it.range is Range.Weekly && it.type is Type.Count
    }
}

fun List<RecordTypeGoal>.getMonthlyCount(): RecordTypeGoal? {
    return firstOrNull {
        it.range is Range.Monthly && it.type is Type.Count
    }
}

fun List<RecordTypeGoal>.hasDailyDuration(): Boolean {
    return getDailyDuration() != null
}

fun List<RecordTypeGoal>.filterDaysOfWeek(dayOfWeek: DayOfWeek): List<RecordTypeGoal> {
    return filter {
        if (it.range is Range.Daily) dayOfWeek in it.daysOfWeek else true
    }
}

val RecordTypeGoal?.value: Long get() = this?.type?.value.orZero()
