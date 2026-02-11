package com.example.util.simpletimetracker.domain.base

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

fun <T> CoroutineScope.suspendLazy(
    initializer: suspend CoroutineScope.() -> T,
) = object : SuspendLazy<T> {
    private val mutex = Mutex()
    private var value: T? = null

    override suspend operator fun invoke(): T {
        value?.let { return it }
        return mutex.withLock {
            value?.let { return it }
            initializer().also { value = it }
        }
    }
}

interface SuspendLazy<T> {
    suspend operator fun invoke(): T
}