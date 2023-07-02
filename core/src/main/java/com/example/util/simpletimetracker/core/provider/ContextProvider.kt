package com.example.util.simpletimetracker.core.provider

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import java.lang.ref.WeakReference
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ContextProvider @Inject constructor(
    @ApplicationContext private val applicationContext: Context,
) {

    private var context: WeakReference<Context>? = null

    fun attach(context: Context) {
        this.context = WeakReference(context)
    }

    fun get(): Context {
        // Fallback to application context just in case.
        return context?.get() ?: applicationContext
    }
}