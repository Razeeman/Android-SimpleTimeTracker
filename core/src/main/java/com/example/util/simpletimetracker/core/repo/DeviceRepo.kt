package com.example.util.simpletimetracker.core.repo

import android.content.Context
import com.example.util.simpletimetracker.core.extension.pxToDp
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class DeviceRepo @Inject constructor(
    @ApplicationContext private val context: Context
) {

    fun getScreenWidthInDp(): Int {
        return context.resources.displayMetrics.widthPixels.pxToDp()
    }
}