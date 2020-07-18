package com.example.util.simpletimetracker.core.provider

import android.content.Context
import com.example.util.simpletimetracker.domain.di.AppContext
import javax.inject.Inject

class PackageNameProvider @Inject constructor(
    @AppContext private val context: Context
) {

    fun getPackageName(): String {
        return context.packageName
    }
}