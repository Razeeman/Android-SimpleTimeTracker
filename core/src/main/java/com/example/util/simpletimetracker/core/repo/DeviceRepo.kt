package com.example.util.simpletimetracker.core.repo

import android.content.Context
import com.example.util.simpletimetracker.core.extension.pxToDp
import com.example.util.simpletimetracker.domain.di.AppContext
import javax.inject.Inject

class DeviceRepo @Inject constructor(
    @AppContext private val context: Context
) {

    fun getScreenWidthInDp(): Int {
        return context.resources.displayMetrics.widthPixels.pxToDp()
    }
}