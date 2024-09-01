package com.example.util.simpletimetracker.feature_dialogs.duration.viewModel

import androidx.lifecycle.LiveData
import com.example.util.simpletimetracker.core.base.BaseViewModel
import com.example.util.simpletimetracker.core.extension.lazySuspend
import com.example.util.simpletimetracker.core.extension.set
import com.example.util.simpletimetracker.domain.extension.orZero
import com.example.util.simpletimetracker.domain.extension.padDuration
import com.example.util.simpletimetracker.feature_dialogs.duration.customView.DurationView
import com.example.util.simpletimetracker.feature_dialogs.duration.customView.NumberKeyboardView
import com.example.util.simpletimetracker.feature_dialogs.duration.model.DurationDialogState
import com.example.util.simpletimetracker.navigation.params.screen.DurationDialogParams
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class DurationPickerViewModel @Inject constructor() : BaseViewModel() {

    lateinit var extra: DurationDialogParams

    val stateViewData: LiveData<DurationDialogState> by lazySuspend {
        reformattedValue = reformatValue(extra.value)
        loadViewData()
    }

    private var reformattedValue: Long = 0

    fun onButtonPressed(button: NumberKeyboardView.Button) {
        when (button) {
            is NumberKeyboardView.Button.Number -> onNumberPressed(button.value)
            is NumberKeyboardView.Button.Delete -> onNumberDelete()
            is NumberKeyboardView.Button.DoubleZero -> {
                onNumberPressed(0)
                onNumberPressed(0)
            }
        }
    }

    private fun onNumberPressed(number: Int) {
        if (reformattedValue <= 999_99_99) {
            reformattedValue = if (extra.showSeconds) {
                reformattedValue * 10 + number
            } else {
                val seconds = reformattedValue % 100
                ((reformattedValue / 100) * 10 + number) * 100 + seconds
            }
            updateViewData()
        }
    }

    private fun onNumberDelete() {
        reformattedValue = if (extra.showSeconds) {
            reformattedValue / 10
        } else {
            val seconds = reformattedValue % 100
            ((reformattedValue / 100) / 10) * 100 + seconds
        }
        updateViewData()
    }

    private fun mapToDurationViewData(durationString: Long): DurationView.ViewData {
        val hours = durationString / 10000
        val minutes = (durationString / 100) % 100
        val seconds = durationString % 100

        return DurationView.ViewData(
            hours = hours,
            minutes = minutes,
            seconds = seconds,
            showSeconds = extra.showSeconds,
        )
    }

    private fun reformatValue(value: DurationDialogParams.Value): Long {
        return when (value) {
            is DurationDialogParams.Value.DurationSeconds -> reformatDurationValue(value.duration)
            is DurationDialogParams.Value.Count -> value.count
        }
    }

    private fun reformatDurationValue(duration: Long): Long {
        fun format(value: Long): String = value.toString().padDuration()

        val hr = duration
            .let(TimeUnit.SECONDS::toHours)
        val min = (duration - TimeUnit.HOURS.toSeconds(hr))
            .let(TimeUnit.SECONDS::toMinutes)
        val sec = (duration - TimeUnit.HOURS.toSeconds(hr) - TimeUnit.MINUTES.toSeconds(min))
            .let(TimeUnit.SECONDS::toSeconds)

        return (format(hr) + format(min) + format(sec)).toLongOrNull().orZero()
    }

    private fun updateViewData() {
        val data = loadViewData()
        stateViewData.set(data)
    }

    private fun loadViewData(): DurationDialogState {
        val state = when (extra.value) {
            is DurationDialogParams.Value.DurationSeconds -> {
                DurationDialogState.Value.Duration(
                    data = mapToDurationViewData(reformattedValue),
                )
            }
            is DurationDialogParams.Value.Count -> {
                DurationDialogState.Value.Count(
                    data = reformattedValue.toString(),
                )
            }
        }

        return DurationDialogState(
            showDisableButton = !extra.hideDisableButton,
            showSeconds = extra.showSeconds,
            value = state,
        )
    }
}
