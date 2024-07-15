package com.example.util.simpletimetracker.feature_pomodoro.settings.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.util.simpletimetracker.core.base.BaseViewModel
import com.example.util.simpletimetracker.core.extension.lazySuspend
import com.example.util.simpletimetracker.core.extension.set
import com.example.util.simpletimetracker.core.viewData.SettingsBlock
import com.example.util.simpletimetracker.domain.interactor.PomodoroCycleNotificationInteractor
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.hintBig.HintBigViewData
import com.example.util.simpletimetracker.feature_pomodoro.settings.interactor.PomodoroSettingsViewDataInteractor
import com.example.util.simpletimetracker.feature_pomodoro.settings.model.PomodoroHintButtonType
import com.example.util.simpletimetracker.navigation.Router
import com.example.util.simpletimetracker.navigation.params.action.OpenSystemSettings
import com.example.util.simpletimetracker.navigation.params.screen.DurationDialogParams
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PomodoroSettingsViewModel @Inject constructor(
    private val router: Router,
    private val prefsInteractor: PrefsInteractor,
    private val pomodoroSettingsViewDataInteractor: PomodoroSettingsViewDataInteractor,
    private val pomodoroCycleNotificationInteractor: PomodoroCycleNotificationInteractor,
) : BaseViewModel() {

    val content: LiveData<List<ViewHolderType>> by lazySuspend { loadContent() }

    fun onVisible() = viewModelScope.launch {
        updateContent()
    }

    fun onHintActionClicked(type: HintBigViewData.ButtonType?) {
        when (type) {
            is PomodoroHintButtonType.PostPermissions -> {
                router.execute(OpenSystemSettings.Notifications)
            }
            is PomodoroHintButtonType.ExactAlarms -> {
                router.execute(OpenSystemSettings.ExactAlarms)
            }
        }
    }

    fun onBlockClicked(block: SettingsBlock) {
        when (block) {
            SettingsBlock.PomodoroFocusTime ->
                onFocusTimeClicked()
            SettingsBlock.PomodoroBreakTime ->
                onBreakTimeClicked()
            SettingsBlock.PomodoroLongBreakTime ->
                onLongBreakTimeClicked()
            SettingsBlock.PomodoroPeriodsUntilLongBreak -> {
                onPeriodsUntilLongBreakClicked()
            }
            else -> {
                // Do nothing.
            }
        }
    }

    @Suppress("UNUSED_PARAMETER")
    fun onSpinnerPositionSelected(block: SettingsBlock, position: Int) {
        // Do nothing.
    }

    fun onDurationSet(tag: String?, duration: Long) = viewModelScope.launch {
        when (tag) {
            FOCUS_TIME_DURATION_DIALOG_TAG -> {
                if (duration != 0L) prefsInteractor.setPomodoroFocusTime(duration)
            }
            BREAK_TIME_DURATION_DIALOG_TAG -> {
                prefsInteractor.setPomodoroBreakTime(duration)
            }
            LONG_BREAK_TIME_DURATION_DIALOG_TAG -> {
                prefsInteractor.setPomodoroLongBreakTime(duration)
            }
        }
        updateContent()
        pomodoroCycleNotificationInteractor.checkAndReschedule()
    }

    fun onCountSet(tag: String?, count: Long) = viewModelScope.launch {
        when (tag) {
            PERIODS_UNTIL_LONG_BREAK_DIALOG_TAG -> {
                prefsInteractor.setPomodoroPeriodsUntilLongBreak(count)
            }
        }
        updateContent()
        pomodoroCycleNotificationInteractor.checkAndReschedule()
    }

    fun onDurationDisabled(tag: String?) = viewModelScope.launch {
        when (tag) {
            FOCUS_TIME_DURATION_DIALOG_TAG -> {
                // Do nothing
            }
            BREAK_TIME_DURATION_DIALOG_TAG -> {
                prefsInteractor.setPomodoroBreakTime(0)
                updateContent()
            }
            LONG_BREAK_TIME_DURATION_DIALOG_TAG -> {
                prefsInteractor.setPomodoroLongBreakTime(0)
            }
            PERIODS_UNTIL_LONG_BREAK_DIALOG_TAG -> {
                prefsInteractor.setPomodoroPeriodsUntilLongBreak(0)
            }
        }
        updateContent()
        pomodoroCycleNotificationInteractor.checkAndReschedule()
    }

    private fun onFocusTimeClicked() = viewModelScope.launch {
        val duration = prefsInteractor.getPomodoroFocusTime()
        DurationDialogParams(
            tag = FOCUS_TIME_DURATION_DIALOG_TAG,
            value = DurationDialogParams.Value.Duration(duration),
            hideDisableButton = true,
        ).let(router::navigate)
    }

    private fun onBreakTimeClicked() = viewModelScope.launch {
        val duration = prefsInteractor.getPomodoroBreakTime()
        DurationDialogParams(
            tag = BREAK_TIME_DURATION_DIALOG_TAG,
            value = DurationDialogParams.Value.Duration(duration),
        ).let(router::navigate)
    }

    private fun onLongBreakTimeClicked() = viewModelScope.launch {
        val duration = prefsInteractor.getPomodoroLongBreakTime()
        DurationDialogParams(
            tag = LONG_BREAK_TIME_DURATION_DIALOG_TAG,
            value = DurationDialogParams.Value.Duration(duration),
        ).let(router::navigate)
    }

    private fun onPeriodsUntilLongBreakClicked() = viewModelScope.launch {
        val count = prefsInteractor.getPomodoroPeriodsUntilLongBreak()
        DurationDialogParams(
            tag = PERIODS_UNTIL_LONG_BREAK_DIALOG_TAG,
            value = DurationDialogParams.Value.Count(count),
        ).let(router::navigate)
    }

    private suspend fun updateContent() {
        content.set(loadContent())
    }

    private suspend fun loadContent(): List<ViewHolderType> {
        return pomodoroSettingsViewDataInteractor.execute()
    }

    companion object {
        const val FOCUS_TIME_DURATION_DIALOG_TAG = "FOCUS_TIME_DURATION_DIALOG_TAG"
        const val BREAK_TIME_DURATION_DIALOG_TAG = "BREAK_TIME_DURATION_DIALOG_TAG"
        const val LONG_BREAK_TIME_DURATION_DIALOG_TAG = "LONG_BREAK_TIME_DURATION_DIALOG_TAG"
        const val PERIODS_UNTIL_LONG_BREAK_DIALOG_TAG = "PERIODS_UNTIL_LONG_BREAK_DIALOG_TAG"
    }
}
