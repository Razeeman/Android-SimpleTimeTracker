package com.example.util.simpletimetracker.core.repo

import android.content.Context
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import com.example.util.simpletimetracker.domain.di.AppContext
import javax.inject.Inject

class ResourceRepo @Inject constructor(
    @AppContext private val context: Context
) {

    fun getColor(@ColorRes colorResId: Int): Int {
        return ContextCompat.getColor(context, colorResId)
    }
}