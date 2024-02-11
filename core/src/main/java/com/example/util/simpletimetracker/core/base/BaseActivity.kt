package com.example.util.simpletimetracker.core.base

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import com.example.util.simpletimetracker.core.extension.allowDiskWrite

abstract class BaseActivity : AppCompatActivity() {

    override fun attachBaseContext(newBase: Context?) {
        // Suppress strictMode for per app language prefs read for stored locale (see autoStoreLocales).
        // Only for api lower than 33.
        allowDiskWrite { super.attachBaseContext(newBase) }
    }

    inline fun <T> LiveData<T>.observe(
        crossinline onChanged: (T) -> Unit,
    ) {
        observe(this@BaseActivity) { onChanged(it) }
    }
}