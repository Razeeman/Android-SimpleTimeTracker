package com.example.util.simpletimetracker.core.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.util.simpletimetracker.core.extension.allowDiskRead
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job

abstract class BaseViewModel :
    ViewModel(),
    ScopeHolder,
    Throttler,
    DelayLoadHandler {

    override var throttleJob: Job? = null

    override var delayDataLoad: Boolean = true

    override fun getScope(): CoroutineScope {
        return allowDiskRead { viewModelScope }
    }
}