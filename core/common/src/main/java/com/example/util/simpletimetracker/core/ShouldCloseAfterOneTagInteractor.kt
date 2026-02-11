package com.example.util.simpletimetracker.core

import javax.inject.Inject

class ShouldCloseAfterOneTagInteractor @Inject constructor() {

    fun execute(
        typeId: Long,
        closeAfterOne: Boolean,
        excludedActivities: Set<Long>,
    ): Boolean {
        if (!closeAfterOne) return false
        return typeId !in excludedActivities
    }
}