package com.example.util.simpletimetracker.core.extension

import android.os.StrictMode

inline fun <T, R> T.allowDiskRead(block: T.() -> R): R {
    val oldPolicy = StrictMode.allowThreadDiskReads()
    try {
        return block()
    } finally {
        StrictMode.setThreadPolicy(oldPolicy)
    }
}