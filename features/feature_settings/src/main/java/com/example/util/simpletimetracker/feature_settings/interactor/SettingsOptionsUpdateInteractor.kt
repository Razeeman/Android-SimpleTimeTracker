package com.example.util.simpletimetracker.feature_settings.interactor

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsOptionsUpdateInteractor @Inject constructor() {

    val dismiss: SharedFlow<Unit> get() = _dismiss.asSharedFlow()

    private val _dismiss = MutableSharedFlow<Unit>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    suspend fun sendDismiss() {
        _dismiss.emit(Unit)
    }
}