package com.example.util.simpletimetracker.domain.extension

import com.example.util.simpletimetracker.domain.record.model.Range
import com.example.util.simpletimetracker.domain.record.model.RecordBase

fun Range?.orEmpty(): Range = this ?: Range(0, 0)

fun RecordBase.toRange(): Range {
    return Range(timeStarted = timeStarted, timeEnded = timeEnded)
}