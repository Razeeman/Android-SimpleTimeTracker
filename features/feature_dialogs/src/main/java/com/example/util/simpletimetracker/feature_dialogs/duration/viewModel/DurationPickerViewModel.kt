package com.example.util.simpletimetracker.feature_dialogs.duration.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.util.simpletimetracker.core.base.BaseViewModel
import com.example.util.simpletimetracker.core.extension.lazySuspend
import com.example.util.simpletimetracker.core.extension.set
import com.example.util.simpletimetracker.domain.durationSuggestion.interactor.DurationSuggestionInteractor
import com.example.util.simpletimetracker.domain.durationSuggestion.model.DurationSuggestion
import com.example.util.simpletimetracker.domain.extension.orZero
import com.example.util.simpletimetracker.domain.extension.padDuration
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_dialogs.duration.adapter.DurationSuggestionViewData
import com.example.util.simpletimetracker.feature_dialogs.duration.customView.DurationView
import com.example.util.simpletimetracker.feature_dialogs.duration.customView.NumberKeyboardView
import com.example.util.simpletimetracker.feature_dialogs.duration.interactor.DurationPickerViewDataInteractor
import com.example.util.simpletimetracker.feature_dialogs.duration.model.DurationDialogState
import com.example.util.simpletimetracker.navigation.params.screen.DurationDialogParams
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class DurationPickerViewModel @Inject constructor(
    private val durationSuggestionInteractor: DurationSuggestionInteractor,
    private val durationPickerViewDataInteractor: DurationPickerViewDataInteractor,
) : BaseViewModel() {

    lateinit var extra: DurationDialogParams

    val stateViewData: LiveData<DurationDialogState> by lazySuspend {
        reformattedValue = reformatValue(extra.value)
        loadViewData()
    }
    val suggestionsViewData: LiveData<List<ViewHolderType>> by lazySuspend {
        loadSuggestionsViewData()
    }

    private var reformattedValue: Long = 0

    fun onButtonPressed(button: NumberKeyboardView.Button) {
        when (button) {
            is NumberKeyboardView.Button.Number -> onNumberPressed(button.value)
            is NumberKeyboardView.Button.Delete -> onNumberDelete(button.isLongClick)
            is NumberKeyboardView.Button.DoubleZero -> {
                onNumberPressed(0)
                onNumberPressed(0)
            }
        }
    }

    fun onValueChanged(viewData: DurationView.ViewData) {
        val newValue = TimeUnit.HOURS.toSeconds(viewData.hours) +
            TimeUnit.MINUTES.toSeconds(viewData.minutes) +
            viewData.seconds
        val newFormattedValue = mapToFormattedValue(newValue)
        if (newFormattedValue != reformattedValue) {
            reformattedValue = newFormattedValue
            updateViewData()
        }
    }

    fun onSuggestionClick(viewData: DurationSuggestionViewData) = viewModelScope.launch {
        when (viewData.type) {
            is DurationSuggestionViewData.Type.Add -> onAddSuggestion()
            is DurationSuggestionViewData.Type.Value -> onSuggestionClicked(viewData.type)
        }
    }

    fun onSuggestionLongClick(viewData: DurationSuggestionViewData) = viewModelScope.launch {
        when (viewData.type) {
            is DurationSuggestionViewData.Type.Add -> onAddSuggestion()
            is DurationSuggestionViewData.Type.Value -> onSuggestionLongClicked(viewData.type)
        }
    }

    private suspend fun onAddSuggestion() {
        val currentDuration = stateViewData.value?.value?.getDurationSeconds() ?: return
        val existingDurations = durationSuggestionInteractor.getAll().map { it.valueSeconds }
        if (currentDuration !in existingDurations) {
            // Zero id creates new record
            val newSuggestion = DurationSuggestion(
                id = 0L,
                valueSeconds = currentDuration,
            )
            durationSuggestionInteractor.add(newSuggestion)
            updateSuggestionsViewData()
        }
    }

    private fun onSuggestionClicked(viewData: DurationSuggestionViewData.Type.Value) {
        reformattedValue = mapToFormattedValue(viewData.value)
        updateViewData()
    }

    private suspend fun onSuggestionLongClicked(viewData: DurationSuggestionViewData.Type.Value) {
        durationSuggestionInteractor.getAll()
            .filter { it.valueSeconds == viewData.value }
            .forEach { durationSuggestionInteractor.remove(it.id) }
        updateSuggestionsViewData()
    }

    private fun onNumberPressed(number: Int) {
        if (reformattedValue <= 9999_99_99) {
            reformattedValue = if (extra.showSeconds) {
                reformattedValue * 10 + number
            } else {
                val seconds = reformattedValue % 100
                ((reformattedValue / 100) * 10 + number) * 100 + seconds
            }
            updateViewData()
        }
    }

    private fun onNumberDelete(isLongClick: Boolean) {
        if (isLongClick) {
            reformattedValue = 0
        } else {
            reformattedValue = if (extra.showSeconds) {
                reformattedValue / 10
            } else {
                val seconds = reformattedValue % 100
                ((reformattedValue / 100) / 10) * 100 + seconds
            }
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
            is DurationDialogParams.Value.DurationSeconds -> mapToFormattedValue(value.duration)
            is DurationDialogParams.Value.Count -> value.count
        }
    }

    private fun mapToFormattedValue(duration: Long): Long {
        fun format(value: Long): String = value.toString().padDuration()

        val hr = duration
            .let(TimeUnit.SECONDS::toHours)
        val min = (duration - TimeUnit.HOURS.toSeconds(hr))
            .let(TimeUnit.SECONDS::toMinutes)
        val sec = (duration - TimeUnit.HOURS.toSeconds(hr) - TimeUnit.MINUTES.toSeconds(min))
            .let(TimeUnit.SECONDS::toSeconds)

        return (format(hr) + format(min) + format(sec)).toLongOrNull().orZero()
    }

    private suspend fun updateSuggestionsViewData() {
        val data = loadSuggestionsViewData()
        suggestionsViewData.set(data)
    }

    private suspend fun loadSuggestionsViewData(): List<ViewHolderType> {
        return durationPickerViewDataInteractor.getSuggestionsViewData(extra)
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
                    data = reformattedValue,
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
