package com.example.util.simpletimetracker.core.provider

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class PackageNameProvider @Inject constructor(
    @ApplicationContext private val context: Context
) {

    fun getPackageName(): String {
        return context.packageName
    }
}