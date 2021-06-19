package com.example.util.simpletimetracker.core.base

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData

abstract class BaseActivity : AppCompatActivity() {

    inline fun <T> LiveData<T>.observe(
        crossinline onChanged: (T) -> Unit
    ) {
        observe(this@BaseActivity, { onChanged(it) })
    }
}