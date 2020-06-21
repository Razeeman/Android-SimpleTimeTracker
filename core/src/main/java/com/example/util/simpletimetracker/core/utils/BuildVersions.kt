package com.example.util.simpletimetracker.core.utils

import android.os.Build

object BuildVersions {

    fun isLollipopOrHigher(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
    }
}