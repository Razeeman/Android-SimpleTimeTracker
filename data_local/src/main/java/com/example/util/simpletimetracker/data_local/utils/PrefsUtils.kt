package com.example.util.simpletimetracker.data_local.utils

import android.content.SharedPreferences
import com.example.util.simpletimetracker.data_local.repo.RepoConstants.LOG_MESSAGE_CACHE
import com.example.util.simpletimetracker.data_local.repo.RepoConstants.LOG_MESSAGE_DISK_PREFIX
import com.example.util.simpletimetracker.data_local.repo.RepoConstants.LOG_MESSAGE_PREFIX
import com.example.util.simpletimetracker.data_local.repo.RepoConstants.LOG_MESSAGE_PREFS_PREFIX
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import timber.log.Timber
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * When adding new types - add support for the backups.
 */
@Suppress("UNCHECKED_CAST")
internal inline fun <reified T : Any> SharedPreferences.delegate(
    key: String,
    default: T,
) = object : ReadWriteProperty<Any?, T> {

    override fun getValue(thisRef: Any?, property: KProperty<*>): T {
        logPrefsDataAccess("get $key")
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
        logPrefsDataAccess("set $key")
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

internal suspend inline fun <T> Mutex.withLockedCache(
    logMessage: String,
    crossinline accessCache: () -> T? = { null },
    crossinline accessSource: suspend () -> T,
): T {
    return withLockedCache(
        logMessage = logMessage,
        accessCache = accessCache,
        accessSource = accessSource,
        afterSourceAccess = {},
    )
}

internal suspend inline fun <T> Mutex.withLockedCache(
    logMessage: String,
    crossinline accessCache: () -> T? = { null },
    crossinline accessSource: suspend () -> T,
    crossinline afterSourceAccess: suspend (T) -> Unit,
): T {
    return withContext(Dispatchers.IO) {
        this@withLockedCache.withLock {
            accessCache()?.let {
                logDataAccess("$logMessage ($LOG_MESSAGE_CACHE)")
                it
            } ?: run {
                logDataAccess(logMessage)
                accessSource().also { afterSourceAccess(it) }
            }
        }
    }
}

/**
 * Inlined for message tag to be an actual class name at call site.
 */
@Suppress("NOTHING_TO_INLINE")
private inline fun logDataAccessInternal(logMessage: String) {
    Timber.d("$LOG_MESSAGE_PREFIX $logMessage")
}

/**
 * Inlined for message tag to be an actual class name at call site.
 */
@Suppress("NOTHING_TO_INLINE")
internal inline fun logDataAccess(logMessage: String) {
    logDataAccessInternal("$LOG_MESSAGE_DISK_PREFIX $logMessage")
}

/**
 * Inlined for message tag to be an actual class name at call site.
 */
@Suppress("NOTHING_TO_INLINE")
internal inline fun logPrefsDataAccess(logMessage: String) {
    logDataAccessInternal("$LOG_MESSAGE_PREFS_PREFIX $logMessage")
}

/**
 * Produces a new list from original list by removing elements satisfying filter block.
 */
inline fun <T> List<T>.removeIf(crossinline filter: (T) -> Boolean): List<T> {
    return this.toMutableList().apply { removeAll { filter(it) } }
}

/**
 * Produces a new list from original list by replacing elements satisfying filter block with new element.
 */
inline fun <T> List<T>.replaceWith(new: T, crossinline filter: (T) -> Boolean): List<T> {
    return this.removeIf(filter).toMutableList().apply { add(new) }
}