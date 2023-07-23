package com.example.util.simpletimetracker.core.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope

abstract class BaseViewModel : ViewModel(), ScopeHolder {

    override fun getScope(): CoroutineScope { return viewModelScope }
}