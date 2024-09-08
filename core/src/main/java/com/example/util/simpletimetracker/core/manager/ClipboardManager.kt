package com.example.util.simpletimetracker.core.manager

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.core.content.getSystemService
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class ClipboardManager @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    private val manager get() = context.getSystemService<ClipboardManager>()

    fun send(text: String) {
        val clip = ClipData.newPlainText(text, text)
        manager?.setPrimaryClip(clip)
    }
}