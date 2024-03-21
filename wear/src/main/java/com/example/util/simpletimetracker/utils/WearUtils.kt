/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.example.util.simpletimetracker.utils

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import com.example.util.simpletimetracker.presentation.MainActivity
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

@SuppressLint("WearRecents")
fun getMainStartIntent(context: Context): PendingIntent {
    val startIntent = Intent(context, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
    }

    return PendingIntent.getActivity(
        context,
        0,
        startIntent,
        getPendingIntentFlags(),
    )
}

fun String.padDuration(): String = this.padStart(2, '0')

fun Long?.orZero(): Long = this ?: 0

@Suppress("UNCHECKED_CAST")
internal inline fun <reified T : Any> SharedPreferences.delegate(
    key: String,
    default: T,
) = object : ReadWriteProperty<Any?, T> {

    override fun getValue(thisRef: Any?, property: KProperty<*>): T {
        val data = when (default) {
            is Boolean -> (getBoolean(key, default) as? T) ?: default
            is Int -> (getInt(key, default) as? T) ?: default
            is Long -> (getLong(key, default) as? T) ?: default
            is String -> (getString(key, default) as? T) ?: default
            is Set<*> -> (getStringSet(key, default as? Set<String>)?.toSet() as? T) ?: default
            else -> throw IllegalArgumentException(
                "Prefs delegate not implemented for class ${(default as Any?)?.javaClass}",
            )
        }
        return data
    }

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) = with(edit()) {
        when (value) {
            is Boolean -> putBoolean(key, value)
            is Int -> putInt(key, value)
            is Long -> putLong(key, value)
            is String -> putString(key, value)
            is Set<*> -> putStringSet(key, value as? Set<String>)
            else -> throw IllegalArgumentException(
                "Prefs delegate not implemented for class ${(default as Any?)?.javaClass}",
            )
        }
        apply()
    }
}

private fun getPendingIntentFlags(): Int {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
    } else {
        PendingIntent.FLAG_UPDATE_CURRENT
    }
}