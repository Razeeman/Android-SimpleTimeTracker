package com.example.util.simpletimetracker.core.base

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

abstract class ViewModelDelegate : ScopeHolder {

    val delegateScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    override fun getScope(): CoroutineScope {
        return delegateScope
    }

    open fun clear() {
        delegateScope.cancel()
    }
}