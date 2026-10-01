/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.example.util.simpletimetracker.features.tagValueSelection.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.util.simpletimetracker.domain.base.TAG_VALUE_DECIMAL_DELIMITER
import com.example.util.simpletimetracker.domain.base.TAG_VALUE_MINUS_SIGN
import com.example.util.simpletimetracker.features.tagValueSelection.interactor.TagValueSelectedInteractor
import com.example.util.simpletimetracker.features.tagValueSelection.mapper.TagValueSelectionViewDataMapper
import com.example.util.simpletimetracker.features.tagValueSelection.screen.TagValueSelectionButton
import com.example.util.simpletimetracker.features.tagValueSelection.screen.TagValueSelectionState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TagValueSelectionViewModel @Inject constructor(
    private val tagValueSelectionViewDataMapper: TagValueSelectionViewDataMapper,
    private val tagValueSelectedInteractor: TagValueSelectedInteractor,
) : ViewModel() {

    val state: StateFlow<TagValueSelectionState> get() = _state.asStateFlow()
    private val _state: MutableStateFlow<TagValueSelectionState> = MutableStateFlow(mapDefaultState())

    val effects: SharedFlow<Effect> get() = _effects.asSharedFlow()
    private val _effects = MutableSharedFlow<Effect>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    private var isInitialized = false
    private var tagId: Long? = null
    private var currentValue: String = ""

    fun init(activityId: Long) {
        if (isInitialized) return
        this.tagId = activityId
        isInitialized = true
    }

    fun onButtonClick(buttonType: TagValueSelectionButton) = viewModelScope.launch {
        when (buttonType) {
            is TagValueSelectionButton.Number -> {
                currentValue += buttonType.value.toString()
            }
            is TagValueSelectionButton.Dot -> {
                currentValue += TAG_VALUE_DECIMAL_DELIMITER
            }
            is TagValueSelectionButton.PlusMinus -> {
                currentValue = if (currentValue.startsWith(TAG_VALUE_MINUS_SIGN)) {
                    currentValue.removePrefix(TAG_VALUE_MINUS_SIGN.toString())
                } else {
                    TAG_VALUE_MINUS_SIGN + currentValue
                }
            }
            is TagValueSelectionButton.Delete -> {
                currentValue = currentValue.dropLast(1)
            }
            is TagValueSelectionButton.Save -> onTagValueSelected()
        }
        _state.value = mapState()
    }

    private fun onTagValueSelected() = viewModelScope.launch {
        val actualTagValue = currentValue
            .replace(TAG_VALUE_DECIMAL_DELIMITER, '.')
            .replace(TAG_VALUE_MINUS_SIGN, '-')
            .toDoubleOrNull()
            ?.takeIf { it.isFinite() }
        val result = TagValueSelectedInteractor.Result(
            tagId = tagId ?: return@launch,
            value = actualTagValue,
        )
        tagValueSelectedInteractor.send(result)
        _effects.emit(Effect.OnComplete)
    }

    private fun mapDefaultState(): TagValueSelectionState {
        return tagValueSelectionViewDataMapper.mapState("")
    }

    private fun mapState(): TagValueSelectionState {
        return tagValueSelectionViewDataMapper.mapState(currentValue)
    }

    sealed interface Effect {
        data object OnComplete : Effect
    }
}