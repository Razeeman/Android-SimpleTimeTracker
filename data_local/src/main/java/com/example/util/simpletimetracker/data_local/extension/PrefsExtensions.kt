package com.example.util.simpletimetracker.data_local.extension

import android.content.SharedPreferences
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

@Suppress("UNCHECKED_CAST")
inline fun <reified T> SharedPreferences.delegate(
    key: String,
    default: T
) = object : ReadWriteProperty<Any?, T> {
    override fun getValue(thisRef: Any?, property: KProperty<*>): T =
        when (default) {
            is Boolean -> (getBoolean(key, default) as? T) ?: default
            is String -> (getString(key, default) as? T) ?: default
            is Set<*> -> (getStringSet(key, default as? Set<String>)?.toSet() as? T) ?: default
            else -> throw IllegalArgumentException(
                "Prefs delegate not implemented for class ${(default as Any?)?.javaClass}"
            )
        }

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) = with(edit()) {
        when (value) {
            is Boolean -> putBoolean(key, value)
            is String -> putString(key, value)
            is Set<*> -> putStringSet(key, value as? Set<String>)
            else -> throw IllegalArgumentException(
                "Prefs delegate not implemented for class ${(default as Any?)?.javaClass}"
            )
        }
        apply()
    }
}