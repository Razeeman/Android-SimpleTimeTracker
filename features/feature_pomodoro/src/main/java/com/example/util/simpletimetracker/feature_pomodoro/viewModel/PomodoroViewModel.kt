package com.example.util.simpletimetracker.feature_pomodoro.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.util.simpletimetracker.core.base.BaseViewModel
import com.example.util.simpletimetracker.core.extension.lazySuspend
import com.example.util.simpletimetracker.core.extension.set
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.feature_pomodoro.mapper.PomodoroViewDataMapper
import com.example.util.simpletimetracker.feature_pomodoro.model.PomodoroButtonState
import com.example.util.simpletimetracker.feature_pomodoro.model.PomodoroTimerState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PomodoroViewModel @Inject constructor(
    private val pomodoroViewDataMapper: PomodoroViewDataMapper,
    private val prefsInteractor: PrefsInteractor,
) : BaseViewModel() {

    val buttonState: LiveData<PomodoroButtonState> by lazySuspend { loadButtonState() }
    val timerState: LiveData<PomodoroTimerState> by lazySuspend { loadTimerState() }

    private var timerJob: Job? = null

    fun onVisible() = viewModelScope.launch {
        checkIfNeedToStartUpdate()
    }

    fun onHidden() {
        stopUpdate()
    }

    fun onSettingsClicked() {
        // TODO POM
    }

    fun onStartStopClicked() = viewModelScope.launch {
        // 0 - disabled.
        val newValue = if (isStarted()) 0 else System.currentTimeMillis()
        prefsInteractor.setPomodoroModeStartedTimestampMs(newValue)
        checkIfNeedToStartUpdate()
        updateButtonState()
        updateTimerState()
    }

    private suspend fun isStarted(): Boolean {
        return prefsInteractor.getPomodoroModeStartedTimestampMs() != 0L
    }

    private suspend fun checkIfNeedToStartUpdate() {
        if (isStarted()) startUpdate() else stopUpdate()
    }

    private fun startUpdate() {
        timerJob = viewModelScope.launch {
            timerJob?.cancelAndJoin()
            while (isActive) {
                updateTimerState()
                delay(TIMER_UPDATE_MS)
            }
        }
    }

    private fun stopUpdate() {
        viewModelScope.launch {
            timerJob?.cancelAndJoin()
        }
    }

    private suspend fun updateButtonState() {
        val data = loadButtonState()
        buttonState.set(data)
    }

    private suspend fun loadButtonState(): PomodoroButtonState {
        return pomodoroViewDataMapper.mapButtonState(
            isStarted = isStarted(),
        )
    }

    private suspend fun updateTimerState() {
        val data = loadTimerState()
        timerState.set(data)
    }

    private suspend fun loadTimerState(): PomodoroTimerState {
        return pomodoroViewDataMapper.mapTimerState(
            isStarted = isStarted(),
            timeStartedMs = prefsInteractor.getPomodoroModeStartedTimestampMs(),
            timerUpdateMs = TIMER_UPDATE_MS,
        )
    }

    companion object {
        private const val TIMER_UPDATE_MS = 1000L
    }
}
