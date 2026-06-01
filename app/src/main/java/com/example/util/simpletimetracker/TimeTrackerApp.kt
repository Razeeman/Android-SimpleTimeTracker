package com.example.util.simpletimetracker

import android.app.Application
import android.os.StrictMode
import androidx.emoji2.bundled.BundledEmojiCompatConfig
import androidx.emoji2.text.EmojiCompat
import com.example.util.simpletimetracker.api.WebApiAdapter
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import timber.log.Timber
import timber.log.Timber.DebugTree
import javax.inject.Inject

@HiltAndroidApp
class TimeTrackerApp : Application() {

    @Inject
    lateinit var webApiAdapter: WebApiAdapter

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        initLog()
        initLibraries()
        initStrictMode()
        startWebApi()
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
                    .build(),
            )
            StrictMode.setVmPolicy(
                StrictMode.VmPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .build(),
            )
        }
    }

    private fun startWebApi() {
        applicationScope.launch {
            try {
                webApiAdapter.start()
                Timber.d("Web API started on port 8080")
            } catch (e: Exception) {
                Timber.e(e, "Failed to start Web API")
            }
        }
    }
}