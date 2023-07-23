package com.example.util.simpletimetracker.core.base

import kotlinx.coroutines.CoroutineScope

interface ScopeHolder {

    fun getScope(): CoroutineScope
}