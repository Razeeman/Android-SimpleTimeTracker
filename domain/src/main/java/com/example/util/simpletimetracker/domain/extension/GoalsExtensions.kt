package com.example.util.simpletimetracker.domain.extension

import com.example.util.simpletimetracker.domain.model.RecordTypeGoal
import com.example.util.simpletimetracker.domain.model.RecordTypeGoal.Range
import com.example.util.simpletimetracker.domain.model.RecordTypeGoal.Type

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

fun List<RecordTypeGoal>.hasSessionDuration(): Boolean {
    return getSessionDuration() != null
}

fun List<RecordTypeGoal>.hasDailyDuration(): Boolean {
    return getDailyDuration() != null
}

fun List<RecordTypeGoal>.hasWeeklyDuration(): Boolean {
    return getWeeklyDuration() != null
}

fun List<RecordTypeGoal>.hasMonthlyDuration(): Boolean {
    return getMonthlyDuration() != null
}

val RecordTypeGoal?.value: Long get() = this?.type?.value.orZero()
