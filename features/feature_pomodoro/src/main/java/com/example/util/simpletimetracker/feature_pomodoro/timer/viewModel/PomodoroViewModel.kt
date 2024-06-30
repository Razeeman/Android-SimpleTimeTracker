package com.example.util.simpletimetracker.feature_pomodoro.timer.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.util.simpletimetracker.core.base.BaseViewModel
import com.example.util.simpletimetracker.core.extension.set
import com.example.util.simpletimetracker.domain.interactor.GetPomodoroSettingsInteractor
import com.example.util.simpletimetracker.domain.interactor.PomodoroNextCycleInteractor
import com.example.util.simpletimetracker.domain.interactor.PomodoroRestartCycleInteractor
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.interactor.PomodoroStartInteractor
import com.example.util.simpletimetracker.domain.interactor.PomodoroStopInteractor
import com.example.util.simpletimetracker.feature_pomodoro.timer.mapper.PomodoroViewDataMapper
import com.example.util.simpletimetracker.feature_pomodoro.timer.model.PomodoroButtonState
import com.example.util.simpletimetracker.feature_pomodoro.timer.model.PomodoroTimerState
import com.example.util.simpletimetracker.navigation.Router
import com.example.util.simpletimetracker.navigation.params.screen.PomodoroSettingsParams
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PomodoroViewModel @Inject constructor(
    private val router: Router,
    private val pomodoroViewDataMapper: PomodoroViewDataMapper,
    private val prefsInteractor: PrefsInteractor,
    private val pomodoroStartInteractor: PomodoroStartInteractor,
    private val pomodoroStopInteractor: PomodoroStopInteractor,
    private val pomodoroNextCycleInteractor: PomodoroNextCycleInteractor,
    private val pomodoroRestartCycleInteractor: PomodoroRestartCycleInteractor,
    private val getPomodoroSettingsInteractor: GetPomodoroSettingsInteractor,
) : BaseViewModel() {

    val buttonState: LiveData<PomodoroButtonState> = MutableLiveData()
    val timerState: LiveData<PomodoroTimerState> = MutableLiveData()

    private var timerJob: Job? = null

    fun onVisible() {
        startUpdate()
    }

    fun onHidden() {
        stopUpdate()
    }

    fun onSettingsClicked() {
        router.navigate(PomodoroSettingsParams)
    }

    fun onStartStopClicked() = viewModelScope.launch {
        // 0 - disabled.
        if (isStarted()) {
            pomodoroStopInteractor.stop()
        } else {
            pomodoroStartInteractor.start()
        }
        updateButtonState()
        updateTimerState()
    }

    fun onRestartClicked() = viewModelScope.launch {
        pomodoroRestartCycleInteractor.execute()
        // Reset animation.
        timerState.value?.copy(progress = 0)?.let(timerState::set)
        updateTimerState()
    }

    fun onNextClicked() = viewModelScope.launch {
        pomodoroNextCycleInteractor.execute()
        // Reset animation.
        timerState.value?.copy(progress = 0)?.let(timerState::set)
        updateTimerState()
    }

    private suspend fun isStarted(): Boolean {
        return prefsInteractor.getPomodoroModeStartedTimestampMs() != 0L
    }

    private fun startUpdate() {
        timerJob = viewModelScope.launch {
            timerJob?.cancelAndJoin()
            while (isActive) {
                updateButtonState()
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
            settings = getPomodoroSettingsInteractor.execute(),
        )
    }

    companion object {
        private const val TIMER_UPDATE_MS = 1000L
    }
}
