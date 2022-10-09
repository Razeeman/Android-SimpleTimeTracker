package com.example.util.simpletimetracker.core.utils

import android.app.PendingIntent
import android.os.Build

object PendingIntents {

    fun getFlags(): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
    }
}