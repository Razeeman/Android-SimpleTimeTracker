package com.example.util.simpletimetracker

import android.app.Application
import android.os.StrictMode
import androidx.emoji.bundled.BundledEmojiCompatConfig
import androidx.emoji.text.EmojiCompat
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
import timber.log.Timber.DebugTree

@HiltAndroidApp
class TimeTrackerApp : Application() {

    override fun onCreate() {
        super.onCreate()
        initLog()
        initLibraries()
        initStrictMode()
    }

    private fun initLog() {
        if (BuildConfig.DEBUG) {
            Timber.plant(DebugTree())
        }
    }

    private fun initLibraries() {
        val config = BundledEmojiCompatConfig(applicationContext)
            .setReplaceAll(true)
        EmojiCompat.init(config)
    }

    private fun initStrictMode() {
        if (BuildConfig.DEBUG) {
            StrictMode.setThreadPolicy(
                StrictMode.ThreadPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .penaltyDialog()
                    .build()
            )
            StrictMode.setVmPolicy(
                StrictMode.VmPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .build()
            )
        }
    }
}